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

import org.ros.node.ParameterTree;
import org.ros.node.ServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.NodeServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Implementation of a ROS node. A {@link Node} is responsible for managing
 * various resources including XML-RPC, TCPROS servers, and topic/service
 * instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Node {

  private static final Log log = LogFactory.getLog(Node.class);

  private final MasterClient masterClient;
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final ParameterManager parameterManager;
  private final MasterRegistration masterRegistration;
  private final SubscriberFactory subscriberFactory;
  private final ServiceFactory serviceFactory;
  private final PublisherFactory publisherFactory;

  /**
   * True if the node is in a running state, false otherwise.
   */
  private boolean running;

  public static Node createPublic(GraphName nodeName, URI masterUri, String advertiseHostname,
      int xmlRpcBindPort, int tcpRosBindPort) {
    Node node =
        new Node(nodeName, masterUri, BindAddress.createPublic(tcpRosBindPort),
            new AdvertiseAddress(advertiseHostname), BindAddress.createPublic(xmlRpcBindPort),
            new AdvertiseAddress(advertiseHostname));
    node.start();
    return node;
  }

  public static Node createPrivate(GraphName nodeName, URI masterUri, int xmlRpcBindPort,
      int tcpRosBindPort) {
    Node node =
        new Node(nodeName, masterUri, BindAddress.createPrivate(tcpRosBindPort),
            AdvertiseAddress.createPrivate(), BindAddress.createPrivate(xmlRpcBindPort),
            AdvertiseAddress.createPrivate());
    node.start();
    return node;
  }

  Node(GraphName nodeName, URI masterUri, BindAddress tcpRosBindAddress,
      AdvertiseAddress tcpRosAdvertiseAddress, BindAddress xmlRpcBindAddress,
      AdvertiseAddress xmlRpcAdvertiseAddress) {
    running = false;
    masterClient = new MasterClient(masterUri);
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    parameterManager = new ParameterManager();
    slaveServer =
        new SlaveServer(nodeName, tcpRosBindAddress, tcpRosAdvertiseAddress, xmlRpcBindAddress,
            xmlRpcAdvertiseAddress, masterClient, topicManager, serviceManager, parameterManager);
    masterRegistration = new MasterRegistration(masterClient);
    topicManager.setListener(masterRegistration);
    serviceManager.setListener(masterRegistration);
    publisherFactory = new PublisherFactory(topicManager);
    subscriberFactory = new SubscriberFactory(slaveServer, topicManager);
    serviceFactory = new ServiceFactory(nodeName, slaveServer, serviceManager);
  }

  public <MessageType> Subscriber<MessageType> createSubscriber(TopicDefinition topicDefinition,
      MessageDeserializer<MessageType> deserializer) {
    return subscriberFactory.create(topicDefinition, deserializer);
  }

  public <MessageType> Publisher<MessageType> createPublisher(TopicDefinition topicDefinition,
      MessageSerializer<MessageType> serializer) {
    return publisherFactory.create(topicDefinition, serializer);
  }

  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> createServiceServer(
      ServiceDefinition serviceDefinition, MessageDeserializer<RequestType> deserializer,
      MessageSerializer<ResponseType> serializer,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    return serviceFactory
        .createServer(serviceDefinition, deserializer, serializer, responseBuilder);
  }

  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> createServiceClient(
      ServiceDefinition serviceDefinition, MessageSerializer<RequestType> serializer,
      MessageDeserializer<ResponseType> deserializer) {
    return serviceFactory.createClient(serviceDefinition, serializer, deserializer);
  }

  // TODO(damonkohler): Use a factory to allow consolidating instances later.
  public ParameterTree createParameterTree(NameResolver resolver) {
    return org.ros.internal.node.parameter.ParameterTree.create(slaveServer.toSlaveIdentifier(),
        masterClient.getRemoteUri(), resolver, parameterManager);
  }

  /**
   * Start the node running. This will initiate master registration.
   */
  void start() {
    if (running) {
      throw new IllegalStateException("Already running.");
    }
    running = true;
    slaveServer.start();
    masterRegistration.start(slaveServer.toSlaveIdentifier());
  }

  /**
   * Is the node running?
   * 
   * <p>
   * A running node may not be fully initialized yet, it is either in the
   * process of starting up or is running.
   * 
   * @return True if the node is running, false otherwise.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Shutdown the node and make a best effort attempt to unregister all
   * {@link Publisher}s, {@link Subscriber}s, and {@link ServiceServer}s.
   */
  public void shutdown() {
    // NOTE(damonkohler): We don't want to raise potentially spurious
    // exceptions during shutdown that would interrupt the process. This is
    // simply best effort cleanup.
    running = false;
    slaveServer.shutdown();
    masterRegistration.shutdown();
    for (Publisher<?> publisher : topicManager.getPublishers()) {
      publisher.shutdown();
      try {
        masterClient.unregisterPublisher(slaveServer.toSlaveIdentifier(), publisher);
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (Subscriber<?> subscriber : topicManager.getSubscribers()) {
      subscriber.shutdown();
      try {
        masterClient.unregisterSubscriber(slaveServer.toSlaveIdentifier(), subscriber);
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (ServiceServer<?, ?> serviceServer : serviceManager.getServers()) {
      try {
        masterClient.unregisterService(slaveServer.toSlaveIdentifier(), serviceServer);
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (ServiceClient<?, ?> serviceClient : serviceManager.getClients()) {
      serviceClient.shutdown();
    }
  }

  /**
   * @return the {@link URI} of the {@link NodeServer}
   */
  public URI getUri() {
    return slaveServer.getUri();
  }

  public InetSocketAddress getAddress() {
    return slaveServer.getAddress();
  }
  
  public ServiceIdentifier lookupService(GraphName serviceName) {
    Response<URI> response =
        masterClient.lookupService(slaveServer.toSlaveIdentifier(), serviceName.toString());
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return new ServiceIdentifier(serviceName, response.getResult());
    } else {
      return null;
    }
  }

  /**
   * @return true if Node is fully registered with {@link MasterServer}.
   *         {@code isRegistered()} can become {@code false} if new
   *         {@link Publisher}s or {@link Subscriber}s are created.
   */
  public boolean isRegistered() {
    return masterRegistration.getPendingSize() == 0;
  }

  public boolean isRegistrationOk() {
    return masterRegistration.isMasterRegistrationOk();
  }

}
