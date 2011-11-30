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

  // TODO(damonkohler): Allow the TcpClientConnection to alter the reconnect
  // strategy (e.g. binary backoff, faster retries for tests, etc.)
  private static final long RECONNECT_DELAY = 1000;

  private final TcpClientConnection context;
  private final Timer timer;

  public RetryingConnectionHandler(TcpClientConnection context) {
    this.context = context;
    this.timer = new Timer();
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    context.setChannel(null);
    if (context.isPersistent()) {
      if (DEBUG) {
        log.info("Channel closed, will reconnect: " + e.getChannel().toString());
      }
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          SocketAddress remoteAddress = context.getRemoteAddress();
          if (DEBUG) {
            log.info("Reconnecting to: " + remoteAddress);
          }
          ChannelFuture future =
              context.getBootstrap().connect(remoteAddress).awaitUninterruptibly();
          if (future.isSuccess()) {
            context.setChannel(future.getChannel());
          }
        }
      }, RECONNECT_DELAY);
    } else {
      if (DEBUG) {
        log.info("Channel closed, will not reconnect: " + e.getChannel().toString());
      }
    }
    super.channelClosed(ctx, e);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    if (DEBUG) {
      log.info("Channel exception: " + e.getChannel().toString());
    }
    e.getChannel().close();
    super.exceptionCaught(ctx, e);
  }
}
