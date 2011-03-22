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

/**
 * Handles a subscription to a ROS topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 * @param <MessageType>
 */
public class Subscriber<MessageType extends Message> {

  private final Class<MessageType> messageClass;
  private final String topicName;

  private final org.ros.internal.node.topic.Subscriber<MessageType> subscriber;
  private final MessageListener<MessageType> messageListener;

  protected Subscriber(String topicName, MessageListener<MessageType> messageListener,
      Class<MessageType> messageClass,
      org.ros.internal.node.topic.Subscriber<MessageType> subscriber) {
    this.messageClass = messageClass;
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
   * @return The name of the subscribed topic.
   */
  public String getTopicName() {
    return topicName;
  }

  /**
   * @return the {@link Message} class literal for the subscribed topic.
   */
  public Class<MessageType> getTopicMessageClass() {
    return messageClass;
  }

  /**
   * @return The {@link MessageListener} for this {@link Subscriber}.
   */
  public MessageListener<MessageType> getMessageListener() {
    return messageListener;
  }

}
