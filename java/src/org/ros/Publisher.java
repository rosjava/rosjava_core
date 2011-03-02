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

import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.TopicDefinition;
import org.ros.message.Message;

import java.io.IOException;

/**
 * A handle for publishing messages of a particular type on a given topic.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @param <MessageType>
 *          The message type to template on. The publisher may only publish
 *          messages of this type.
 */
public class Publisher<MessageType extends Message> {
  org.ros.internal.topic.Publisher publisher;
  String topicName;
  // deal with type erasure for generics
  Class<MessageType> messageClass;

  /**
   * Default package level constructor
   * 
   * @param topicName
   * @param messageClass
   */
  Publisher(String topicName, Class<MessageType> messageClass) {
    this.topicName = topicName;
    this.messageClass = messageClass;
  }

  /**
   * @param m
   *          The message to publish. This message will be available on the
   *          topic that this Publisher has been associated with.
   */
  public void publish(MessageType m) {
    publisher.publish(m);
  }

  /**
   * This starts up the topic
   * 
   * @throws IOException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  void start() throws IOException, InstantiationException, IllegalAccessException {
    // create an instance of the message of type MessageT
    Message m = messageClass.newInstance();
    TopicDefinition topicDefinition;
    topicDefinition = new TopicDefinition(topicName, MessageDefinition.createFromMessage(m));
    publisher = new org.ros.internal.topic.Publisher(topicDefinition, Ros.getHostName(), 0);
    publisher.start();
  }
}