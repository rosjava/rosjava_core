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
package org.ros.internal.topic;

import com.google.common.collect.Maps;

import org.ros.internal.node.server.SlaveIdentifier;

import org.ros.message.Message;

import java.util.Map;

/**
 * Factory for generating both user-facing and internal Publisher and Subscriber
 * instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class PubSubFactory {

  private final Map<String, Subscriber<?>> subscribers;
  private final SlaveIdentifier slaveIdentifier;

  public PubSubFactory(SlaveIdentifier slaveIdentifier) {
    // TODO(kwc): implement publishers factory
    this.slaveIdentifier = slaveIdentifier;
    subscribers = Maps.newConcurrentMap();
  }

  /**
   * Get or create internal Subscriber object. Factory uses a Subscriber
   * singleton per topic for efficiency.
   * 
   * @param topicName
   *          Name of ROS topic that is subscribed to.
   * @param topicDefinition
   *          Description of ROS topic that is subscribed to.
   * @param messageClass
   *          Message type class of topic
   * @return Internal Subscriber implementation instance.
   */
  @SuppressWarnings("unchecked")
  public <S extends Message> Subscriber<S> createSubscriber(
      TopicDefinition description, Class<S> messageClass) {
    String topicName = description.getName();
    Subscriber<S> subscriber;
    if (subscribers.containsKey(topicName)) {
      // Return existing internal subscriber.
      subscriber = (Subscriber<S>) subscribers.get(topicName);
    } else {
      // Create new singleton for topic subscription.
      subscriber = Subscriber.create(slaveIdentifier, description, messageClass);
      subscribers.put(topicName, subscriber);
    }
    return subscriber;
  }

}
