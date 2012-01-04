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

import java.util.concurrent.TimeUnit;

/**
 * Publishes messages of a given type on a given ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the {@link Publisher} may only publish messages of this type
 */
public interface Publisher<T> extends Topic {

  /**
   * @see http://www.ros.org/wiki/roscpp/Overview/Publishers%20and%20Subscribers#Publisher_Options
   * @param enabled
   *          {@code true} if published messages should be latched,
   *          {@code false} otherwise
   */
  void setLatchMode(boolean enabled);

  /**
   * @see http://www.ros.org/wiki/roscpp/Overview/Publishers%20and%20Subscribers#Publisher_Options
   * @return {@code true} if published messages will be latched, {@code false}
   *         otherwise
   */
  boolean getLatchMode();

  /**
   * Publishes a message. This message will be available on the topic that this
   * {@link Publisher} has been associated with.
   * 
   * @param message
   *          the message to publish
   */
  void publish(T message);

  /**
   * @return {@code true} if {@code getNumberOfSubscribers() > 0}, {@code false}
   *         otherwise
   */
  boolean hasSubscribers();

  /**
   * Get the number of {@link Subscriber}s currently connected to the
   * {@link Publisher}.
   * 
   * <p>
   * This counts the number of {@link Subscriber} registered. If a
   * {@link Subscriber} does not shutdown properly it will not be unregistered
   * and thus will contribute to this count.
   * 
   * @return the number of {@link Subscriber}s currently connected to the
   *         {@link Publisher}
   */
  int getNumberOfSubscribers();

  /**
   * Shuts down and unregisters the {@link Publisher}. Shutdown is delayed by at
   * most the specified timeout to allow
   * {@link PublisherListener#onShutdown(Publisher)} callbacks to complete.
   * 
   * <p>
   * {@link PublisherListener#onShutdown(Publisher)} callbacks are executed in
   * separate threads.
   */
  void shutdown(long timeout, TimeUnit unit);

  /**
   * Shuts down and unregisters the {@link Publisher} using the default timeout
   * for {@link PublisherListener#onShutdown(Publisher)} callbacks.
   * 
   * <p>
   * {@link PublisherListener#onShutdown(Publisher)} callbacks are executed in
   * separate threads.
   * 
   * @see Publisher#shutdown(long, TimeUnit)
   */
  void shutdown();

  /**
   * Add a new lifecycle listener to the {@link Publisher}.
   * 
   * @param listener
   *          the {@link PublisherListener} to add
   */
  void addListener(PublisherListener<T> listener);

  /**
   * Remove a lifecycle listener from the {@link Publisher}.
   * 
   * <p>
   * Nothing will happen if the given listener is not registered.
   * 
   * @param listener
   *          the {@link PublisherListener} to remove
   */
  void removeListener(PublisherListener<T> listener);

  /**
   * @param limit
   *          the maximum number of messages to queue (i.e. buffer) for sending
   */
  void setQueueLimit(int limit);

  /**
   * @return the maximum number of messages to queue (i.e. buffer) for sending
   */
  int getQueueLimit();
}