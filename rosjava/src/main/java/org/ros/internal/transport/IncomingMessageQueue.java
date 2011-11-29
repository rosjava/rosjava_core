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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.message.MessageDeserializer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class IncomingMessageQueue<MessageType> {
  
  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(IncomingMessageQueue.class);
  
  private static final int MESSAGE_BUFFER_CAPACITY = 8192;

  private final CircularBlockingQueue<MessageType> messages;
  private final MessageDeserializer<MessageType> deserializer;

  private final class MessageHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
      MessageType message = deserializer.deserialize(buffer.toByteBuffer());
      messages.put(message);
      if (DEBUG) {
        log.info("Received message: " + message);
      }
      super.messageReceived(ctx, e);
    }
  }

  public IncomingMessageQueue(MessageDeserializer<MessageType> deserializer) {
    this.deserializer = deserializer;
    messages = new CircularBlockingQueue<MessageType>(MESSAGE_BUFFER_CAPACITY);
  }

  /**
   * @see CircularBlockingQueue#setLimit(int)
   */
  public void setLimit(int limit) {
    messages.setLimit(limit);
  }

  /**
   * @see CircularBlockingQueue#getLimit()
   */
  public int getLimit() {
    return messages.getLimit();
  }

  public MessageType take() throws InterruptedException {
    return messages.take();
  }

  public ChannelHandler createChannelHandler() {
    return new MessageHandler();
  }
}
