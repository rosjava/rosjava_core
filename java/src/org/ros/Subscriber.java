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
 * Handle to subscription to ROS topic.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 * 
 * @param <MessageType>
 * 
 */
public class Subscriber<MessageType extends Message> {

  private final Class<MessageType> messageClass;
  private final String topicName;

  private final org.ros.internal.node.topic.Subscriber<MessageType> subscriberImpl;
  /* Callback for new messages. */
  private final MessageListener<MessageType> messageCallback;

  protected Subscriber(String topicName, MessageListener<MessageType> callback,
      Class<MessageType> messageClass, org.ros.internal.node.topic.Subscriber<MessageType> subscriberImpl) {
    this.messageClass = messageClass;
    this.topicName = topicName;
    this.messageCallback = callback;
    this.subscriberImpl = subscriberImpl;
  }

  /**
   * Cancel this subscription's callback.
   */
  public void cancel() {
    subscriberImpl.removeMessageListener(messageCallback);
  }

  public String getTopicName() {
    return topicName;
  }

  public Class<MessageType> getTopicClass() {
    return messageClass;
  }

  public MessageListener<MessageType> getMessageCallback() {
    return messageCallback;
  }

}