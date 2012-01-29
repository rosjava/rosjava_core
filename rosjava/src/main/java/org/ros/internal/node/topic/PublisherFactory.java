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
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A factory for {@link Publisher} instances.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherFactory {

  private final TopicManager topicManager;
  private final MessageFactory messageFactory;
  private final ScheduledExecutorService executorService;
  private final NodeIdentifier nodeIdentifier;

  public PublisherFactory(NodeIdentifier nodeIdentifier, TopicManager topicManager,
      MessageFactory messageFactory, ScheduledExecutorService executorService) {
    this.nodeIdentifier = nodeIdentifier;
    this.topicManager = topicManager;
    this.messageFactory = messageFactory;
    this.executorService = executorService;
  }

  /**
   * Gets or creates a {@link Publisher} instance. {@link Publisher}s are cached
   * and reused per topic. When a new {@link Publisher} is generated, it is
   * registered with the master.
   * 
   * @param <T>
   *          the message type associated with the {@link Publisher}
   * @param topicDefinition
   *          {@link TopicDefinition} that is being published
   * @param messageSerializer
   *          the {@link MessageSerializer} used for published messages
   * @return a new or cached {@link Publisher} instance
   */
  @SuppressWarnings("unchecked")
  public <T> Publisher<T> newOrExisting(TopicDefinition topicDefinition,
      MessageSerializer<T> messageSerializer) {
    String topicName = topicDefinition.getName().toString();

    synchronized (topicManager) {
      if (topicManager.hasPublisher(topicName)) {
        return (DefaultPublisher<T>) topicManager.getPublisher(topicName);
      } else {
        DefaultPublisher<T> publisher =
            new DefaultPublisher<T>(nodeIdentifier, topicDefinition, messageSerializer,
                messageFactory, executorService);
        publisher.addListener(new DefaultPublisherListener<T>() {
          @Override
          public void onShutdown(Publisher<T> publisher) {
            topicManager.removePublisher((DefaultPublisher<T>) publisher);
          }
        });
        topicManager.putPublisher(publisher);
        return publisher;
      }
    }
  }
}
