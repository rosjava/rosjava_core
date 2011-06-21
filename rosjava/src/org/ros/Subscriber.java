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

import org.ros.message.Message;

import java.util.concurrent.TimeUnit;

/**
 * Handles a subscription to a ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 * @param <MessageType>
 */
public class Subscriber<MessageType> {

  /**
   * Fully namespace qualified name of the subscriber.
   */
  private final String topicName;

  private final org.ros.internal.node.topic.Subscriber<MessageType> subscriber;

  private final MessageListener<MessageType> messageListener;

  protected Subscriber(String topicName, MessageListener<MessageType> messageListener,
      org.ros.internal.node.topic.Subscriber<MessageType> subscriber) {
    this.topicName = topicName;
    this.messageListener = messageListener;
    this.subscriber = subscriber;
  }

  /**
   * Cancels this subscription.
   */
  public void cancel() {
    subscriber.removeMessageListener(messageListener);
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
    subscriber.awaitRegistration();
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
    return subscriber.awaitRegistration(timeout, unit);
  }

  /**
   * @return The name of the subscribed topic.
   */
  public String getTopicName() {
    return topicName;
  }

  /**
   * @return The {@link MessageListener} for this {@link Subscriber}.
   */
  public MessageListener<MessageType> getMessageListener() {
    return messageListener;
  }

}
