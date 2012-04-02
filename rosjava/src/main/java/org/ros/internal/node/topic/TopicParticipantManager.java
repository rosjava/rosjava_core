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
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.List;
import java.util.Map;

/**
 * Manages a collection of {@link Publisher}s and {@link Subscriber}s.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicParticipantManager {

  /**
   * A mapping from topic name to {@link Subscriber}.
   */
  private final Map<GraphName, DefaultSubscriber<?>> subscribers;
  
  /**
   * A mapping from topic name to {@link Publisher}.
   */
  private final Map<GraphName, DefaultPublisher<?>> publishers;

  // TODO(damonkohler): Change to ListenerCollection.
  private TopicParticipantManagerListener listener;

  public TopicParticipantManager() {
    publishers = Maps.newConcurrentMap();
    subscribers = Maps.newConcurrentMap();
  }

  public void setListener(TopicParticipantManagerListener listener) {
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
    publishers.put(publisher.getTopicName(), publisher);
    if (listener != null) {
      listener.onPublisherAdded(publisher);
    }
  }

  public void removePublisher(DefaultPublisher<?> publisher) {
    publishers.remove(publisher.getTopicName());
    if (listener != null) {
      listener.onPublisherRemoved(publisher);
    }
  }

  public void putSubscriber(DefaultSubscriber<?> subscriber) {
    subscribers.put(subscriber.getTopicName(), subscriber);
    if (listener != null) {
      listener.onSubscriberAdded(subscriber);
    }
  }

  public void removeSubscriber(DefaultSubscriber<?> subscriber) {
    subscribers.remove(subscriber.getTopicName());
    if (listener != null) {
      listener.onSubscriberRemoved(subscriber);
    }
  }

  public List<DefaultSubscriber<?>> getSubscribers() {
    return ImmutableList.copyOf(subscribers.values());
  }

  public List<DefaultPublisher<?>> getPublishers() {
    return ImmutableList.copyOf(publishers.values());
  }
}
