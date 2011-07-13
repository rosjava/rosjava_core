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

import org.ros.namespace.GraphName;

import java.util.concurrent.TimeUnit;

/**
 * @see http://www.ros.org/wiki/Topics
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Topic {

  /**
   * @return the name of the subscribed topic
   */
  GraphName getTopicName();

  /**
   * @return the message type (e.g. "std_msgs/String")
   */
  String getTopicMessageType();

  /**
   * @return {@code true} if the {@link Topic} consumer/producer (e.g. a
   *         {@link Subscriber} or a {@link Publisher}) is registered with the
   *         ROS master
   */
  boolean isRegistered();

  /**
   * Wait for the {@link Topic} to register with the ROS master.
   * 
   * <p>
   * This call blocks.
   * 
   * @throws InterruptedException
   */
  void awaitRegistration() throws InterruptedException;

  /**
   * Wait for the {@link Topic} to register with the ROS master.
   * 
   * @param timeout
   *          how long to wait for registration
   * @param unit
   *          the units for how long to wait
   * @return {@code true} if the {@link Subscriber} registered with the ROS
   *         master, {@code false} otherwise
   * @throws InterruptedException
   */
  boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException;

}