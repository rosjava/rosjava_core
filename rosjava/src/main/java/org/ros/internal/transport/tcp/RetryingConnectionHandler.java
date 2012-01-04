/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.transport.tcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Automatically reconnects when a {@link Channel} is closed.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RetryingConnectionHandler extends SimpleChannelHandler {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(RetryingConnectionHandler.class);

  private static final String CONNECTION_REFUSED = "Connection refused";

  // TODO(damonkohler): Allow the TcpClientConnection to alter the
  // reconnect strategy (e.g. binary backoff, faster retries for tests, etc.)
  private static final long RECONNECT_DELAY = 1000;

  private final TcpClientConnection tcpClientConnection;
  private final Timer timer;

  public RetryingConnectionHandler(TcpClientConnection tcpClientConnection) {
    this.tcpClientConnection = tcpClientConnection;
    this.timer = new Timer();
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    tcpClientConnection.setChannel(null);
    if (DEBUG) {
      if (tcpClientConnection.isDefunct()) {
        log.info("Connection defunct: " + tcpClientConnection.getName());
      }
    }
    if (tcpClientConnection.isPersistent() && !tcpClientConnection.isDefunct()) {
      if (DEBUG) {
        log.info("Connection closed, will reconnect: " + tcpClientConnection.getName());
      }
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          SocketAddress remoteAddress = tcpClientConnection.getRemoteAddress();
          if (DEBUG) {
            log.info("Reconnecting: " + tcpClientConnection.getName());
          }
          ChannelFuture future =
              tcpClientConnection.getBootstrap().connect(remoteAddress).awaitUninterruptibly();
          if (future.isSuccess()) {
            tcpClientConnection.setChannel(future.getChannel());
            if (DEBUG) {
              log.info("Reconnect successful: " + tcpClientConnection.getName());
            }
          } else {
            if (DEBUG) {
              log.error("Reconnect failed: " + tcpClientConnection.getName(), future.getCause());
            }
            // TODO(damonkohler): Is there a better way to check for connection
            // refused?
            if (future.getCause() instanceof ConnectException
                && future.getCause().getMessage().equals(CONNECTION_REFUSED)) {
              if (DEBUG) {
                log.error(
                    "Connection refused, marking as defunct: " + tcpClientConnection.getName(),
                    future.getCause());
              }
              // TODO(damonkohler): Add a listener so that publishers and
              // subscribers can be notified when they lose a connection.
              tcpClientConnection.setDefunct(true);
            }
          }
        }
      }, RECONNECT_DELAY);
    } else {
      if (DEBUG) {
        log.info("Connection closed, will not reconnect: " + tcpClientConnection.getName());
      }
    }
    super.channelClosed(ctx, e);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    if (DEBUG) {
      log.error("Connection exception: " + tcpClientConnection.getName(), e.getCause());
    }
    e.getChannel().close();
    super.exceptionCaught(ctx, e);
  }
}
