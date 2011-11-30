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

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.ros.exception.RosRuntimeException;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnectionManager {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpClientConnectionManager.class);

  private static final int CONNECTION_TIMEOUT_MILLIS = 5000;

  private final ChannelFactory channelFactory;
  private final ChannelGroup channelGroup;
  private final Collection<TcpClientConnection> tcpClientConnections;

  public TcpClientConnectionManager(ExecutorService executorService) {
    channelFactory = new NioClientSocketChannelFactory(executorService, executorService);
    channelGroup = new DefaultChannelGroup();
    tcpClientConnections = Lists.newArrayList();
  }

  public TcpClientConnection connect(SocketAddress address, final ChannelHandler handler,
      final String handlerName) {
    ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    bootstrap.setOption("connectionTimeoutMillis", CONNECTION_TIMEOUT_MILLIS);
    bootstrap.setOption("keepAlive", true);
    TcpClientConnection tcpClientConnection = newTcpClientConnection(address, bootstrap);
    TcpClientPipelineFactory factory =
        new TcpClientPipelineFactory(channelGroup, tcpClientConnection) {
          @Override
          public ChannelPipeline getPipeline() {
            ChannelPipeline pipeline = super.getPipeline();
            pipeline.addLast(handlerName, handler);
            return pipeline;
          }
        };
    bootstrap.setPipelineFactory(factory);
    ChannelFuture future = bootstrap.connect(address).awaitUninterruptibly();
    if (future.isSuccess()) {
      Channel channel = future.getChannel();
      tcpClientConnection.setChannel(channel);
      if (DEBUG) {
        log.info("Connected to: " + address);
      }
    } else {
      throw new RosRuntimeException("Connection failed: " + address, future.getCause());
    }
    return tcpClientConnection;
  }

  private TcpClientConnection newTcpClientConnection(SocketAddress address,
      ClientBootstrap bootstrap) {
    TcpClientConnection tcpClientConnection = new TcpClientConnection(bootstrap, address);
    tcpClientConnections.add(tcpClientConnection);
    return tcpClientConnection;
  }

  /**
   * Sets all {@link TcpClientConnection}s as non-persistent and closes all open
   * {@link Channel}s.
   */
  public void shutdown() {
    for (TcpClientConnection tcpClientConnection : tcpClientConnections) {
      tcpClientConnection.setPersistent(false);
      tcpClientConnection.setChannel(null);
    }
    channelGroup.close().awaitUninterruptibly();
    tcpClientConnections.clear();
    // Not calling channelFactory.releaseExternalResources() or
    // bootstrap.releaseExternalResources() since only external resources are
    // the ExecutorService and control of that must remain with the overall
    // application.
  }
}
