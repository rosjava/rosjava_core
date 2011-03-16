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

package org.ros.internal.topic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.NettyConnectionHeader;
import org.ros.internal.transport.OutgoingMessageQueue;
import org.ros.internal.transport.SimplePipelineFactory;
import org.ros.internal.transport.tcp.TcpServer;
import org.ros.message.Message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Publisher extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final OutgoingMessageQueue out;
  private final List<SubscriberIdentifier> subscribers;
  private final TcpServer server;

  class HandshakeHandler extends SimpleChannelHandler {

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
      Channel channel = e.getChannel();
      out.addChannel(channel);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
      ChannelBuffer outgoingBuffer = handshake(incomingBuffer);
      Channel channel = ctx.getChannel();
      channel.write(outgoingBuffer);
      // TODO(damonkohler): Replace this handler with a discard handler in the
      // pipeline.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
      log.error("Incomming connection failed.", e.getCause());
      e.getChannel().close();
    }
  }

  public Publisher(TopicDefinition description) {
    super(description);
    out = new OutgoingMessageQueue();
    subscribers = Lists.newArrayList();
    // TODO(kwc): We only need one TCPROS server for the entire node.
    SimplePipelineFactory factory = new SimplePipelineFactory();
    factory.getPipeline().addLast("HandshakeHandler", new HandshakeHandler());
    server = new TcpServer(factory);
  }

  public void start(SocketAddress address) {
    server.start(address);
    out.start();
  }

  public void shutdown() {
    try {
      out.shutdown();
    } catch (InterruptedException e) {
      log.error("Failed to shutdown outgoing message queue.", e);
    }
    server.shutdown();
  }

  public PublisherIdentifier toPublisherIdentifier(SlaveIdentifier description) {
    return new PublisherIdentifier(description, getTopicDefinition());
  }

  public InetSocketAddress getAddress() {
    return server.getAddress();
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
  public void publish(Message message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    out.put(message);
  }

  @VisibleForTesting
  ChannelBuffer handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = NettyConnectionHeader.decode(buffer);
    Map<String, String> header = getTopicDefinitionHeader();
    if (DEBUG) {
      log.info("Subscriber handshake header: " + incomingHeader);
      log.info("Publisher handshake header: " + header);
    }
    // TODO(damonkohler): Return error to the subscriber over the wire?
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    SubscriberIdentifier subscriber = SubscriberIdentifier.createFromHeader(incomingHeader);
    subscribers.add(subscriber);
    return NettyConnectionHeader.encode(header);
  }

}
