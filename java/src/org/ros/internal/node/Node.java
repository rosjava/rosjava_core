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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpRosServer;
import org.ros.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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

  private static final String LOOPBACK = "127.0.0.1";

  private final Executor executor;
  private final String nodeName;
  private final MasterClient masterClient;
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final TcpRosServer tcpRosServer;

  public static Node createPublic(String nodeName, URI masterUri, String publicHostname,
      int xmlRpcBindPort, int tcpRosBindPort) throws XmlRpcException, IOException,
      URISyntaxException {
    Node node = new Node(nodeName, masterUri, new NodeSocketAddress(new InetSocketAddress(
        xmlRpcBindPort), publicHostname), new NodeSocketAddress(new InetSocketAddress(
        tcpRosBindPort), publicHostname));
    node.start();
    return node;
  }

  public static Node createPrivate(String nodeName, URI masterUri, int xmlRpcBindPort,
      int tcpRosBindPort) throws XmlRpcException, IOException, URISyntaxException {
    // Public hostname is automatically set to "localhost" in private mode.
    String publicHostname = "localhost";
    Node node = new Node(nodeName, masterUri, new NodeSocketAddress(new InetSocketAddress(LOOPBACK,
        xmlRpcBindPort), publicHostname), new NodeSocketAddress(new InetSocketAddress(LOOPBACK,
        tcpRosBindPort), publicHostname));
    node.start();
    return node;
  }

  Node(String nodeName, URI masterUri, NodeSocketAddress xmlRpcBindAddress,
      NodeSocketAddress tcpRosBindAddress) throws MalformedURLException {
    this.nodeName = nodeName;
    executor = Executors.newCachedThreadPool();
    masterClient = new MasterClient(masterUri);
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    tcpRosServer = new TcpRosServer(tcpRosBindAddress, topicManager, serviceManager);
    slaveServer = new SlaveServer(nodeName, xmlRpcBindAddress, masterClient, topicManager,
        serviceManager, tcpRosServer);
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
    String topicName = topicDefinition.getName();
    Subscriber<MessageType> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        subscriber = (Subscriber<MessageType>) topicManager.getSubscriber(topicName);
        Preconditions.checkState(subscriber.checkMessageClass(messageClass));
      } else {
        subscriber = Subscriber.create(slaveServer.toSlaveIdentifier(), topicDefinition,
            messageClass, executor);
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      slaveServer.addSubscriber(subscriber);
    }
    return subscriber;
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Publisher<MessageType> createPublisher(
      TopicDefinition topicDefinition, Class<MessageType> messageClass) throws IOException,
      URISyntaxException, RemoteException {
    String topicName = topicDefinition.getName();
    Publisher<MessageType> publisher;
    boolean createdNewPublisher = false;

    synchronized (topicManager) {
      if (topicManager.hasPublisher(topicName)) {
        publisher = (Publisher<MessageType>) topicManager.getPublisher(topicName);
        Preconditions.checkState(publisher.checkMessageClass(messageClass));
      } else {
        publisher = new Publisher<MessageType>(topicDefinition, messageClass);
        createdNewPublisher = true;
      }
    }

    if (createdNewPublisher) {
      slaveServer.addPublisher(publisher);
    }
    return publisher;
  }

  @SuppressWarnings("unchecked")
  public <RequestMessageType extends Message> ServiceServer<RequestMessageType> createServiceServer(
      ServiceDefinition serviceDefinition, Class<RequestMessageType> requestMessageClass,
      ServiceResponseBuilder<RequestMessageType> responseBuilder) throws MalformedURLException,
      URISyntaxException, RemoteException {
    ServiceServer<RequestMessageType> serviceServer;
    String name = serviceDefinition.getName();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasService(name)) {
        serviceServer = (ServiceServer<RequestMessageType>) serviceManager.getService(name);
        Preconditions.checkState(serviceServer.checkMessageClass(requestMessageClass));
      } else {
        serviceServer = new ServiceServer<RequestMessageType>(serviceDefinition,
            requestMessageClass, responseBuilder);
        serviceServer.setAddress(tcpRosServer.getAddress());
        createdNewService = true;
      }
    }

    if (createdNewService) {
      slaveServer.addService(serviceServer);
    }
    return serviceServer;
  }

  // TODO(damonkohler): Cache clients.
  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, Class<ResponseMessageType> responseMessageClass) {
    ServiceClient<ResponseMessageType> serviceClient = ServiceClient.create(responseMessageClass,
        nodeName, serviceIdentifier);
    URI uri = serviceIdentifier.getUri();
    serviceClient.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
    return serviceClient;
  }

  /**
   * Start the node.
   * 
   * @throws XmlRpcException
   * @throws IOException
   * @throws URISyntaxException
   */
  public void start() throws XmlRpcException, IOException, URISyntaxException {
    slaveServer.start();
  }

  public void stop() {
    slaveServer.shutdown();
  }

  /**
   * @return the {@link TcpRosServer}
   */
  @VisibleForTesting
  TcpRosServer getTcpRosServer() {
    return tcpRosServer;
  }

  /**
   * @return the {@link SlaveServer}
   */
  @VisibleForTesting
  SlaveServer getSlaveServer() {
    return slaveServer;
  }

  /**
   * @return {@link URI} of the Node server (XML-RPC).
   */
  public URI getUri() {
    return slaveServer.getUri();
  }

}
