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

import static org.junit.Assert.assertEquals;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpClientPipelineFactory;
import org.ros.internal.transport.tcp.TcpServerPipelineFactory;
import org.ros.message.Message;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageImplQueueIntegrationTest {

  private OutgoingMessageQueue<Message> out;

  private class ServerHandler extends SimpleChannelHandler {
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
      Channel channel = e.getChannel();
      out.addChannel(channel);
    }
  }

  @Before
  public void setup() {
    out = new OutgoingMessageQueue<Message>(new MessageSerializer<Message>());
    out.start();
  }

  @Test
  public void testSendAndReceiveMessage() throws InterruptedException {
    ChannelGroup serverChannelGroup = new DefaultChannelGroup();
    TopicManager topicManager = new TopicManager();
    ServiceManager serviceManager = new ServiceManager();

    NioServerSocketChannelFactory channelFactory =
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOption("child.bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    TcpServerPipelineFactory serverPipelineFactory =
        new TcpServerPipelineFactory(serverChannelGroup, topicManager, serviceManager) {
          @Override
          public ChannelPipeline getPipeline() {
            ChannelPipeline pipeline = super.getPipeline();
            pipeline.remove(TcpServerPipelineFactory.HANDSHAKE_HANDLER);
            pipeline.addLast("ServerHandler", new ServerHandler());
            return pipeline;
          }
        };
    bootstrap.setPipelineFactory(serverPipelineFactory);
    Channel serverChannel = bootstrap.bind(new InetSocketAddress(0));

    // TODO(damonkohler): Test connecting multiple incoming queues to single
    // outgoing queue and visa versa.
    final IncomingMessageQueue<org.ros.message.std_msgs.String> in =
        new IncomingMessageQueue<org.ros.message.std_msgs.String>(
            new MessageDeserializer<org.ros.message.std_msgs.String>(
                org.ros.message.std_msgs.String.class));

    ChannelFactory clientChannelFactory =
        new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    ClientBootstrap clientBootstrap = new ClientBootstrap(clientChannelFactory);
    clientBootstrap.setOption("bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    TcpClientPipelineFactory clientPipelineFactory = new TcpClientPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = super.getPipeline();
        pipeline.addLast("ClientHandler", in.createChannelHandler());
        return pipeline;
      }
    };
    clientBootstrap.setPipelineFactory(clientPipelineFactory);
    clientBootstrap.connect(serverChannel.getLocalAddress()).awaitUninterruptibly();

    // TODO(damonkohler): Ugly hack because we can't yet determine when the
    // connection has been established.
    Thread.sleep(100);

    org.ros.message.std_msgs.String hello = new org.ros.message.std_msgs.String();
    hello.data = "Would you like to play a game?";
    out.put(hello);
    assertEquals(in.take(), hello);
  }
}
