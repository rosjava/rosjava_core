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

import org.ros.internal.node.server.MasterServer;
import org.ros.message.MessageSerializer;

import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherFactory {

  private final TopicManager topicManager;
  private final ExecutorService executorService;

  public PublisherFactory(TopicManager topicManager, ExecutorService executorService) {
    this.topicManager = topicManager;
    this.executorService = executorService;
  }

  /**
   * Gets or creates a {@link DefaultPublisher} instance.
   * {@link DefaultPublisher}s are cached and reused per topic. When a new
   * {@link DefaultPublisher} is generated, it is registered with the
   * {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition
   *          {@link TopicDefinition} that is being published
   * @return a {@link DefaultSubscriber} instance
   */
  @SuppressWarnings("unchecked")
  public <MessageType> DefaultPublisher<MessageType> create(TopicDefinition topicDefinition,
      MessageSerializer<MessageType> serializer) {
    String topicName = topicDefinition.getName().toString();
    DefaultPublisher<MessageType> publisher;
    boolean createdNewPublisher = false;

    synchronized (topicManager) {
      if (topicManager.hasPublisher(topicName)) {
        publisher = (DefaultPublisher<MessageType>) topicManager.getPublisher(topicName);
      } else {
        publisher = new DefaultPublisher<MessageType>(topicDefinition, serializer, executorService);
        createdNewPublisher = true;
      }
    }

    if (createdNewPublisher) {
      topicManager.putPublisher(publisher);
    }
    return publisher;
  }

}
