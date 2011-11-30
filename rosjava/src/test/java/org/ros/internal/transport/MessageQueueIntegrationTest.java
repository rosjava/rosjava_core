/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except firstIncomingMessageQueue compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to firstIncomingMessageQueue writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.old_style.MessageDeserializer;
import org.ros.internal.message.old_style.MessageSerializer;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpClientConnection;
import org.ros.internal.transport.tcp.TcpClientConnectionManager;
import org.ros.internal.transport.tcp.TcpServerPipelineFactory;
import org.ros.message.Message;
import org.ros.message.std_msgs.String;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageQueueIntegrationTest {

  private static final Log log = LogFactory.getLog(MessageQueueIntegrationTest.class);

  private ExecutorService executorService;
  private TcpClientConnectionManager tcpClientConnectionManager;
  private CancellableLoop repeatingPublisher;
  private OutgoingMessageQueue<Message> outgoingMessageQueue;
  private IncomingMessageQueue<org.ros.message.std_msgs.String> firstIncomingMessageQueue;
  private IncomingMessageQueue<String> secondIncomingMessageQueue;
  private org.ros.message.std_msgs.String expectedMessage;

  private class ServerHandler extends SimpleChannelHandler {
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      log.info("Channel connected: " + e.getChannel().toString());
      Channel channel = e.getChannel();
      outgoingMessageQueue.addChannel(channel);
      super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
      log.info("Channel disconnected: " + e.getChannel().toString());
      super.channelDisconnected(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.info("Channel exception: " + e.getChannel().toString());
      e.getChannel().close();
      throw new RuntimeException(e.getCause());
    }
  }

  @Before
  public void setup() {
    executorService = Executors.newCachedThreadPool();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
    expectedMessage = new org.ros.message.std_msgs.String();
    expectedMessage.data = "Would you like to play a game?";
    outgoingMessageQueue =
        new OutgoingMessageQueue<Message>(new MessageSerializer<Message>(), executorService);

    repeatingPublisher = new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        outgoingMessageQueue.put(expectedMessage);
        Thread.sleep(100);
      }
    };
    executorService.execute(repeatingPublisher);

    firstIncomingMessageQueue =
        new IncomingMessageQueue<org.ros.message.std_msgs.String>(
            new MessageDeserializer<org.ros.message.std_msgs.String>(
                org.ros.message.std_msgs.String.class));
    secondIncomingMessageQueue =
        new IncomingMessageQueue<org.ros.message.std_msgs.String>(
            new MessageDeserializer<org.ros.message.std_msgs.String>(
                org.ros.message.std_msgs.String.class));
  }

  @After
  public void tearDown() {
    outgoingMessageQueue.shutdown();
    executorService.shutdown();
  }

  private TcpClientConnection connectIncomingMessageQueue(
      final IncomingMessageQueue<org.ros.message.std_msgs.String> incomingMessageQueue,
      Channel serverChannel) throws InterruptedException {
    return tcpClientConnectionManager.connect("Foo", serverChannel.getLocalAddress(),
        incomingMessageQueue.createChannelHandler(), "MessageHandler");
  }

  @Test
  public void testSendAndReceiveMessage() throws InterruptedException {
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    connectIncomingMessageQueue(secondIncomingMessageQueue, serverChannel);
    expectMessage();
  }

  @Test
  public void testSendAndReceiveLatchedMessage() throws InterruptedException {
    // Setting latched mode and writing a message should cause any future
    // IncomingMessageQueue to receive that message.
    outgoingMessageQueue.setLatchMode(true);
    repeatingPublisher.cancel();
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    connectIncomingMessageQueue(secondIncomingMessageQueue, serverChannel);
    expectMessage();
  }

  @Test
  public void testSendAfterIncomingQueueShutdown() throws InterruptedException {
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    tcpClientConnectionManager.shutdown();
    outgoingMessageQueue.put(expectedMessage);
  }

  @Test
  public void testSendAfterServerChannelClosed() throws InterruptedException {
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    serverChannel.close().await();
    outgoingMessageQueue.put(expectedMessage);
  }

  @Test
  public void testSendAfterOutgoingQueueShutdown() throws InterruptedException {
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    outgoingMessageQueue.shutdown();
    outgoingMessageQueue.put(expectedMessage);
  }

  private Channel buildServerChannel() {
    TopicManager topicManager = new TopicManager();
    ServiceManager serviceManager = new ServiceManager();
    NioServerSocketChannelFactory channelFactory =
        new NioServerSocketChannelFactory(executorService, executorService);
    ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOption("child.bufferFactory",
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    bootstrap.setOption("child.keepAlive", true);
    ChannelGroup serverChannelGroup = new DefaultChannelGroup();
    TcpServerPipelineFactory serverPipelineFactory =
        new TcpServerPipelineFactory(serverChannelGroup, topicManager, serviceManager) {
          @Override
          public ChannelPipeline getPipeline() {
            ChannelPipeline pipeline = super.getPipeline();
            // We're not interested firstIncomingMessageQueue testing the
            // handshake
            // here. Removing it
            // means connections are established immediately.
            pipeline.remove(TcpServerPipelineFactory.HANDSHAKE_HANDLER);
            pipeline.addLast("ServerHandler", new ServerHandler());
            return pipeline;
          }
        };
    bootstrap.setPipelineFactory(serverPipelineFactory);
    Channel serverChannel = bootstrap.bind(new InetSocketAddress(0));
    return serverChannel;
  }

  private void expectMessage() throws InterruptedException {
    assertEquals(firstIncomingMessageQueue.take(), expectedMessage);
    assertEquals(secondIncomingMessageQueue.take(), expectedMessage);
  }

  /**
   * @throws InterruptedException
   */
  @Test
  public void testReconnect() throws InterruptedException {
    Channel serverChannel = buildServerChannel();
    connectIncomingMessageQueue(firstIncomingMessageQueue, serverChannel);
    connectIncomingMessageQueue(secondIncomingMessageQueue, serverChannel);
    expectMessage();

    // Disconnect the outgoing queue's incoming connections.
    ChannelGroupFuture future = outgoingMessageQueue.getChannelGroup().close();
    assertTrue(future.await(1, TimeUnit.SECONDS));
    assertTrue(future.isCompleteSuccess());
    expectMessage();

    // Disconnect the outgoing queue's incoming connections again to see that
    // retries work more than once.
    future = outgoingMessageQueue.getChannelGroup().close();
    assertTrue(future.await(1, TimeUnit.SECONDS));
    assertTrue(future.isCompleteSuccess());
    expectMessage();

    // Shutdown the TcpClientConnectionManager to check that we will not
    // reconnect.
    tcpClientConnectionManager.shutdown();
    future = outgoingMessageQueue.getChannelGroup().close();
    assertTrue(future.await(1, TimeUnit.SECONDS));
    assertTrue(future.isCompleteSuccess());
    assertEquals(null, firstIncomingMessageQueue.poll(3, TimeUnit.SECONDS));
    assertEquals(null, secondIncomingMessageQueue.poll(3, TimeUnit.SECONDS));
  }
}
