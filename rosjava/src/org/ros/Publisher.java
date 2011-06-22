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
package org.ros;

import java.util.concurrent.TimeUnit;

/**
 * A handle for publishing messages of a particular type on a given topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @param <MessageType>
 *          The message type to template on. The publisher may only publish
 *          messages of this type.
 */
public class Publisher<MessageType> {

  private final org.ros.internal.node.topic.Publisher<MessageType> publisher;
  
  /**
   * Fully qualified namespace topic name for the publisher.
   */
  private final String topicName;

  /**
   * Default package level constructor
   * 
   * @param topicName
   * @param messageClass
   */
  Publisher(String topicName, org.ros.internal.node.topic.Publisher<MessageType> publisher) {
    this.topicName = topicName;
    this.publisher = publisher;
  }

  /**
   * @param message
   *          The {@link Message} to publish. This message will be available on
   *          the topic that this {@link Publisher} has been associated with.
   */
  public void publish(MessageType message) {
    // publisher is shared across multiple publisher instances, so lock access.
    synchronized (this) {
      publisher.publish(message);
    }
  }

  /**
   * Get the topic name for the publisher.
   * 
   * @return The fully-namespaced name of the publisher.
   */
  public String getTopicName() {
    return topicName;
  }

  /**
   * Wait for the publisher to register with the master.
   * 
   * <p>
   * This call blocks.
   * 
   * @throws InterruptedException
   */
  public void awaitRegistration() throws InterruptedException {
    publisher.awaitRegistration();
  }

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
  public boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException {
    return publisher.awaitRegistration(timeout, unit);
  }
  
  /**
   * Does the publisher have any connected subscribers?
   * 
   * <p>This will be about subscribers registered. If the subscriber didn't shut down
   * properly it will not be unregistered.
   * 
   * @return True if there are connected subscribers, false otherwise.
   */
  public boolean hasSubscribers() {
	  return publisher.hasSubscribers();
  }
  
  /**
   * Get the number of subscribers currently connected to the publisher.
   * 
   * <p>This will be about subscribers registered. If the subscriber didn't shut down
   * properly it will not be unregistered.
   * 
   * @return The number of subscribers currently connected to the publisher.
   */
  public int getNumberSubscribers() {
	  return publisher.getNumberSubscribers();
  }

  /**
   * Shut the publisher down.
   */
  public void shutdown() {
    publisher.shutdown();
  }

}