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

import org.ros.concurrent.CircularBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RosRuntimeException;
import org.ros.message.MessageSerializer;

import java.nio.ByteOrder;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OutgoingMessageQueue<T> {

  private static final Log log = LogFactory.getLog(OutgoingMessageQueue.class);

  private static final int MESSAGE_QUEUE_CAPACITY = 8192;
  private static final int ESTIMATED_MESSAGE_SIZE = 256;

  private final MessageSerializer<T> serializer;
  private final CircularBlockingQueue<T> queue;
  private final ChannelGroup channelGroup;
  private final Writer writer;
  private final ChannelBuffer buffer;

  private boolean latchMode;
  private T latchedMessage;

  private final class Writer extends CancellableLoop {
    @Override
    public void loop() throws InterruptedException {
      T message = queue.take();
      buffer.clear();
      serializer.serialize(message, buffer);
      channelGroup.write(buffer);
    }
  }

  public OutgoingMessageQueue(MessageSerializer<T> serializer,
      ScheduledExecutorService executorService) {
    this.serializer = serializer;
    queue = new CircularBlockingQueue<T>(MESSAGE_QUEUE_CAPACITY);
    channelGroup = new DefaultChannelGroup();
    writer = new Writer();
    buffer = ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, ESTIMATED_MESSAGE_SIZE);
    latchMode = false;
    executorService.execute(writer);
  }

  public void setLatchMode(boolean enabled) {
    latchMode = enabled;
  }

  public boolean getLatchMode() {
    return latchMode;
  }

  /**
   * @param message
   *          the message to add to the queue
   */
  public void put(T message) {
    try {
      queue.put(message);
    } catch (InterruptedException e) {
      throw new RosRuntimeException(e);
    }
    setLatchedMessage(message);
  }

  private synchronized void setLatchedMessage(T message) {
    latchedMessage = message;
  }

  /**
   * Stop writing messages and close all outgoing connections.
   */
  public void shutdown() {
    writer.cancel();
    channelGroup.close().awaitUninterruptibly();
  }

  /**
   * @see CircularBlockingQueue#setLimit(int)
   */
  public void setLimit(int limit) {
    queue.setLimit(limit);
  }

  /**
   * @see CircularBlockingQueue#getLimit
   */
  public int getLimit() {
    return queue.getLimit();
  }

  /**
   * @param channel
   *          added to this {@link OutgoingMessageQueue}'s {@link ChannelGroup}
   */
  public void addChannel(Channel channel) {
    if (!writer.isRunning()) {
      log.warn("Failed to add channel. Cannot add channels after shutdown.");
      return;
    }
    if (latchMode && latchedMessage != null) {
      writeLatchedMessage(channel);
    }
    channelGroup.add(channel);
  }

  private synchronized void writeLatchedMessage(Channel channel) {
    ChannelBuffer latchedBuffer =
        ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, ESTIMATED_MESSAGE_SIZE);
    serializer.serialize(latchedMessage, latchedBuffer);
    channel.write(latchedBuffer);
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
