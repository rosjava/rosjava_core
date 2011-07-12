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
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.TopicManager;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * The TCP server which is used for data communication between publishers and
 * subscribers or between a service and a service client.
 * 
 * <p>
 * This server is used after publishers, subscribers, services and service
 * clients have been told about each other by the master.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpRosServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpRosServer.class);

  private final BindAddress bindAddress;
  private final AdvertiseAddress advertiseAddress;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;

  private ChannelFactory channelFactory;
  private ServerBootstrap bootstrap;
  private Channel outgoingChannel;
  private ChannelGroup incomingChannelGroup;

  public TcpRosServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress,
      TopicManager topicManager, ServiceManager serviceManager) {
    this.bindAddress = bindAddress;
    this.advertiseAddress = advertiseAddress;
    this.topicManager = topicManager;
    this.serviceManager = serviceManager;
  }

  public void start() {
    Preconditions.checkState(outgoingChannel == null);
    channelFactory =
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOption("child.bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    incomingChannelGroup = new DefaultChannelGroup();
    bootstrap.setPipelineFactory(new TcpServerPipelineFactory(incomingChannelGroup, topicManager,
        serviceManager));

    outgoingChannel = bootstrap.bind(bindAddress.toInetSocketAddress());
    advertiseAddress.setPortCallable(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return ((InetSocketAddress) outgoingChannel.getLocalAddress()).getPort();
      }
    });
    if (DEBUG) {
      log.info("Bound to: " + bindAddress);
      log.info("Advertising: " + advertiseAddress);
    }
  }

  public void shutdown() {
    Preconditions.checkNotNull(outgoingChannel);
    if (DEBUG) {
      log.info("Shutting down: " + getAddress());
    }
    incomingChannelGroup.close().awaitUninterruptibly();
    outgoingChannel.close().awaitUninterruptibly();
    channelFactory.releaseExternalResources();
    bootstrap.releaseExternalResources();
    outgoingChannel = null;
  }

  /**
   * @return the advertise-able address of this {@link TcpRosServer}
   */
  public InetSocketAddress getAddress() {
    return advertiseAddress.toInetSocketAddress();
  }

  /**
   * @return the {@link AdvertiseAddress} of this {@link TcpRosServer}
   */
  public AdvertiseAddress getAdvertiseAddress() {
    return advertiseAddress;
  }

}
