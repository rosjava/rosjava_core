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

import static org.jboss.netty.channel.Channels.pipeline;

import com.google.common.base.Preconditions;

import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.topic.TopicManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpServer.class);

  private final ChannelGroup channelGroup;
  private final ChannelFactory channelFactory;
  private final ServerBootstrap bootstrap;

  private Channel channel;

  private final class ConnectionTrackingHandler extends SimpleChannelHandler {
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
      channelGroup.add(e.getChannel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      e.getChannel().close();
      throw new RuntimeException(e.getCause());
    }
  }

  private class TcpRosPipelineFactory implements ChannelPipelineFactory {

    private static final String LENGTH_FIELD_BASED_FRAME_DECODER = "LengthFieldBasedFrameDecoder";
    private static final String LENGTH_FIELD_PREPENDER = "LengthFieldPrepender";

    private final TopicManager topicManager;
    private final ServiceManager serviceManager;

    public TcpRosPipelineFactory(TopicManager topicManager, ServiceManager serviceManager) {
      this.topicManager = topicManager;
      this.serviceManager = serviceManager;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
      ChannelPipeline pipeline = pipeline();
      pipeline.addLast(LENGTH_FIELD_PREPENDER, new LengthFieldPrepender(4));
      pipeline.addLast(LENGTH_FIELD_BASED_FRAME_DECODER, new LengthFieldBasedFrameDecoder(
          Integer.MAX_VALUE, 0, 4, 0, 4));
      pipeline.addLast("ConnectionTrackingHandler", new ConnectionTrackingHandler());
      pipeline.addLast("HandshakeHandler", new HandshakeHandler(topicManager, serviceManager));
      return pipeline;
    }

  }

  public TcpServer(TopicManager topicManager, ServiceManager serviceManager) {
    channelGroup = new DefaultChannelGroup();
    channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool());
    bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOption("child.bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    TcpRosPipelineFactory pipelineFactory = new TcpRosPipelineFactory(topicManager, serviceManager);
    bootstrap.setPipelineFactory(pipelineFactory);
  }

  public void start(SocketAddress address) {
    channel = bootstrap.bind(address);
    if (DEBUG) {
      log.info("TCP server bound to: " + getAddress());
    }
  }

  public void shutdown() {
    if (DEBUG) {
      log.info("TCP server shutting down." + getAddress());
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
