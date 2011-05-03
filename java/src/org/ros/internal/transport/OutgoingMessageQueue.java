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
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.ros.MessageSerializer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OutgoingMessageQueue<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(OutgoingMessageQueue.class);
  
  private static final int MESSAGE_BUFFER_CAPACITY = 8192;

  private final ChannelGroup channelGroup;
  private final CircularBlockingQueue<MessageType> messages;
  private final MessageSendingThread thread;
  private final MessageSerializer<MessageType> serializer;

  private final class MessageSendingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          ChannelBuffer buffer =
              ChannelBuffers.wrappedBuffer(serializer.serialize(messages.take()));
          channelGroup.write(buffer);
          if (DEBUG) {
            // TODO(damonkohler): Add a utility method for a better ChannelBuffer.toString() method.
            log.info("Wrote " + buffer.toString());
          }
        }
      } catch (InterruptedException e) {
        // Cancelable
        if (DEBUG) {
          log.info("Interrupted.");
        }
      }
    }

    public ChannelGroupFuture cancel() {
      interrupt();
      return channelGroup.close();
    }
  }

  public OutgoingMessageQueue(MessageSerializer<MessageType> serializer) {
    this.serializer = serializer;
    channelGroup = new DefaultChannelGroup();
    messages = new CircularBlockingQueue<MessageType>(MESSAGE_BUFFER_CAPACITY);
    thread = new MessageSendingThread();
  }

  public void put(MessageType message) {
    messages.put(message);
  }

  public void shutdown() throws InterruptedException {
    thread.cancel().await();
  }

  public void start() {
    thread.start();
  }

  /**
   * @param channel added to this {@link OutgoingMessageQueue}'s
   *        {@link ChannelGroup}
   */
  public void addChannel(Channel channel) {
    channelGroup.add(channel);
  }

}
