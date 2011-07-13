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

package org.ros.internal.node.topic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.ros.namespace.GraphName;

import java.util.List;
import java.util.Map;

/**
 * Stores internal Publisher and Subscriber instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicManager {

  private final Map<GraphName, DefaultSubscriber<?>> subscribers;
  private final Map<GraphName, DefaultPublisher<?>> publishers;
  
  private TopicListener listener;

  public TopicManager() {
    publishers = Maps.newConcurrentMap();
    subscribers = Maps.newConcurrentMap();
  }

  public void setListener(TopicListener listener) {
    this.listener = listener;
  }

  public boolean hasSubscriber(String topicName) {
    return subscribers.containsKey(new GraphName(topicName));
  }

  public boolean hasPublisher(String topicName) {
    return publishers.containsKey(new GraphName(topicName));
  }

  public DefaultPublisher<?> getPublisher(String topicName) {
    return publishers.get(new GraphName(topicName));
  }

  public DefaultSubscriber<?> getSubscriber(String topicName) {
    return subscribers.get(new GraphName(topicName));
  }

  public void putPublisher(DefaultPublisher<?> publisher) {
    GraphName topicGraphName = publisher.getTopicName();
    publishers.put(topicGraphName, publisher);
    if (listener != null) {
      listener.publisherAdded(publisher);
    }
  }

  public void putSubscriber(DefaultSubscriber<?> subscriber) {
    subscribers.put(subscriber.getTopicName(), subscriber);
    if (listener != null) {
      listener.subscriberAdded(subscriber);
    }
  }

  public List<DefaultSubscriber<?>> getSubscribers() {
    return ImmutableList.copyOf(subscribers.values());
  }

  public List<DefaultPublisher<?>> getPublishers() {
    return ImmutableList.copyOf(publishers.values());
  }

}
