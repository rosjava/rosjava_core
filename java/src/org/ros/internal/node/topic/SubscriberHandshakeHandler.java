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
import org.jboss.netty.channel.ExceptionEvent;
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
  private final IncomingMessageQueue<MessageType> in;

  public SubscriberHandshakeHandler(ImmutableMap<String, String> header,
      IncomingMessageQueue<MessageType> in) {
    this.header = header;
    this.in = in;
  }

  private void handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = ConnectionHeader.decode(buffer);
    if (DEBUG) {
      log.info("Outgoing handshake header: " + header);
      log.info("Incoming handshake header: " + incomingHeader);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
    handshake(incomingBuffer);
    Channel channel = e.getChannel();
    ChannelPipeline pipeline = channel.getPipeline();
    pipeline.remove(this);
    pipeline.addLast("MessageHandler", in.createChannelHandler());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    throw new RuntimeException(e.getCause());
  }

}
