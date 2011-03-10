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

import org.ros.internal.node.RemoteException;

import org.ros.internal.node.server.SlaveServer;

import org.ros.internal.node.ConnectionJobQueue;

import org.ros.internal.node.server.SlaveIdentifier;

import org.ros.message.Message;

import java.io.IOException;
import java.net.URISyntaxException;
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
  private ConnectionJobQueue jobQueue;

  public PubSubFactory(SlaveIdentifier slaveIdentifier, ConnectionJobQueue jobQueue) {
    // TODO(kwc): implement publishers factory
    this.slaveIdentifier = slaveIdentifier;
    subscribers = Maps.newConcurrentMap();
    this.jobQueue = jobQueue;
  }

  /**
   * Get or create internal Subscriber object. Factory uses a Subscriber
   * singleton per topic for efficiency. If a new Subscriber is generated,
   * registration with the Master occurs.
   * 
   * @param topicName
   *          Name of ROS topic that is subscribed to.
   * @param topicDefinition
   *          Description of ROS topic that is subscribed to.
   * @param messageClass
   *          Message type class of topic
   * @return Internal Subscriber implementation instance.
   * @throws RemoteException
   * @throws URISyntaxException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public <S extends Message> Subscriber<S> createSubscriber(SlaveServer slaveServer,
      TopicDefinition description, Class<S> messageClass) throws IOException, URISyntaxException,
      RemoteException {

    String topicName = description.getName();
    Subscriber<S> subscriber;
    if (subscribers.containsKey(topicName)) {
      // Return existing internal subscriber.
      subscriber = (Subscriber<S>) subscribers.get(topicName);
    } else {
      // Create new singleton for topic subscription.
      subscriber = Subscriber.create(slaveIdentifier, description, messageClass, jobQueue);
      subscribers.put(topicName, subscriber);

      // kwc: for now we have factory directly trigger the slaveServer to handle
      // master registration semantics. I'd rather have a listener or other sort
      // of pattern to consolidate master registration communication in a single
      // entity.

      // Slave server handles registration with the ROS Master.
      slaveServer.addSubscriber(subscriber);
    }
    return subscriber;
  }

}
