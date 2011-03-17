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

import com.google.common.base.Preconditions;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpServer;
import org.ros.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of a ROS node. This implementation is responsible for the
 * managing the various node resources, including XMLRPC and TCPROS servers and
 * topic/service instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Node {

  private final MasterClient master;
  private final SlaveServer slave;
  private final Executor executor;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final TcpServer tcpServer;
  private boolean started;

  public Node(String nodeName, URI masterUri, SocketAddress xmlRpcServerAddress)
      throws MalformedURLException {
    master = new MasterClient(masterUri);
    slave = new SlaveServer(nodeName, master, xmlRpcServerAddress);
    executor = Executors.newCachedThreadPool();
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    tcpServer = new TcpServer(topicManager, serviceManager);
    started = false;
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
      TopicDefinition topicDefinition, Class<MessageType> messageClass) throws IOException,
      URISyntaxException, RemoteException {
    Preconditions.checkState(started, "Node has not been started yet");
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
        subscriber = Subscriber.create(slave.toSlaveIdentifier(), topicDefinition, messageClass,
            executor);
        topicManager.putSubscriber(topicName, subscriber);
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      slave.addSubscriber(subscriber);
    }
    return subscriber;
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Publisher<MessageType> createPublisher(
      TopicDefinition topicDefinition, Class<MessageType> messageClass) throws IOException,
      URISyntaxException, RemoteException {
    Preconditions.checkState(started, "Node has not been started yet");
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
      slave.addPublisher(publisher);
    }
    return publisher;
  }

  /**
   * Start I/O resources.
   * 
   * @param publicHostName
   *          Hostname/address to use to report to public resources.
   * @param tcpRosServerBindAddress
   *          Address to bind TCPROS server to.
   * @throws XmlRpcException
   * @throws IOException
   * @throws URISyntaxException
   */
  public void start(String publicHostName, InetSocketAddress tcpRosServerBindAddress)
      throws XmlRpcException, IOException, URISyntaxException {
    tcpServer.start(tcpRosServerBindAddress);
    // compute the public-facing address for this server.
    InetSocketAddress publicTcpRosServerAddress = new InetSocketAddress(publicHostName, tcpServer
        .getAddress().getPort());
    slave.start(publicTcpRosServerAddress);
    started = true;
  }

  public void stop() {
    tcpServer.shutdown();
    slave.shutdown();
    // TODO(damonkohler): make sure shutdown stops all threads, I/O, etc...
  }

  // TODO(damonkohler): Possibly add some normalization here like in
  // ProtocolDescription?
  public SocketAddress getAddress() {
    return tcpServer.getAddress();
  }

  public SlaveIdentifier getSlaveIdentifier() throws MalformedURLException, URISyntaxException {
    return slave.toSlaveIdentifier();
  }
}
