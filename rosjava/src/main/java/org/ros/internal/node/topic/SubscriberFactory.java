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

import org.ros.internal.node.server.NodeIdentifier;
import org.ros.message.MessageDeserializer;
import org.ros.node.topic.DefaultSubscriberListener;
import org.ros.node.topic.Subscriber;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A factory for {@link Subscriber} instances.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberFactory {

  private final NodeIdentifier nodeIdentifier;
  private final TopicManager topicManager;
  private final ScheduledExecutorService executorService;

  public SubscriberFactory(NodeIdentifier nodeIdentifier, TopicManager topicManager,
      ScheduledExecutorService executorService) {
    this.nodeIdentifier = nodeIdentifier;
    this.topicManager = topicManager;
    this.executorService = executorService;
  }

  /**
   * Gets or creates a {@link Subscriber} instance. {@link Subscriber}s are
   * cached and reused per topic. When a new {@link Subscriber} is generated, it
   * is registered with the master.
   * 
   * @param <T>
   *          the message type associated with the new {@link Subscriber}
   * @param topicDefinition
   *          {@link TopicDefinition} that is subscribed to
   * @param messageDeserializer
   *          the {@link MessageDeserializer} to use for incoming messages
   * @return a new or cached {@link Subscriber} instance
   */
  @SuppressWarnings("unchecked")
  public <T> Subscriber<T> newOrExisting(TopicDefinition topicDefinition,
      MessageDeserializer<T> messageDeserializer) {
    String topicName = topicDefinition.getName().toString();
    DefaultSubscriber<T> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        subscriber = (DefaultSubscriber<T>) topicManager.getSubscriber(topicName);
      } else {
        subscriber =
            DefaultSubscriber.newDefault(nodeIdentifier, topicDefinition,
                executorService, messageDeserializer);
        subscriber.addSubscriberListener(new DefaultSubscriberListener<T>() {
          @Override
          public void onShutdown(Subscriber<T> subscriber) {
            topicManager.removeSubscriber((DefaultSubscriber<T>) subscriber);
          }
        });
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      topicManager.putSubscriber(subscriber);
    }
    return subscriber;
  }
}
