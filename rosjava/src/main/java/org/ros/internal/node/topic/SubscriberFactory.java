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
import org.ros.internal.node.server.SlaveServer;
import org.ros.message.MessageDeserializer;

import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberFactory {

  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ExecutorService executorService;

  public SubscriberFactory(SlaveServer slaveServer, TopicManager topicManager,
      ExecutorService executorService) {
    this.slaveServer = slaveServer;
    this.topicManager = topicManager;
    this.executorService = executorService;
  }

  /**
   * Gets or creates a {@link DefaultSubscriber} instance.
   * {@link DefaultSubscriber}s are cached and reused per topic. When a new
   * {@link DefaultSubscriber} is generated, it is registered with the
   * {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition
   *          {@link TopicDefinition} that is subscribed to
   * @return a {@link DefaultSubscriber} instance
   */
  @SuppressWarnings("unchecked")
  public <MessageType> DefaultSubscriber<MessageType> create(TopicDefinition topicDefinition,
      MessageDeserializer<MessageType> deserializer) {
    String topicName = topicDefinition.getName().toString();
    DefaultSubscriber<MessageType> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        subscriber = (DefaultSubscriber<MessageType>) topicManager.getSubscriber(topicName);
      } else {
        subscriber =
            DefaultSubscriber.create(slaveServer.toSlaveIdentifier(), topicDefinition,
                executorService, deserializer);
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      topicManager.putSubscriber(subscriber);
    }
    return subscriber;
  }

}
