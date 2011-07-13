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

/**
 * Publishes messages of a given type on a given ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 *          The message type to use. The {@link Publisher} may only publish
 *          messages of this type.
 */
public interface Publisher<MessageType> extends Topic {

  /**
   * @see http://www.ros.org/wiki/roscpp/Overview/Publishers%20and%20Subscribers#Publisher_Options
   * @param enabled
   *          if {@code true}, all messages published to this topic from the
   *          {@link Publisher}'s {@link Node} will be latched
   */
  void setLatchMode(boolean enabled);

  /**
   * Publish a message. This message will be available on the topic that this
   * {@link Publisher} has been associated with.
   * 
   * @param message
   *          the message to publish
   */
  void publish(MessageType message);

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
   * This counts the number {@link Subscriber} registered. If a
   * {@link Subscriber} does not shutdown properly it will not be unregistered
   * and thus will contribute to this count.
   * 
   * @return the number of {@link Subscriber}s currently connected to the
   *         {@link Publisher}
   */
  int getNumberOfSubscribers();

  /**
   * Cancels the publication and unregisters the {@link Publisher}.
   */
  void shutdown();

}