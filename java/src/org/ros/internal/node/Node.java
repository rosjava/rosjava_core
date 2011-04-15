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

import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.ros.MessageDeserializer;
import org.ros.MessageSerializer;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.NodeServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpRosServer;
import org.ros.message.Message;
import org.ros.message.Service;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of a ROS node. This implementation is responsible for the
 * managing the various node resources including XML-RPC, TCPROS servers, and
 * topic/service instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Node {

  private static final Log log = LogFactory.getLog(Node.class);

  private final Executor executor;
  private final GraphName nodeName;
  private final MasterClient masterClient;
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final TcpRosServer tcpRosServer;
  private final MasterRegistration masterRegistration;

  private boolean started;

  public static Node createPublic(GraphName nodeName, URI masterUri, String advertiseHostname,
      int xmlRpcBindPort, int tcpRosBindPort) throws XmlRpcException, IOException,
      URISyntaxException {
    Node node = new Node(nodeName, masterUri, BindAddress.createPublic(tcpRosBindPort),
        new AdvertiseAddress(advertiseHostname), BindAddress.createPublic(xmlRpcBindPort),
        new AdvertiseAddress(advertiseHostname));
    node.start();
    return node;
  }

  public static Node createPrivate(GraphName nodeName, URI masterUri, int xmlRpcBindPort,
      int tcpRosBindPort) throws XmlRpcException, IOException, URISyntaxException {
    Node node = new Node(nodeName, masterUri, BindAddress.createPrivate(tcpRosBindPort),
        AdvertiseAddress.createPrivate(), BindAddress.createPrivate(xmlRpcBindPort),
        AdvertiseAddress.createPrivate());
    node.start();
    return node;
  }

  Node(GraphName nodeName, URI masterUri, BindAddress tcpRosBindAddress,
      AdvertiseAddress tcpRosAdvertiseAddress, BindAddress xmlRpcBindAddress,
      AdvertiseAddress xmlRpcAdvertiseAddress) throws MalformedURLException {
    this.nodeName = nodeName;
    started = false;
    executor = Executors.newCachedThreadPool();
    masterClient = new MasterClient(masterUri);
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    tcpRosServer = new TcpRosServer(tcpRosBindAddress, tcpRosAdvertiseAddress, topicManager,
        serviceManager);
    slaveServer = new SlaveServer(nodeName, xmlRpcBindAddress, xmlRpcAdvertiseAddress,
        masterClient, topicManager, serviceManager, tcpRosServer);

    masterRegistration = new MasterRegistration(masterClient);
    topicManager.setListener(masterRegistration);
  }

  /**
   * Gets or creates a {@link Subscriber} instance. {@link Subscriber}s are
   * cached and reused per topic. When a new {@link Subscriber} is generated, it
   * is registered with the {@link MasterServer}.
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
  public <MessageType> Subscriber<MessageType> createSubscriber(TopicDefinition topicDefinition,
      Class<MessageType> messageClass, MessageDeserializer<MessageType> deserializer)
      throws IOException, URISyntaxException, RemoteException {
    String topicName = topicDefinition.getName().toString();
    Subscriber<MessageType> subscriber;
    boolean createdNewSubscriber = false;

    synchronized (topicManager) {
      if (topicManager.hasSubscriber(topicName)) {
        subscriber = (Subscriber<MessageType>) topicManager.getSubscriber(topicName);
      } else {
        subscriber = Subscriber.create(slaveServer.toSlaveIdentifier(), topicDefinition,
            messageClass, executor, deserializer);
        createdNewSubscriber = true;
      }
    }

    if (createdNewSubscriber) {
      topicManager.putSubscriber(topicName, subscriber);
    }
    return subscriber;
  }

  /**
   * Gets or creates a {@link Publisher} instance. {@link Publisher}s are cached
   * and reused per topic. When a new {@link Publisher} is generated, it is
   * registered with the {@link MasterServer}.
   * 
   * @param <MessageType>
   * @param topicDefinition
   *          {@link TopicDefinition} that is being published
   * @param messageClass
   *          {@link Message} class for topic
   * @return a {@link Subscriber} instance
   * @throws RemoteException
   * @throws URISyntaxException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Publisher<MessageType> createPublisher(
      TopicDefinition topicDefinition, Class<MessageType> messageClass,
      MessageSerializer<MessageType> serializer) throws IOException, URISyntaxException,
      RemoteException {
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
      topicManager.putPublisher(publisher.getTopicName().toString(), publisher);
    }
    return publisher;
  }

  /**
   * Gets or creates a {@link ServiceServer} instance. {@link ServiceServer}s
   * are cached and reused per service. When a new {@link ServiceServer} is
   * generated, it is registered with the {@link MasterServer}.
   * 
   * @param serviceDefinition
   *          the {@link ServiceDefinition} that is being served
   * @param responseBuilder
   *          the {@link ServiceResponseBuilder} that is used to build responses
   * @return a {@link ServiceServer} instance
   * @throws Exception
   */
  public <RequestType, ResponseType> ServiceServer createServiceServer(
      ServiceDefinition serviceDefinition,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) throws Exception {
    ServiceServer serviceServer;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServiceServer(name)) {
        serviceServer = serviceManager.getServiceServer(name);
      } else {
        serviceServer = new ServiceServer(serviceDefinition, responseBuilder,
            tcpRosServer.getAdvertiseAddress());
        createdNewService = true;
      }
    }

    if (createdNewService) {
      slaveServer.addService(serviceServer);
    }
    return serviceServer;
  }

  /**
   * Gets or creates a {@link ServiceClient} instance. {@link ServiceClient}s
   * are cached and reused per service. When a new {@link ServiceClient} is
   * created, it is connected to the {@link ServiceServer}.
   * 
   * @param <ResponseMessageType>
   * @param serviceIdentifier
   *          the {@link ServiceIdentifier} of the server
   * @return a {@link ServiceClient} instance
   */
  @SuppressWarnings("unchecked")
  public <ResponseMessageType> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, MessageDeserializer<ResponseMessageType> deserializer) {
    ServiceClient<ResponseMessageType> serviceClient;
    String name = serviceIdentifier.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServiceClient(name)) {
        serviceClient = (ServiceClient<ResponseMessageType>) serviceManager.getServiceClient(name);
      } else {
        serviceClient = ServiceClient.create(nodeName, serviceIdentifier, deserializer);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      serviceClient.connect(serviceIdentifier.getUri());
    }
    return serviceClient;
  }

  void start() throws XmlRpcException, IOException, URISyntaxException {
    if (started) {
      throw new IllegalStateException("Already started.");
    }
    started = true;
    slaveServer.start();
    masterRegistration.start(slaveServer.toSlaveIdentifier());
  }

  /**
   * Stops the node.
   */
  public void stop() {
    for (Publisher<?> pub : topicManager.getPublishers()) {
      pub.shutdown();
    }
    for (Subscriber<?> sub : topicManager.getSubscribers()) {
      sub.shutdown();
    }
    // TODO: need to shutdown services as well
    slaveServer.shutdown();
    tcpRosServer.shutdown();
    masterRegistration.shutdown();
  }

  @VisibleForTesting
  TcpRosServer getTcpRosServer() {
    return tcpRosServer;
  }

  @VisibleForTesting
  SlaveServer getSlaveServer() {
    return slaveServer;
  }

  /**
   * @return the {@link URI} of the {@link NodeServer}
   */
  public URI getUri() {
    return slaveServer.getUri();
  }

  public ServiceIdentifier lookupService(GraphName serviceName, Service<?, ?> serviceType)
      throws RemoteException, XmlRpcTimeoutException {
    Response<URI> response;
    try {
      response = masterClient
          .lookupService(slaveServer.toSlaveIdentifier(), serviceName.toString());
      if (response.getStatusCode() == StatusCode.SUCCESS) {
        ServiceDefinition serviceDefinition = new ServiceDefinition(serviceName,
            serviceType.getDataType(), serviceType.getMD5Sum());
        return new ServiceIdentifier(response.getResult(), serviceDefinition);
      } else {
        return null;
      }
    } catch (URISyntaxException e) {
      // TODO(kwc) what should be the error policy here be?
      log.error("master returned invalid URI for lookupService", e);
      throw new RemoteException(StatusCode.FAILURE, "master returned invalid URI");
    } catch (UndeclaredThrowableException e) { 
      //TODO: add more general IO error
      log.error("undeclared throwable leaked through API", e);
      throw new XmlRpcTimeoutException(0, "unable to commnicate with master");
    }
  }

  /**
   * @return true if Node is fully registered with {@link MasterServer}.
   *         {@code isRegistered()} can go to false if new publisher or
   *         subscribers are created.
   */
  public boolean isRegistered() {
    return masterRegistration.getPendingSize() == 0;
  }

  public boolean isRegistrationOk() {
    return masterRegistration.isMasterRegistrationOk();
  }

}
