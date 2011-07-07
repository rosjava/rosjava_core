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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.Ros;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.message.new_style.MessageDefinition;
import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.message.old_style.MessageDefinitionFactory;
import org.ros.internal.message.old_style.MessageDeserializer;
import org.ros.internal.message.old_style.MessageSerializer;
import org.ros.internal.message.old_style.ServiceMessageDefinitionFactory;
import org.ros.internal.namespace.NodeNameResolver;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.ParameterTree;
import org.ros.node.Publisher;
import org.ros.node.ServiceClient;
import org.ros.node.ServiceServer;
import org.ros.node.Subscriber;
import org.ros.time.TimeProvider;

import java.net.InetAddress;
import java.net.URI;

/**
 * A default implementation of a {@link Node}.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNode implements Node {

  private final NodeConfiguration configuration;
  private final NodeNameResolver resolver;
  private final GraphName nodeName;
  private final org.ros.internal.node.Node node;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;

  public DefaultNode(GraphName name, NodeConfiguration configuration) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(configuration.getHost());
    Preconditions.checkNotNull(name);
    this.configuration = configuration;
    NameResolver parentResolver = configuration.getParentResolver();
    GraphName basename;
    String nodeNameOverride = configuration.getNodeNameOverride();
    if (nodeNameOverride != null) {
      basename = Ros.newGraphName(nodeNameOverride);
    } else {
      basename = name;
    }
    nodeName = parentResolver.getNamespace().join(basename);
    resolver = NodeNameResolver.create(parentResolver, nodeName);

    // TODO(kwc): Implement simulated time.
    // TODO(damonkohler): Move TimeProvider into NodeConfiguration.
    timeProvider = new WallclockProvider();

    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);

    InetAddress host = InetAddressFactory.createFromHostString(configuration.getHost());
    if (host.isLoopbackAddress()) {
      node =
          org.ros.internal.node.Node.createPrivate(nodeName, configuration.getMasterUri(),
              configuration.getXmlRpcPort(), configuration.getTcpRosPort());
    } else {
      node =
          org.ros.internal.node.Node
              .createPublic(nodeName, configuration.getMasterUri(), configuration.getHost(),
                  configuration.getXmlRpcPort(), configuration.getTcpRosPort());
    }

    // TODO(damonkohler): Move the creation and management of the RosoutLogger
    // into the internal.Node class.
    Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher =
        newPublisher("/rosout", "rosgraph_msgs/Log");
    log.setRosoutPublisher(rosoutPublisher);
  }

  @Override
  public <MessageType> Publisher<MessageType> newPublisher(String topicName, String messageType) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(Ros.newGraphName(resolvedTopicName), messageDefinition);
    org.ros.message.MessageSerializer<MessageType> serializer =
        configuration.getMessageSerializationFactory().createSerializer(messageType);
    return node.createPublisher(topicDefinition, serializer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> Subscriber<MessageType> newSubscriber(String topicName, String messageType,
      final MessageListener<MessageType> listener) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(Ros.newGraphName(resolvedTopicName), messageDefinition);
    MessageDeserializer<MessageType> deserializer =
        (MessageDeserializer<MessageType>) configuration.getMessageSerializationFactory()
            .createDeserializer(messageType);
    Subscriber<MessageType> subscriber = node.createSubscriber(topicDefinition, deserializer);
    subscriber.addMessageListener(listener);
    return subscriber;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    // TODO(damonkohler): It's rather non-obvious that the URI will be created
    // later on the fly.
    ServiceIdentifier identifier = new ServiceIdentifier(Ros.newGraphName(serviceName), null);
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    ServiceDefinition definition = new ServiceDefinition(identifier, messageDefinition);
    MessageDeserializer<RequestType> requestDeserializer =
        (MessageDeserializer<RequestType>) configuration.getMessageSerializationFactory()
            .createServiceRequestDeserializer(serviceType);
    MessageSerializer<ResponseType> responseSerializer =
        (MessageSerializer<ResponseType>) configuration.getMessageSerializationFactory()
            .createServiceResponseSerializer(serviceType);
    return node.createServiceServer(definition, requestDeserializer, responseSerializer,
        responseBuilder);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      String serviceName, String serviceType) {
    ServiceIdentifier identifier = lookupService(serviceName);
    if (identifier == null) {
      throw new RosRuntimeException("No such service: " + serviceName + " of type " + serviceType);
    }
    ServiceMessageDefinition messageDefinition =
        ServiceMessageDefinitionFactory.createFromString(serviceType);
    ServiceDefinition definition = new ServiceDefinition(identifier, messageDefinition);
    MessageSerializer<RequestType> requestSerializer =
        (MessageSerializer<RequestType>) configuration.getMessageSerializationFactory()
            .createServiceRequestSerializer(serviceType);
    MessageDeserializer<ResponseType> responseDeserializer =
        (MessageDeserializer<ResponseType>) configuration.getMessageSerializationFactory()
            .createServiceResponseDeserializer(serviceType);
    return node.createServiceClient(definition, requestSerializer, responseDeserializer);
  }

  @Override
  public ServiceIdentifier lookupService(String serviceName) {
    GraphName resolvedServiceName = Ros.newGraphName(resolveName(serviceName));
    try {
      return node.lookupService(resolvedServiceName);
    } catch (RemoteException e) {
      return null;
    } catch (XmlRpcTimeoutException e) {
      // TODO(kwc): change timeout policies
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

  @Override
  public boolean isOk() {
    return node.isRunning() && node.isRegistered();
  }

  @Override
  public void shutdown() {
    node.shutdown();
  }

  @Override
  public URI getMasterUri() {
    return configuration.getMasterUri();
  }

  @Override
  public NodeNameResolver getResolver() {
    return resolver;
  }

  @Override
  public ParameterTree newParameterTree() {
    return node.createParameterTree(resolver);
  }

  @Override
  public URI getUri() {
    return node.getUri();
  }

  @Override
  public boolean isRegistered() {
    return node.isRegistered();
  }

  @Override
  public boolean isRegistrationOk() {
    return node.isRegistrationOk();
  }
}
