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

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;

import java.util.concurrent.TimeUnit;

/**
 * Handles a subscription to a ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 * @param <MessageType>
 */
public interface Subscriber<MessageType> {

  /**
   * Cancels this subscription.
   */
  void shutdown();

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
   * @return The name of the subscribed topic.
   */
  String getTopicName();

  GraphName getTopicGraphName();

  String getTopicMessageType();

  void addMessageListener(MessageListener<MessageType> listener);

  void removeMessageListener(MessageListener<MessageType> listener);

}
