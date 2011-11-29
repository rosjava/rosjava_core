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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageSerializer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OutgoingMessageQueue<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(OutgoingMessageQueue.class);

  private static final int MESSAGE_BUFFER_CAPACITY = 8192;

  private final MessageSerializer<MessageType> serializer;
  private final CircularBlockingQueue<MessageType> messages;
  private final ChannelGroup channelGroup;
  private final MessageWriter messageWriter;

  private boolean latchMode;
  private MessageType latchedMessage;

  private final class MessageWriter extends CancellableLoop {
    @Override
    public void loop() throws InterruptedException {
      writeMessageToChannel(messages.take());
    }
  }

  public OutgoingMessageQueue(MessageSerializer<MessageType> serializer,
      ExecutorService executorService) {
    this.serializer = serializer;
    messages = new CircularBlockingQueue<MessageType>(MESSAGE_BUFFER_CAPACITY);
    channelGroup = new DefaultChannelGroup();
    messageWriter = new MessageWriter();
    latchMode = false;
    executorService.execute(messageWriter);
  }

  public void setLatchMode(boolean enabled) {
    latchMode = enabled;
  }

  private void writeMessageToChannel(MessageType message) {
    ByteBuffer serializedMessage = serializer.serialize(message);
    ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(serializedMessage);
    if (DEBUG) {
      // TODO(damonkohler): Add a utility method for a better
      // ChannelBuffer.toString() method.
      log.info("Writing message: " + message);
    }
    channelGroup.write(buffer);
  }

  /**
   * @param message
   *          the message to add to the queue
   */
  public void put(MessageType message) {
    messages.put(message);
    latchedMessage = message;
  }

  /**
   * Stop writing messages.
   */
  public void shutdown() {
    messageWriter.cancel();
    channelGroup.close().awaitUninterruptibly();
  }

  /**
   * @see {@link CircularBlockingQueue#setLimit(int)}
   */
  public void setLimit(int limit) {
    messages.setLimit(limit);
  }

  /**
   * @see {@link CircularBlockingQueue#getLimit()}
   */
  public int getLimit() {
    return messages.getLimit();
  }

  /**
   * @param channel
   *          added to this {@link OutgoingMessageQueue}'s {@link ChannelGroup}
   */
  public void addChannel(Channel channel) {
    Preconditions.checkState(messageWriter.isRunning());
    if (DEBUG) {
      log.info("Adding channel: " + channel);
    }
    channelGroup.add(channel);
    if (latchMode && latchedMessage != null) {
      if (DEBUG) {
        log.info("Writing latched message: " + latchedMessage);
      }
      writeMessageToChannel(latchedMessage);
    }
  }

  /**
   * @return the number of {@link Channel}s which have been added to this queue
   */
  public int getNumberOfChannels() {
    return channelGroup.size();
  }

  @VisibleForTesting
  ChannelGroup getChannelGroup() {
    return channelGroup;
  }
}
