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

package org.ros.internal.node.topic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.IncomingMessageQueue;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class SubscriberHandshakeHandler<MessageType> extends SimpleChannelHandler {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(SubscriberHandshakeHandler.class);

  private final ImmutableMap<String, String> header;
  private final IncomingMessageQueue<MessageType> incomingMessageQueue;

  public SubscriberHandshakeHandler(ImmutableMap<String, String> header,
      IncomingMessageQueue<MessageType> incomingMessageQueue) {
    this.header = header;
    this.incomingMessageQueue = incomingMessageQueue;
  }

  private Map<String, String> handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = ConnectionHeader.decode(buffer);
    if (DEBUG) {
      log.info("Outgoing handshake header: " + header);
      log.info("Incoming handshake header: " + incomingHeader);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    return incomingHeader;
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    e.getChannel().write(ConnectionHeader.encode(header));
    super.channelConnected(ctx, e);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
    Map<String, String> handshakeHeader = handshake(incomingBuffer);
    Channel channel = e.getChannel();
    ChannelPipeline pipeline = channel.getPipeline();
    pipeline.remove(this);
    pipeline.addLast("MessageHandler", incomingMessageQueue.newChannelHandler());
    String latching = handshakeHeader.get(ConnectionHeaderFields.LATCHING);
    if (latching != null && latching.equals("1")) {
      incomingMessageQueue.setLatchMode(true);
    }
    super.messageReceived(ctx, e);
  }
}
