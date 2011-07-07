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

package org.ros.internal.node;

import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.message.MessageSerializer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherFactory {

  private final TopicManager topicManager;

  public PublisherFactory(TopicManager topicManager) {
    this.topicManager = topicManager;
  }

  /**
   * Gets or creates a {@link Publisher} instance. {@link Publisher}s are cached
   * and reused per topic. When a new {@link Publisher} is generated, it is
   * registered with the {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition {@link TopicDefinition} that is being published
   * @return a {@link Subscriber} instance
   */
  @SuppressWarnings("unchecked")
  public <MessageType> Publisher<MessageType> create(TopicDefinition topicDefinition,
      MessageSerializer<MessageType> serializer) {
    String topicName = topicDefinition.getName().toString();
    Publisher<MessageType> publisher;
    boolean createdNewPublisher = false;

    synchronized (topicManager) {
      if (topicManager.hasPublisher(topicName)) {
        publisher = (Publisher<MessageType>) topicManager.getPublisher(topicName);
      } else {
        publisher = new Publisher<MessageType>(topicDefinition, serializer);
        createdNewPublisher = true;
      }
    }

    if (createdNewPublisher) {
      topicManager.putPublisher(publisher);
    }
    return publisher;
  }

}
