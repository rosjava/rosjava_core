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
 * A handle for publishing messages of a particular type on a given topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @param <MessageType>
 *          The message type to template on. The publisher may only publish
 *          messages of this type.
 */
public class Publisher<MessageType extends Message> {
  private final org.ros.internal.node.topic.Publisher<MessageType> publisher;
  private final String topicName;
  // deal with type erasure for generics
  private final Class<MessageType> messageClass;

  /**
   * Default package level constructor
   * 
   * @param topicName
   * @param messageClass
   */
  Publisher(String topicName, Class<MessageType> messageClass,
      org.ros.internal.node.topic.Publisher<MessageType> publisher) {
    this.topicName = topicName;
    this.messageClass = messageClass;
    this.publisher = publisher;
  }

  /**
   * @param m
   *          The {@link Message} to publish. This message will be available on
   *          the topic that this {@link Publisher} has been associated with.
   */
  public void publish(MessageType m) {
    // publisher is shared across multiple publisher instances, so lock access.
    synchronized (this) {
      publisher.publish(m);
    }
  }

  public String getTopicName() {
    return topicName;
  }

  /**
   * @return The {@link Message} class literal for the published topic.
   */
  public Class<MessageType> getTopicMessageClass() {
    return messageClass;
  }

}