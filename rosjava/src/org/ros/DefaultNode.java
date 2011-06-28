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

package org.ros;

import com.google.common.base.Preconditions;


import org.ros.exception.RosRuntimeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.exception.RosInitException;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.message.MessageDefinition;
import org.ros.internal.message.ServiceMessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RosoutLogger;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.xmlrpc.Master;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.internal.time.TimeProvider;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.MessageDefinitionFactory;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.message.ServiceMessageDefinitionFactory;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * A default implementation of a {@link Node}.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNode implements Node {

  /**
   * Configuration this node was created with.
   */
  private final NodeConfiguration configuration;
  private final NodeNameResolver resolver;
  private final GraphName nodeName;
  private final org.ros.internal.node.Node node;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;

  /**
   * @param name
   *          Node name. This identifies this node to the rest of the ROS graph.
   * @param configuration
   *          Configuration parameters for the node.
   * @throws RosInitException
   */
  public DefaultNode(String name, NodeConfiguration configuration) throws RosInitException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(name);
    this.configuration = configuration;
    NameResolver parentResolver = configuration.getParentResolver();
    String baseName;
    String nodeNameOverride = configuration.getNodeNameOverride();
    if (nodeNameOverride != null) {
      baseName = nodeNameOverride;
    } else {
      baseName = name;
    }
    nodeName = new GraphName(NameResolver.join(parentResolver.getNamespace(), baseName));
    resolver = NodeNameResolver.create(parentResolver, nodeName);

    // TODO(kwc): Implement simulated time.
    // TODO(damonkohler): Move TimeProvider into NodeConfiguration.
    timeProvider = new WallclockProvider();

    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);

    try {
      Preconditions.checkNotNull(configuration.getHost());
      InetAddress host = InetAddressFactory.createFromHostString(configuration.getHost());
      if (host.isLoopbackAddress()) {
        node =
            org.ros.internal.node.Node.createPrivate(nodeName, configuration.getMasterUri(),
                configuration.getXmlRpcPort(), configuration.getTcpRosPort());
      } else {
        node =
            org.ros.internal.node.Node.createPublic(nodeName, configuration.getMasterUri(),
                configuration.getHost(), configuration.getXmlRpcPort(),
                configuration.getTcpRosPort());
      }
    } catch (Exception e) {
      throw new RosInitException(e);
    }

    // TODO(damonkohler): Move the creation and management of the RosoutLogger
    // into the internal.Node class.
    Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher =
        createPublisher("/rosout", "rosgraph_msgs/Log");
    log.setRosoutPublisher(rosoutPublisher);
  }

  /**
   * @param <MessageType>
   *          The message type to create the publisher for
   * @param topicName
   *          The topic name, will be pushed down under this namespace unless
   *          '/' is prepended.
   * @param messageClass
   *          The Class object of the topic message type.
   * @return A handle to a publisher that may be used to publish messages of
   *         type MessageType
   * @throws RosInitException
   *           May throw if the system is not in a proper state.
   */
  @Override
  public <MessageType> Publisher<MessageType> createPublisher(String topicName, String messageType) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(new GraphName(resolvedTopicName), messageDefinition);
    org.ros.MessageSerializer<MessageType> serializer =
        configuration.getMessageSerializationFactory().createSerializer(messageType);
    return node.createPublisher(topicDefinition, serializer);
  }

  /**
   * @param <MessageType>
   *          The message type to create the Subscriber for.
   * @param topicName
   *          The topic name to be subscribed to. This may be "bar" "/foo/bar"
   *          "~my" and will be auto resolved.
   * @param callback
   *          The callback to be registered to this subscription. This will be
   *          called asynchronously any time that a message is published on the
   *          topic.
   * @param messageClass
   *          The class of the message type that is being published on the
   *          topic.
   * @return A handle to a Subscriber that may be used to subscribe messages of
   *         type MessageType.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> Subscriber<MessageType> createSubscriber(String topicName,
      String messageType, final MessageListener<MessageType> listener) {
    String resolvedTopicName = resolveName(topicName);
    MessageDefinition messageDefinition = MessageDefinitionFactory.createFromString(messageType);
    TopicDefinition topicDefinition =
        TopicDefinition.create(new GraphName(resolvedTopicName), messageDefinition);
    MessageDeserializer<MessageType> deserializer =
        (MessageDeserializer<MessageType>) configuration.getMessageSerializationFactory()
            .createDeserializer(messageType);
    Subscriber<MessageType> subscriber = node.createSubscriber(topicDefinition, deserializer);
    subscriber.addMessageListener(listener);
    return subscriber;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> createServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    // TODO(damonkohler): It's rather non-obvious that the URI will be created
    // later on the fly.
    ServiceIdentifier identifier = new ServiceIdentifier(new GraphName(serviceName), null);
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
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> createServiceClient(
      String serviceName, String serviceType) {
    ServiceIdentifier identifier = lookupService(serviceName);
    if (identifier == null) {
      throw new RosRuntimeException("No such service.");
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
    GraphName resolvedServiceName = new GraphName(resolveName(serviceName));
    try {
      return node.lookupService(resolvedServiceName);
    } catch (RemoteException e) {
      return null;
    } catch (XmlRpcTimeoutException e) {
      // TODO(kwc): change timeout policies
      return null;
    }
  }

  /**
   * Returns the current time. In ROS, time can be wallclock (actual) or
   * simulated, so it is important to use {@code Node.getCurrentTime()} instead
   * of using the standard Java routines for determining the current time.
   * 
   * @return the current time
   */
  @Override
  public Time getCurrentTime() {
    return timeProvider.getCurrentTime();
  }

  /**
   * @return The fully resolved name of this namespace, e.g. "/foo/bar/boop".
   */
  @Override
  public String getName() {
    return nodeName.toString();
  }

  /**
   * @return Logger for this node, which will also perform logging to /rosout.
   */
  @Override
  public Log getLog() {
    return log;
  }

  /**
   * Resolve the given name, using ROS conventions, into a full ROS namespace
   * name. Will be relative to the current namespace unless the name is global.
   * 
   * @param name
   *          The name to resolve.
   * @return Fully resolved ros namespace name.
   */
  @Override
  public String resolveName(String name) {
    return resolver.resolveName(name);
  }

  /**
   * Is the node ok?
   * 
   * <p>
   * "ok" means that the node is in the running state and registered with the
   * master.
   * 
   * @return True if the node is OK, false otherwise.
   */
  @Override
  public boolean isOk() {
    return node.isRunning() && node.isRegistered();
  }

  /**
   * Shut the node down.
   */
  @Override
  public void shutdown() {
    node.shutdown();
  }

  /**
   * @return {@link URI} of {@link Master} that this node is attached to.
   */
  @Override
  public URI getMasterUri() {
    return configuration.getMasterUri();
  }

  /**
   * @return {@link NameResolver} for this namespace.
   */
  @Override
  public NodeNameResolver getResolver() {
    return resolver;
  }

  /**
   * Create a {@link ParameterClient} to query and set parameters on the ROS
   * parameter server.
   * 
   * @return {@link ParameterClient} with {@link NameResolver} in this
   *         namespace.
   */
  @Override
  public ParameterClient createParameterClient() {
    try {
      return ParameterClient.create(getName(), getMasterUri(), resolver);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return the {@link URI} of this {@link Node}
   */
  @Override
  public URI getUri() {
    return node.getUri();
  }

  /**
   * Poll for whether or not Node is current fully registered with
   * {@link MasterServer}.
   * 
   * @return true if Node is fully registered with {@link MasterServer}.
   *         {@code isRegistered()} can go to false if new publisher or
   *         subscribers are created.
   */
  @Override
  public boolean isRegistered() {
    return node.isRegistered();
  }

  /**
   * Poll for whether or not registration with the {@link MasterServer} is
   * proceeding normally. If this returns false, it means that the
   * {@link MasterServer} is out of contact or is misbehaving.
   * 
   * @return true if Node registrations are proceeding normally with
   *         {@link MasterServer}.
   */
  @Override
  public boolean isRegistrationOk() {
    return node.isRegistrationOk();
  }
}
