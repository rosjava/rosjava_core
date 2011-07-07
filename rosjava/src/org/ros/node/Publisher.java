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

package org.ros.node;

import java.util.concurrent.TimeUnit;

/**
 * A handle for publishing messages of a particular type on a given topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @param <MessageType>
 *          The message type to template on. The publisher may only publish
 *          messages of this type.
 */
public interface Publisher<MessageType> {
 
  void setLatchMode(boolean enabled);

  /**
   * @param message
   *          The {@link Message} to publish. This message will be available on
   *          the topic that this {@link Publisher} has been associated with.
   */
  void publish(MessageType message);

  /**
   * Get the topic for the {@link Publisher}.
   * 
   * @return the fully-namespaced topic for the {@link Publisher}
   */
  String getTopicName();

  /**
   * Wait for the publisher to register with the master.
   * 
   * <p>
   * This call blocks.
   * 
   * @throws InterruptedException
   */
  void awaitRegistration() throws InterruptedException;

  /**
   * Wait for the publisher to register with the master.
   * 
   * @param timeout
   *          How long to wait for registration.
   * @param unit
   *          The units for how long to wait.
   * @return True if the publisher registered with the master, false otherwise.
   * 
   * @throws InterruptedException
   */
  boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException;
  
  /**
   * Does the publisher have any connected subscribers?
   * 
   * <p>This will be about subscribers registered. If the subscriber didn't shut down
   * properly it will not be unregistered.
   * 
   * @return True if there are connected subscribers, false otherwise.
   */
  boolean hasSubscribers();
  
  /**
   * Get the number of subscribers currently connected to the publisher.
   * 
   * <p>This will be about subscribers registered. If the subscriber didn't shut down
   * properly it will not be unregistered.
   * 
   * @return The number of subscribers currently connected to the publisher.
   */
  int getNumberOfSubscribers();

  /**
   * Shut the publisher down.
   */
  void shutdown();

}