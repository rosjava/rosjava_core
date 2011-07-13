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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.message.new_style.MessageDefinition;
import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.message.old_style.MessageDefinitionFactory;
import org.ros.internal.message.old_style.MessageDeserializer;
import org.ros.internal.message.old_style.MessageSerializer;
import org.ros.internal.message.old_style.ServiceMessageDefinitionFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceFactory;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.topic.PublisherFactory;
import org.ros.internal.node.topic.SubscriberFactory;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.internal.time.TimeProvider;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * The default implementation of a {@link Node}.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNode implements Node {

  private final GraphName nodeName;
  private final NodeConfiguration nodeConfiguration;
  private final NodeNameResolver resolver;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;
  private final MasterClient masterClient;
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final ParameterManager parameterManager;
  private final MasterRegistration masterRegistration;
  private final SubscriberFactory subscriberFactory;
  private final ServiceFactory serviceFactory;
  private final PublisherFactory publisherFactory;
  private final URI masterUri;

  /**
   * True if the node is in a running state, false otherwise.
   */
  private boolean running;

  public DefaultNode(NodeConfiguration nodeConfiguration) {
    this.nodeConfiguration = NodeConfiguration.copyOf(nodeConfiguration);
    running = false;
    masterClient = new MasterClient(nodeConfiguration.getMasterUri());
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    parameterManager = new ParameterManager();
    masterRegistration = new MasterRegistration(masterClient);
    topicManager.setListener(masterRegistration);
    serviceManager.setListener(masterRegistration);
    publisherFactory = new PublisherFactory(topicManager);

    GraphName basename = nodeConfiguration.getNodeName();
    NameResolver parentResolver = nodeConfiguration.getParentResolver();
    nodeName = parentResolver.getNamespace().join(basename);
    resolver = new NodeNameResolver(nodeName, parentResolver);
    slaveServer =
        new SlaveServer(nodeName, nodeConfiguration.getTcpRosBindAddress(),
            nodeConfiguration.getTcpRosAdvertiseAddress(),
            nodeConfiguration.getXmlRpcBindAddress(),
            nodeConfiguration.getXmlRpcAdvertiseAddress(), masterClient, topicManager,
            serviceManager, parameterManager);
    subscriberFactory = new SubscriberFactory(slaveServer, topicManager);
    serviceFactory = new ServiceFactory(nodeName, slaveServer, serviceManager);

    // TODO(kwc): Implement simulated time.
    timeProvider = new WallclockProvider();

    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);
    Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher =
        newPublisher("/rosout", "rosgraph_msgs/Log");
    log.setRosoutPublisher(rosoutPublisher);

    masterUri = nodeConfiguration.getMasterUri();
    start();
  }

  /**
   * Start the node and initiate master registration.
   */
  @VisibleForTesting
  void start() {
    Preconditions.checkState(!running);
    running = true;
    slaveServer.start();
    masterRegistration.start(slaveServer.toSlaveIdentifier());
  }

  private <MessageType> org.ros.message.MessageSerializer<MessageType> newMessageSerializer(
      String messageType) {
    return nodeConfiguration.getMessageSerializationFactory().newMessageSerializer(messageType);
  }

  @SuppressWarnings("unchecked")
  private <MessageType> MessageDeserializer<MessageType> newMessageDeserializer(String messageType) {
    return (MessageDeserializer<MessageType>) nodeConfiguration.getMessageSerializationFactory()
        .newMessageDeserializer(messageType);
  }

  @SuppressWarnings("unchecked")
  private <ResponseType> MessageSerializer<ResponseType> newServiceResponseSerializer(
      String serviceType) {
    return (MessageSerializer<ResponseType>) nodeConfiguration.getMessageSerializationFactory()
        .newServiceResponseSerializer(serviceType);
  }

  @SuppressWarnings("unchecked")
  private <ResponseType> MessageDeserializer<ResponseType> newServiceResponseDeserializer(
      String serviceType) {
    return (MessageDeserializer<ResponseType>) nodeConfiguration.getMessageSerializationFactory()
        .newServiceResponseDeserializer(serviceType);
  }

  @SuppressWarnings("unchecked")
  private <RequestType> MessageSerializer<RequestType> newServiceRequestSerializer(
      String serviceType) {
    return (MessageSerializer<RequestType>) nodeConfiguration.getMessageSerializationFactory()
        .newServiceRequestSerializer(serviceType);
  }

  @SuppressWarnings("unchecked")
  private <RequestType> MessageDeserializer<RequestType> newServiceRequestDeserializer(
      String serviceType) {
    return (MessageDeserializer<RequestType>) nodeConfiguration.getMessageSerializationFactory()
        .newServiceRequestDeserializer(serviceType);
  }

  @Override
  public <MessageType> Publisher<MessageType> newPublisher(String topicName, String messageType) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(new GraphName(resolvedTopicName), messageDefinition);
    org.ros.message.MessageSerializer<MessageType> serializer = newMessageSerializer(messageType);
    return publisherFactory.create(topicDefinition, serializer);
  }

  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(String topicName, String messageType,
      final MessageListener<MessageType> listener) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(new GraphName(resolvedTopicName), messageDefinition);
    MessageDeserializer<MessageType> deserializer = newMessageDeserializer(messageType);
    Subscriber<MessageType> subscriber = subscriberFactory.create(topicDefinition, deserializer);
    subscriber.addMessageListener(listener);
    return subscriber;
  }

  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    // TODO(damonkohler): It's rather non-obvious that the URI will be created
    // later on the fly.
    ServiceIdentifier identifier = new ServiceIdentifier(new GraphName(serviceName), null);
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    ServiceDefinition definition = new ServiceDefinition(identifier, messageDefinition);
    MessageDeserializer<RequestType> requestDeserializer =
        newServiceRequestDeserializer(serviceType);
    MessageSerializer<ResponseType> responseSerializer = newServiceResponseSerializer(serviceType);
    return serviceFactory.createServer(definition, requestDeserializer, responseSerializer,
        responseBuilder);
  }

  @Override
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      String serviceName, String serviceType) throws ServiceNotFoundException {
    URI uri = lookupService(serviceName);
    if (uri == null) {
      throw new ServiceNotFoundException("No such service " + serviceName + " of type "
          + serviceType);
    }
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    GraphName resolvedServiceName = new GraphName(resolveName(serviceName));
    ServiceIdentifier serviceIdentifier = new ServiceIdentifier(resolvedServiceName, uri);
    ServiceDefinition definition = new ServiceDefinition(serviceIdentifier, messageDefinition);
    MessageSerializer<RequestType> requestSerializer = newServiceRequestSerializer(serviceType);
    MessageDeserializer<ResponseType> responseDeserializer =
        newServiceResponseDeserializer(serviceType);
    return serviceFactory.createClient(definition, requestSerializer, responseDeserializer);
  }

  @Override
  public URI lookupService(String serviceName) {
    Response<URI> response =
        masterClient.lookupService(slaveServer.toSlaveIdentifier(), resolveName(serviceName));
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public Time getCurrentTime() {
    return timeProvider.getCurrentTime();
  }

  @Override
  public String getName() {
    return nodeName.toString();
  }

  @Override
  public Log getLog() {
    return log;
  }

  @Override
  public String resolveName(String name) {
    return resolver.resolve(name);
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

  @Override
  public boolean isOk() {
    return isRunning() && isRegistered();
  }

  @Override
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

  @Override
  public URI getMasterUri() {
    return masterUri;
  }

  @Override
  public NodeNameResolver getResolver() {
    return resolver;
  }

  @Override
  public ParameterTree newParameterTree() {
    return org.ros.internal.node.parameter.ParameterTree.create(slaveServer.toSlaveIdentifier(),
        masterClient.getRemoteUri(), resolver, parameterManager);
  }

  @Override
  public URI getUri() {
    return slaveServer.getUri();
  }

  @Override
  public InetSocketAddress getAddress() {
    return slaveServer.getAddress();
  }

  @Override
  public boolean isRegistered() {
    return masterRegistration.getPendingSize() == 0;
  }

  @Override
  public boolean isRegistrationOk() {
    return masterRegistration.isMasterRegistrationOk();
  }

}
