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

import org.ros.MessageDeserializer;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.message.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberFactory {
  
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final Executor executor;
  
  public SubscriberFactory(SlaveServer slaveServer, TopicManager topicManager) {
    this.slaveServer = slaveServer;
    this.topicManager = topicManager;
    this.executor = Executors.newCachedThreadPool();
  }
  
  /**
   * Gets or creates a {@link Subscriber} instance. {@link Subscriber}s are
   * cached and reused per topic. When a new {@link Subscriber} is generated, it
   * is registered with the {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition {@link TopicDefinition} that is subscribed to
   * @param messageClass {@link Message} class for topic
   * @return a {@link Subscriber} instance
   */
  @SuppressWarnings("unchecked")
  public <MessageType> Subscriber<MessageType> create(TopicDefinition topicDefinition,
      Class<MessageType> messageClass, MessageDeserializer<MessageType> deserializer) {
    String topicName = topicDefinition.getName().toString();
    Subscriber<MessageType> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        subscriber = (Subscriber<MessageType>) topicManager.getSubscriber(topicName);
      } else {
        subscriber =
            Subscriber.create(slaveServer.toSlaveIdentifier(), topicDefinition, messageClass,
                executor, deserializer);
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      topicManager.putSubscriber(subscriber);
    }
    return subscriber;
  }

}
