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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.IncomingMessageQueue;
import org.ros.internal.transport.tcp.AbstractNamedChannelHandler;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * Performs a handshake with the connected {@link Publisher} and connects the
 * {@link Publisher} to the {@link IncomingMessageQueue} on success.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the {@link Subscriber} may only subscribe to messages of this type
 */
class SubscriberHandshakeHandler<T> extends AbstractNamedChannelHandler {

  private static final Log log = LogFactory.getLog(SubscriberHandshakeHandler.class);

  private final IncomingMessageQueue<T> incomingMessageQueue;
  private final SubscriberHandshake subscriberHandshake;

  public SubscriberHandshakeHandler(ConnectionHeader outgoingConnectionHeader,
      IncomingMessageQueue<T> incomingMessageQueue) {
    subscriberHandshake = new SubscriberHandshake(outgoingConnectionHeader);
    this.incomingMessageQueue = incomingMessageQueue;
  }
  
  @Override
  public String getName() {
    return "SubscriberHandshakeHandler";
  }
  
  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    e.getChannel().write(subscriberHandshake.getOutgoingConnectionHeader().encode());
    super.channelConnected(ctx, e);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    super.messageReceived(ctx, e);
    ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
    ConnectionHeader incomingConnectionHeader = ConnectionHeader.decode(buffer);
    if (subscriberHandshake.handshake(incomingConnectionHeader)) {
      ChannelPipeline pipeline = e.getChannel().getPipeline();
      pipeline.remove(this);
      pipeline.addLast("MessageHandler", incomingMessageQueue.newNamedChannelHandler());
      String latching = incomingConnectionHeader.getField(ConnectionHeaderFields.LATCHING);
      if (latching != null && latching.equals("1")) {
        incomingMessageQueue.setLatchMode(true);
      }
    } else {
      // TODO(damonkohler): Call listener that connection failed.
      log.error("Subscriber handshake failed: " + subscriberHandshake.getErrorMessage());
      e.getChannel().close();
    }
  }
}
