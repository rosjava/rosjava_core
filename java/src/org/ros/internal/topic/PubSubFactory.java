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

import com.google.common.base.Preconditions;

import org.ros.exceptions.RosInitException;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.transport.tcp.TcpServer;
import org.ros.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;

/**
 * Factory for generating both user-facing and internal Publisher and Subscriber
 * instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class PubSubFactory {

  private final SlaveIdentifier slaveIdentifier;
  private final Executor executor;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  
  private TcpServer server;

  public PubSubFactory(SlaveIdentifier slaveIdentifier, Executor executor) {
    this.slaveIdentifier = slaveIdentifier;
    this.executor = executor;
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
  }

  /**
   * Get or create a {@link Subscriber} instance. {@link Subscriber}s are cached
   * and reused per topic for efficiency. If a new {@link Subscriber} is
   * generated, it is registered with the {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition
   *          {@link TopicDefinition} that is subscribed to
   * @param messageClass
   *          {@link Message} class for topic
   * @return a {@link Subscriber} instance
   * @throws RemoteException
   * @throws URISyntaxException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(
      SlaveServer slaveServer, TopicDefinition topicDefinition, Class<MessageType> messageClass)
      throws IOException, URISyntaxException, RemoteException {
    String topicName = topicDefinition.getName();
    Subscriber<MessageType> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        // Return existing internal subscriber.
        subscriber = (Subscriber<MessageType>) topicManager.getSubscriber(topicName);
        Preconditions.checkState(subscriber.checkMessageClass(messageClass));
      } else {
        // Create new underlying implementation for topic subscription.
        subscriber = Subscriber.create(slaveIdentifier, topicDefinition, messageClass, executor);
        topicManager.putSubscriber(topicName, subscriber);
        createdNewSubscriber = true;
      }
    }

    // TODO(kwc): for now we have factory directly trigger the slaveServer to
    // handle master registration semantics. I'd rather have a listener or other
    // sort of pattern to consolidate master registration communication in a
    // single entity.
    if (createdNewSubscriber) {
      slaveServer.addSubscriber(subscriber);
    }
    return subscriber;
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Publisher<MessageType> createPublisher(
      SlaveServer slaveServer, TopicDefinition topicDefinition, Class<MessageType> messageClass)
      throws IOException, URISyntaxException, RemoteException {
    String topicName = topicDefinition.getName();
    Publisher<MessageType> publisher;
    boolean createdNewPublisher = false;

    synchronized (topicManager) {
      if (topicManager.hasPublisher(topicName)) {
        publisher = (Publisher<MessageType>) topicManager.getPublisher(topicName);
        Preconditions.checkState(publisher.checkMessageClass(messageClass));
      } else {
        publisher = new Publisher<MessageType>(topicDefinition, messageClass);
        topicManager.putPublisher(topicName, publisher);
        createdNewPublisher = true;
      }
    }

    if (createdNewPublisher) {
      slaveServer.addPublisher(publisher);
      publisher.start();
    }
    return publisher;
  }

  public InetSocketAddress startTcpRosServer(InetSocketAddress tcpRosServerAddress,
      String publicHostname) throws RosInitException {
    try {
      server = new TcpServer(topicManager, serviceManager);
    } catch (Exception e) {
      throw new RosInitException(e);
    }
    server.start(tcpRosServerAddress);
    // Override address that TCPROS server reports with the hostname/IP
    // address that we've been configured to report.
    return new InetSocketAddress(publicHostname, server.getAddress().getPort());
  }

  public void stopTcpRosServer() {
    server.shutdown();
    server = null;
  }
}
