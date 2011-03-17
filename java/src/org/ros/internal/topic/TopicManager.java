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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.ros.message.Message;

import java.util.List;
import java.util.Map;

/**
 * Stores internal Publisher and Subscriber instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TopicManager {

  private final Map<String, Subscriber<? extends Message>> subscribers;
  private final Map<String, Publisher<? extends Message>> publishers;

  public TopicManager() {
    publishers = Maps.newConcurrentMap();
    subscribers = Maps.newConcurrentMap();
  }

  public boolean hasSubscriber(String topicName) {
    return subscribers.containsKey(topicName);
  }

  public boolean hasPublisher(String topicName) {
    return publishers.containsKey(topicName);
  }

  public Publisher<? extends Message> getPublisher(String topicName) {
    return publishers.get(topicName);
  }

  public Subscriber<? extends Message> getSubscriber(String topicName) {
    return subscribers.get(topicName);
  }

  public void putPublisher(String topicName, Publisher<? extends Message> publisher) {
    publishers.put(topicName, publisher);
  }

  public void putSubscriber(String topicName, Subscriber<?> subscriber) {
    subscribers.put(topicName, subscriber);
  }

  public List<Subscriber<? extends Message>> getSubscribers() {
    return ImmutableList.copyOf(subscribers.values());
  }

  public List<Publisher<? extends Message>> getPublishers() {
    return ImmutableList.copyOf(publishers.values());
  }

}
