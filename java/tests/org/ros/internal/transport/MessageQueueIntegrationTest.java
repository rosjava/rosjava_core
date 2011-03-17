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

import org.ros.internal.topic.TopicManager;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.transport.tcp.TcpServer;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageQueueIntegrationTest {

  private OutgoingMessageQueue out;

  private class ServerHandler extends SimpleChannelHandler {
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
      Channel channel = e.getChannel();
      out.addChannel(channel);
    }
  }

  @Before
  public void setup() {
    out = new OutgoingMessageQueue();
    out.start();
  }

  @Test
  public void testSendAndReceiveMessage() throws InterruptedException {
    SimplePipelineFactory serverPipelineFactory = new SimplePipelineFactory();
    serverPipelineFactory.getPipeline().addLast("Server Handler", new ServerHandler());
    TopicManager topicManager = new TopicManager();
    TcpServer server = new TcpServer(serverPipelineFactory, topicManager);
    server.start(new InetSocketAddress(0));

    // TODO(damonkohler): Test connecting multiple incoming queues to single
    // outgoing queue and visa versa.
    IncomingMessageQueue<org.ros.message.std.String> in = new IncomingMessageQueue<org.ros.message.std.String>(
        org.ros.message.std.String.class);

    ChannelFactory channelFactory = new NioClientSocketChannelFactory(
        Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    SimplePipelineFactory clientPipelineFactory = new SimplePipelineFactory();
    clientPipelineFactory.getPipeline().addLast("Client Handler", in.createChannelHandler());
    bootstrap.setPipelineFactory(clientPipelineFactory);
    bootstrap.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    bootstrap.connect(server.getAddress()).awaitUninterruptibly();

    // TODO(damonkohler): There is a race here that makes this test flaky. Once
    // the IncomingMessageQueue is ported to Netty, we can wait for a successful
    // connection before putting the message on the outgoing queue. That should
    // fix the problem.
    org.ros.message.std.String hello = new org.ros.message.std.String();
    hello.data = "Would you like to play a game?";
    out.put(hello);
    assertEquals(in.take(), hello);
  }
}
