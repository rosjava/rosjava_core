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
import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.message.old_style.MessageDeserializer;
import org.ros.internal.message.old_style.MessageSerializer;
import org.ros.internal.message.old_style.ServiceMessageDefinitionFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.Registrar;
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
import org.ros.message.MessageDefinition;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;
import org.ros.node.service.ServiceServerListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * The default implementation of a {@link Node}.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNode implements Node {

  private static final boolean DEBUG = false;

  private final GraphName nodeName;
  private final NodeConfiguration nodeConfiguration;
  private final NodeNameResolver resolver;
  private final RosoutLogger log;
  private final MasterClient masterClient;
  private final SlaveServer slaveServer;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final ParameterManager parameterManager;
  private final Registrar registrar;
  private final SubscriberFactory subscriberFactory;
  private final ServiceFactory serviceFactory;
  private final PublisherFactory publisherFactory;
  private final URI masterUri;

  /**
   * Use for all thread creation.
   */
  private final ExecutorService executorService;

  /**
   * All {@link NodeListener} instances registered with the node.
   */
  private final List<NodeListener> nodeListeners = new CopyOnWriteArrayList<NodeListener>();

  /**
   * True if the node is in a running state, false otherwise.
   */
  private boolean running;

  /**
   * {@link DefaultNode}s should only be constructed using the
   * {@link DefaultNodeFactory}.
   * 
   * @param nodeConfiguration
   *          the {@link NodeConfiguration} for this {@link Node}
   */
  public DefaultNode(NodeConfiguration nodeConfiguration) {
    this.nodeConfiguration = NodeConfiguration.copyOf(nodeConfiguration);
    running = false;
    executorService = nodeConfiguration.getExecutorService();
    masterClient = new MasterClient(nodeConfiguration.getMasterUri());
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    parameterManager = new ParameterManager();
    registrar = new Registrar(masterClient, executorService);
    topicManager.setListener(registrar);
    serviceManager.setListener(registrar);

    publisherFactory = new PublisherFactory(topicManager, executorService);

    GraphName basename = nodeConfiguration.getNodeName();
    NameResolver parentResolver = nodeConfiguration.getParentResolver();
    nodeName = parentResolver.getNamespace().join(basename);
    resolver = new NodeNameResolver(nodeName, parentResolver);
    slaveServer =
        new SlaveServer(nodeName, nodeConfiguration.getTcpRosBindAddress(),
            nodeConfiguration.getTcpRosAdvertiseAddress(),
            nodeConfiguration.getXmlRpcBindAddress(),
            nodeConfiguration.getXmlRpcAdvertiseAddress(), masterClient, topicManager,
            serviceManager, parameterManager, executorService);
    subscriberFactory = new SubscriberFactory(slaveServer, topicManager, executorService);
    serviceFactory = new ServiceFactory(nodeName, slaveServer, serviceManager, executorService);

    masterUri = nodeConfiguration.getMasterUri();
    start();

    // NOTE(damonkohler): This must be created after start() is called so that
    // the Registrar can be initialized with the SlaveServer's SlaveIdentifier
    // before trying to register the /rosout Publisher.
    log = new RosoutLogger(this);
  }

  /**
   * Start the node and initiate master registration.
   */
  @VisibleForTesting
  void start() {
    Preconditions.checkState(!running);
    running = true;
    slaveServer.start();
    registrar.start(slaveServer.toSlaveIdentifier());
  }

  @VisibleForTesting
  Registrar getRegistrar() {
    return registrar;
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
  public <MessageType> Publisher<MessageType> newPublisher(GraphName topicName, String messageType) {
    return newPublisher(topicName, messageType, null);
  }

  @Override
  public <MessageType> Publisher<MessageType> newPublisher(GraphName topicName, String messageType,
      Collection<? extends PublisherListener> listeners) {
    GraphName resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition =
        nodeConfiguration.getMessageDefinitionFactory().newFromString(messageType);
    TopicDefinition topicDefinition = TopicDefinition.create(resolvedTopicName, messageDefinition);
    org.ros.message.MessageSerializer<MessageType> serializer = newMessageSerializer(messageType);
    return publisherFactory.create(topicDefinition, serializer, listeners);
  }

  @Override
  public <MessageType> Publisher<MessageType> newPublisher(String topicName, String messageType,
      Collection<? extends PublisherListener> listeners) {
    return newPublisher(new GraphName(topicName), messageType, listeners);
  }

  @Override
  public <MessageType> Publisher<MessageType> newPublisher(String topicName, String messageType) {
    return newPublisher(topicName, messageType, null);
  }

  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(GraphName topicName,
      String messageType, final MessageListener<MessageType> messageListener,
      Collection<? extends SubscriberListener> listeners) {
    GraphName resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition =
        nodeConfiguration.getMessageDefinitionFactory().newFromString(messageType);
    TopicDefinition topicDefinition = TopicDefinition.create(resolvedTopicName, messageDefinition);
    MessageDeserializer<MessageType> deserializer = newMessageDeserializer(messageType);
    Subscriber<MessageType> subscriber =
        subscriberFactory.create(topicDefinition, deserializer, listeners);
    subscriber.addMessageListener(messageListener);
    return subscriber;
  }

  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(GraphName topicName,
      String messageType, final MessageListener<MessageType> messageListener) {
    return newSubscriber(topicName, messageType, messageListener, null);
  }

  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(String topicName, String messageType,
      final MessageListener<MessageType> messageListener,
      Collection<? extends SubscriberListener> listeners) {
    return newSubscriber(new GraphName(topicName), messageType, messageListener, listeners);
  }

  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(String topicName, String messageType,
      final MessageListener<MessageType> messageListener) {
    return newSubscriber(topicName, messageType, messageListener, null);
  }

  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      GraphName serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder,
      Collection<? extends ServiceServerListener> serverListeners) {
    GraphName resolvedServiceName = resolveName(serviceName);
    // TODO(damonkohler): It's rather non-obvious that the URI will be created
    // later on the fly.
    ServiceIdentifier identifier = new ServiceIdentifier(resolvedServiceName, null);
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    ServiceDefinition definition = new ServiceDefinition(identifier, messageDefinition);
    MessageDeserializer<RequestType> requestDeserializer =
        newServiceRequestDeserializer(serviceType);
    MessageSerializer<ResponseType> responseSerializer = newServiceResponseSerializer(serviceType);
    return serviceFactory.createServer(definition, requestDeserializer, responseSerializer,
        responseBuilder, serverListeners);
  }

  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      GraphName serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    return newServiceServer(serviceName, serviceType, responseBuilder, null);
  }

  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder,
      Collection<? extends ServiceServerListener> serverListeners) {
    return newServiceServer(new GraphName(serviceName), serviceType, responseBuilder,
        serverListeners);
  }

  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    return newServiceServer(serviceName, serviceType, responseBuilder, null);
  }

  @Override
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      GraphName serviceName, String serviceType) throws ServiceNotFoundException {
    GraphName resolvedServiceName = resolveName(serviceName);
    URI uri = lookupService(resolvedServiceName);
    if (uri == null) {
      throw new ServiceNotFoundException("No such service " + resolvedServiceName + " of type "
          + serviceType);
    }
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    ServiceIdentifier serviceIdentifier = new ServiceIdentifier(resolvedServiceName, uri);
    ServiceDefinition definition = new ServiceDefinition(serviceIdentifier, messageDefinition);
    MessageSerializer<RequestType> requestSerializer = newServiceRequestSerializer(serviceType);
    MessageDeserializer<ResponseType> responseDeserializer =
        newServiceResponseDeserializer(serviceType);
    return serviceFactory.createClient(definition, requestSerializer, responseDeserializer);
  }

  @Override
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      String serviceName, String serviceType) throws ServiceNotFoundException {
    return newServiceClient(new GraphName(serviceName), serviceType);
  }

  @Override
  public URI lookupService(GraphName serviceName) {
    Response<URI> response =
        masterClient.lookupService(slaveServer.toSlaveIdentifier(), resolveName(serviceName)
            .toString());
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public URI lookupService(String serviceName) {
    return lookupService(new GraphName(serviceName));
  }

  @Override
  public Time getCurrentTime() {
    return nodeConfiguration.getTimeProvider().getCurrentTime();
  }

  @Override
  public GraphName getName() {
    return nodeName;
  }

  @Override
  public Log getLog() {
    return log;
  }

  @Override
  public GraphName resolveName(GraphName name) {
    return resolver.resolve(name);
  }

  @Override
  public GraphName resolveName(String name) {
    return resolver.resolve(new GraphName(name));
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void shutdown() {
    // NOTE(damonkohler): We don't want to raise potentially spurious
    // exceptions during shutdown that would interrupt the process. This is
    // simply best effort cleanup.
    running = false;
    slaveServer.shutdown();
    registrar.shutdown();
    for (Publisher<?> publisher : topicManager.getPublishers()) {
      publisher.shutdown();
      try {
        Response<Integer> response =
            masterClient.unregisterPublisher(slaveServer.toSlaveIdentifier(), publisher);
        if (DEBUG) {
          if (response.getResult() == 0) {
            System.err.println("Failed to unregister publisher: " + publisher.getTopicName());
          }
        }
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (Subscriber<?> subscriber : topicManager.getSubscribers()) {
      subscriber.shutdown();
      try {
        Response<Integer> response =
            masterClient.unregisterSubscriber(slaveServer.toSlaveIdentifier(), subscriber);
        if (DEBUG) {
          if (response.getResult() == 0) {
            System.err.println("Failed to unregister subscriber: " + subscriber.getTopicName());
          }
        }
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (ServiceServer<?, ?> serviceServer : serviceManager.getServers()) {
      try {
        Response<Integer> response =
            masterClient.unregisterService(slaveServer.toSlaveIdentifier(), serviceServer);
        if (DEBUG) {
          if (response.getResult() == 0) {
            System.err.println("Failed to unregister service: " + serviceServer.getName());
          }
        }
      } catch (XmlRpcTimeoutException e) {
        log.error(e);
      } catch (RemoteException e) {
        log.error(e);
      }
    }
    for (ServiceClient<?, ?> serviceClient : serviceManager.getClients()) {
      serviceClient.shutdown();
    }

    signalShutdown();
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
    return org.ros.internal.node.parameter.DefaultParameterTree.create(
        slaveServer.toSlaveIdentifier(), masterClient.getRemoteUri(), resolver, parameterManager);
  }

  @Override
  public URI getUri() {
    return slaveServer.getUri();
  }

  @Override
  public MessageSerializationFactory getMessageSerializationFactory() {
    return nodeConfiguration.getMessageSerializationFactory();
  }

  @Override
  public MessageFactory getMessageFactory() {
    return nodeConfiguration.getMessageFactory();
  }

  @Override
  public void addNodeListener(NodeListener listener) {
    nodeListeners.add(listener);
  }

  @Override
  public void removeNodeListener(NodeListener listener) {
    nodeListeners.remove(listener);
  }

  /**
   * Let all listeners know the node is being created.
   * 
   * <p>
   * Run in the same thread as the caller.
   */
  public void signalCreate() {
    for (NodeListener listener : nodeListeners) {
      listener.onNodeCreate(this);
    }
  }

  /**
   * Let all listeners know the node is being shut down.
   * 
   * <p>
   * Run in a separate thread from the caller.
   */
  private void signalShutdown() {
    final Node node = this;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        for (NodeListener listener : nodeListeners) {
          listener.onNodeShutdown(node);
        }
      }
    });
  }

  @VisibleForTesting
  InetSocketAddress getAddress() {
    return slaveServer.getAddress();
  }
}
