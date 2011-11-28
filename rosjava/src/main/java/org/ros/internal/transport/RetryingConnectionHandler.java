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

package org.ros.internal.transport;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.exception.RosRuntimeException;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Automatically reconnects when a {@link Channel} is closed.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RetryingConnectionHandler extends SimpleChannelHandler {

  private static final boolean DEBUG = true;
  private static final Log log = LogFactory.getLog(RetryingConnectionHandler.class);

  // TODO(damonkohler): Use binary backoff.
  private static final long RECONNECT_DELAY = 1000;

  private final ClientBootstrap bootstrap;
  private final Timer timer;

  private volatile boolean reconnect;

  public RetryingConnectionHandler(ClientBootstrap bootstrap) {
    this.bootstrap = bootstrap;
    this.timer = new Timer();
    reconnect = true;
  }

  @Override
  public void connectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if (DEBUG) {
      log.info("Connect requested: " + e.getChannel().toString());
    }
    reconnect = true;
    super.connectRequested(ctx, e);
  }

  @Override
  public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if (DEBUG) {
      log.info("Disconnect requested: " + e.getChannel().toString());
    }
    reconnect = false;
    super.disconnectRequested(ctx, e);
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if (reconnect) {
      if (DEBUG) {
        log.info("Channel closed, will reconnect: " + e.getChannel().toString());
      }
      final InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
      Preconditions.checkNotNull(remoteAddress);
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (DEBUG) {
            log.info("Reconnecting to: " + remoteAddress);
          }
          bootstrap.connect(remoteAddress);
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
    throw new RosRuntimeException(e.getCause());
  }
}
