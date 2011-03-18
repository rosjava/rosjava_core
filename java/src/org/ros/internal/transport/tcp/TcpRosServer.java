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

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.topic.TopicManager;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpRosServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpRosServer.class);

  private final InetSocketAddress bindAddress;
  private final ChannelGroup channelGroup;
  private final ChannelFactory channelFactory;
  private final ServerBootstrap bootstrap;

  private Channel channel;

  public TcpRosServer(InetSocketAddress bindAddress, TopicManager topicManager,
      ServiceManager serviceManager) {
    this.bindAddress = bindAddress;
    channelGroup = new DefaultChannelGroup();
    channelFactory =
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOption("child.bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    TcpServerPipelineFactory pipelineFactory =
        new TcpServerPipelineFactory(channelGroup, topicManager, serviceManager);
    bootstrap.setPipelineFactory(pipelineFactory);
  }

  public void start() {
    channel = bootstrap.bind(bindAddress);
    if (DEBUG) {
      log.info("Bound to: " + getAddress());
    }
  }

  public void shutdown() {
    if (DEBUG) {
      log.info("Shutting down: " + getAddress());
    }
    ChannelGroupFuture future = channelGroup.close();
    future.awaitUninterruptibly();
    channelFactory.releaseExternalResources();
    bootstrap.releaseExternalResources();
  }

  public InetSocketAddress getAddress() {
    Preconditions
        .checkNotNull(channel, "Calling getAddress() is only valid after calling start().");
    return (InetSocketAddress) channel.getLocalAddress();
  }

}
