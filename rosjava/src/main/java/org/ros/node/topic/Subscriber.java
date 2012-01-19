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

package org.ros.node.topic;

import org.ros.internal.node.topic.Topic;
import org.ros.message.MessageListener;

import java.util.concurrent.TimeUnit;

/**
 * Subscribes to messages of a given type on a given ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the {@link Subscriber} may only subscribe to messages of this type
 */
public interface Subscriber<T> extends Topic {

  /**
   * The message type given when a {@link Subscriber} chooses not to commit to a
   * specific message type.
   */
  public static final String TOPIC_MESSAGE_TYPE_WILDCARD = "*";

  /**
   * @param listener
   *          this {@link MessageListener} will be called for every new message
   *          received
   */
  void addMessageListener(MessageListener<T> listener);

  /**
   * @param listener
   *          the {@link MessageListener} to remove
   */
  void removeMessageListener(MessageListener<T> listener);

  /**
   * Shuts down and unregisters the {@link Subscriber}. using the default
   * timeout Shutdown is delayed by at most the specified timeout to allow
   * {@link SubscriberListener#onShutdown(Subscriber)} callbacks to complete.
   * 
   * <p>
   * {@link SubscriberListener#onShutdown(Subscriber)} callbacks are executed in
   * separate threads.
   */
  void shutdown(long timeout, TimeUnit unit);

  /**
   * Shuts down and unregisters the {@link Subscriber} using the default timeout
   * for {@link SubscriberListener#onShutdown(Subscriber)} callbacks.
   * 
   * <p>
   * {@link SubscriberListener#onShutdown(Subscriber)} callbacks are executed in
   * separate threads.
   * 
   * @see Subscriber#shutdown(long, TimeUnit)
   */
  void shutdown();

  /**
   * Add a new lifecycle listener to the subscriber.
   * 
   * @param listener
   *          The listener to add.
   */
  void addSubscriberListener(SubscriberListener<T> listener);

  /**
   * Remove a lifecycle listener from the subscriber.
   * 
   * <p>
   * Nothing will happen if the given listener is not registered.
   * 
   * @param listener
   *          The listener to remove.
   */
  void removeSubscriberListener(SubscriberListener<T> listener);

  /**
   * @param limit
   *          the maximum number of incoming messages to queue (i.e. buffer)
   */
  void setQueueLimit(int limit);

  /**
   * @return the maximum number of incoming messages to queue (i.e. buffer)
   */
  int getQueueLimit();

  /**
   * @return {@code true} if the {@link Publisher} of this {@link Subscriber}'s
   *         topic is latched, {@code false} otherwise
   */
  boolean getLatchMode();
}
