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

package org.ros.internal.transport.queue;

import org.ros.concurrent.CircularBlockingQueue;
import org.ros.internal.transport.tcp.NamedChannelHandler;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageListener;

import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class IncomingMessageQueue<T> {

  /**
   * The maximum number of incoming messages that will be queued by default.
   * <p>
   * This limit applies to dispatching {@link LazyMessage}s as they arrive over
   * the network. It is independent of {@link MessageDispatcher} queue
   * capacities specified by
   * {@link IncomingMessageQueue#addListener(MessageListener, int)} which are
   * consumed by user provided {@link MessageListener}s.
   */
  private static final int DEFAULT_LAZY_MESSAGES_LIMIT = 32;

  private final CircularBlockingQueue<LazyMessage<T>> lazyMessages;
  private final MessageReceiver<T> messageReceiver;
  private final MessageDispatcher<T> messageDispatcher;

  public IncomingMessageQueue(MessageDeserializer<T> deserializer, ExecutorService executorService) {
    lazyMessages = new CircularBlockingQueue<LazyMessage<T>>();
    lazyMessages.setLimit(DEFAULT_LAZY_MESSAGES_LIMIT);
    messageReceiver = new MessageReceiver<T>(lazyMessages, deserializer);
    messageDispatcher = new MessageDispatcher<T>(lazyMessages, executorService);
    executorService.execute(messageDispatcher);
  }

  /**
   * @see MessageDispatcher#setLatchMode(boolean)
   */
  public void setLatchMode(boolean enabled) {
    messageDispatcher.setLatchMode(enabled);
  }

  /**
   * @see MessageDispatcher#getLatchMode()
   */
  public boolean getLatchMode() {
    return messageDispatcher.getLatchMode();
  }

  /**
   * @see CircularBlockingQueue#setLimit(int)
   */
  public void setLimit(int limit) {
    lazyMessages.setLimit(limit);
  }

  /**
   * @see MessageDispatcher#addListener(MessageListener, int)
   */
  public void addListener(final MessageListener<T> messageListener, int queueCapacity) {
    messageDispatcher.addListener(messageListener, queueCapacity);
  }

  public void shutdown() {
    messageDispatcher.cancel();
  }

  /**
   * @return a {@link NamedChannelHandler} that will receive messages and add
   *         them to the queue
   */
  public NamedChannelHandler getMessageReceiver() {
    return messageReceiver;
  }
}
