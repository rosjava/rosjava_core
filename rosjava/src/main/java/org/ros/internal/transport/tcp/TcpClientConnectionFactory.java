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
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.ros.exception.RosRuntimeException;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnectionFactory {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpClientConnectionFactory.class);

  public static final String RETRYING_CONNECTION_HANDLER = "RetryingConnectionHandler";

  private static final int CONNECTION_TIMEOUT_MILLIS = 5000;

  private final ChannelFactory channelFactory;
  private final ChannelGroup channelGroup;
  private final ChannelBufferFactory channelBufferFactory;
  private final String channelHandlerName;
  private final ChannelHandler channelHandler;

  public TcpClientConnectionFactory(String channelHandlerName, ChannelHandler channelHandler,
      ChannelGroup channelGroup, ScheduledExecutorService scheduledExecutorService) {
    this.channelHandler = channelHandler;
    this.channelHandlerName = channelHandlerName;
    this.channelGroup = channelGroup;
    channelFactory =
        new NioClientSocketChannelFactory(scheduledExecutorService, scheduledExecutorService);
    channelBufferFactory = new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);
  }

  public TcpClientConnection connect(String connectionName, SocketAddress socketAddress) {
    ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setOption("bufferFactory", channelBufferFactory);
    bootstrap.setOption("connectionTimeoutMillis", CONNECTION_TIMEOUT_MILLIS);
    bootstrap.setOption("keepAlive", true);
    final TcpClientConnection tcpClientConnection =
        new TcpClientConnection(connectionName, bootstrap, socketAddress);
    TcpClientPipelineFactory tcpClientPipelineFactory = new TcpClientPipelineFactory(channelGroup) {
      @Override
      public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = super.getPipeline();
        pipeline.addAfter(CONNECTION_TRACKING_HANDLER, RETRYING_CONNECTION_HANDLER,
            tcpClientConnection.getRetryingConnectionHandler());
        pipeline.addLast(channelHandlerName, channelHandler);
        return pipeline;
      }
    };
    bootstrap.setPipelineFactory(tcpClientPipelineFactory);
    ChannelFuture future = bootstrap.connect(socketAddress).awaitUninterruptibly();
    if (future.isSuccess()) {
      Channel channel = future.getChannel();
      tcpClientConnection.setChannel(channel);
      if (DEBUG) {
        log.info("Connected to: " + socketAddress);
      }
    } else {
      // We expect the first connection to succeed. If not, fail fast.
      throw new RosRuntimeException("Connection exception: " + socketAddress, future.getCause());
    }
    return tcpClientConnection;
  }
}