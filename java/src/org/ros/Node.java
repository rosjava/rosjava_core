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

import org.ros.internal.node.server.MasterServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.RosoutLogger;
import org.ros.internal.node.client.TimeProvider;
import org.ros.internal.node.client.WallclockProvider;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.MessageDefinition;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.xmlrpc.Master;
import org.ros.message.Message;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.message.Service;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import org.ros.namespace.NodeNameResolver;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
// TODO(kwc): add createNamespace method to enable creation of new Namespace
// handles.
public class Node implements Namespace {

  private final NodeContext context;
  private final NodeNameResolver resolver;
  private final GraphName nodeName;
  private final org.ros.internal.node.Node node;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;
  private Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher;

  /**
   * @param name
   *          Node name. This identifies this node to the rest of the ROS graph.
   * @param context
   * @throws RosInitException
   */
  public Node(String name, NodeContext context) throws RosInitException {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(name);
    this.context = context;
    NameResolver parentResolver = context.getParentResolver();
    String baseName;
    String nodeNameOverride = context.getNodeNameOverride();
    if (nodeNameOverride != null) {
      baseName = nodeNameOverride;
    } else {
      baseName = name;
    }
    nodeName = new GraphName(NameResolver.join(parentResolver.getNamespace(), baseName));
    resolver = NodeNameResolver.create(parentResolver, nodeName);

    // TODO (kwc): implement simulated time.
    timeProvider = new WallclockProvider();
    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);

    try {
      if (context.getHostName() == null) {
        throw new NullPointerException("context.getHostName() cannot be null");
      }
      if (context.getHostName().equals("localhost") || context.getHostName().startsWith("127.0.0.")) {
        // If we are advertising as localhost, explicitly bind to loopback-only.
        // NOTE: technically 127.0.0.0/8 is loopback, not 127.0.0.1/24.
        node = org.ros.internal.node.Node.createPrivate(nodeName, context.getRosMasterUri(),
            context.getXmlRpcPort(), context.getTcpRosPort());
      } else {
        node = org.ros.internal.node.Node.createPublic(nodeName, context.getRosMasterUri(),
            context.getHostName(), context.getXmlRpcPort(), context.getTcpRosPort());
      }
    } catch (Exception e) {
      throw new RosInitException(e);
    }

    // TODO(damonkohler): Move the creation and management of the RosoutLogger
    // into the internal.Node class.
    rosoutPublisher = createPublisher("/rosout", org.ros.message.rosgraph_msgs.Log.class);
    log.setRosoutPublisher(rosoutPublisher);
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topicName,
      Class<MessageType> messageClass) throws RosInitException {
    try {
      String resolvedTopicName = resolveName(topicName);
      Message message = messageClass.newInstance();
      TopicDefinition topicDefinition = new TopicDefinition(new GraphName(resolvedTopicName),
          MessageDefinition.createFromMessage(message));
      org.ros.internal.node.topic.Publisher<MessageType> publisherImpl = node.createPublisher(
          topicDefinition, messageClass, new MessageSerializer<MessageType>());
      return new Publisher<MessageType>(resolveName(topicName), messageClass, publisherImpl);
    } catch (Exception e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topicName,
      final MessageListener<MessageType> callback, Class<MessageType> messageClass)
      throws RosInitException {
    try {
      String resolvedTopicName = resolveName(topicName);
      Message message = messageClass.newInstance();
      TopicDefinition topicDefinition = new TopicDefinition(new GraphName(resolvedTopicName),
          MessageDefinition.createFromMessage(message));
      org.ros.internal.node.topic.Subscriber<MessageType> subscriber = node.createSubscriber(
          topicDefinition, messageClass, new MessageDeserializer<MessageType>(messageClass));
      subscriber.addMessageListener(callback);
      return new Subscriber<MessageType>(resolvedTopicName, callback, messageClass, subscriber);
    } catch (Exception e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <RequestType, ResponseType> ServiceServer createServiceServer(
      ServiceDefinition serviceDefinition,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) throws Exception {
    return node.createServiceServer(serviceDefinition, responseBuilder);
  }

  @Override
  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, Class<ResponseMessageType> responseMessageClass) {
    return node.createServiceClient(serviceIdentifier,
        new MessageDeserializer<ResponseMessageType>(responseMessageClass));
  }

  /**
   * Returns a {@link ServiceIdentifier} for communicating with the current
   * provider of a {@link Service}. Return value is null if no provider can be
   * determined.
   * 
   * @param serviceName
   * @param serviceType
   * @return {@link ServiceIdentifier} of current {@Service} provider
   *         or null if none present.
   */
  public ServiceIdentifier lookupService(String serviceName, Service<?, ?> serviceType) {
    // TODO(kwc) the need for the serviceType is an artifact of the
    // ServiceIdentifier type. I would like to eliminate this need.
    GraphName resolvedServiceName = new GraphName(resolveName(serviceName));
    try {
      return node.lookupService(resolvedServiceName, serviceType);
    } catch (RemoteException e) {
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
  public Time getCurrentTime() {
    return timeProvider.getCurrentTime();
  }

  @Override
  public String getName() {
    return nodeName.toString();
  }

  /**
   * @return Logger for this node, which will also perform logging to /rosout.
   */
  public Log getLog() {
    return log;
  }

  @Override
  public String resolveName(String name) {
    return resolver.resolveName(name);
  }

  public void stop() {
    rosoutPublisher.shutdown();
    node.stop();
  }

  /**
   * @return {@link URI} of {@link Master} that this node is attached to.
   */
  @Override
  public URI getMasterUri() {
    return context.getRosMasterUri();
  }

  @Override
  public NodeNameResolver getResolver() {
    return resolver;
  }

  @Override
  public ParameterClient createParameterClient() {
    try {
      return ParameterClient.createFromNamespace(this);
    } catch (MalformedURLException e) {
      // Convert to unchecked exception as this really shouldn't happen as URL
      // is already validated.
      throw new RuntimeException("MalformedURLException should not have been thrown: " + e);
    }
  }

  /**
   * @return the {@link URI} of this {@link Node}
   */
  public URI getUri() {
    return node.getUri();
  }

  public NodeNamespace createNamespace(String namespace) {
    return new NodeNamespace(this, namespace);
  }

  /**
   * Poll for whether or not Node is current fully registered with
   * {@link MasterServer}.
   * 
   * @return true if Node is fully registered with {@link MasterServer}.
   *         {@code isRegistered()} can go to false if new publisher or
   *         subscribers are created.
   */
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
  public boolean isRegistrationOk() {
    return node.isRegistrationOk();
  }
}
