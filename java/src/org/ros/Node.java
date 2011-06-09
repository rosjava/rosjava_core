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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.exception.RosInitException;
import org.ros.exception.RosNameException;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.message.MessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RosoutLogger;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.xmlrpc.Master;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.internal.time.TimeProvider;
import org.ros.internal.time.WallclockProvider;
import org.ros.message.Message;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.message.Service;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class Node {

  private final NodeConfiguration configuration;
  private final NodeNameResolver resolver;
  private final GraphName nodeName;
  private final org.ros.internal.node.Node node;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;
  private final Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher;
  private final MessageSerializerFactory<Message> messageSerializerFactory;

  private final class PregeneratedCodeMessageSerializerFactory implements
      MessageSerializerFactory<Message> {
    @Override
    public <MessageType extends Message> org.ros.MessageSerializer<MessageType> create() {
      return new MessageSerializer<MessageType>();
    }
  }

  /**
   * @param name
   *          Node name. This identifies this node to the rest of the ROS graph.
   * @param configuration
   * @throws RosInitException
   */
  public Node(String name, NodeConfiguration configuration) throws RosInitException {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(name);
    messageSerializerFactory = new PregeneratedCodeMessageSerializerFactory();
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

    // TODO (kwc): implement simulated time.
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
    rosoutPublisher = createPublisher("/rosout", org.ros.message.rosgraph_msgs.Log.class);
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
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topicName,
      Class<MessageType> messageClass) throws RosInitException {
    try {
      String resolvedTopicName = resolveName(topicName);
      Message message = messageClass.newInstance();
      TopicDefinition topicDefinition =
          TopicDefinition.create(new GraphName(resolvedTopicName), MessageDefinition.create(
          message.getDataType(), message.getMessageDefinition(), message.getMD5Sum()));
      org.ros.internal.node.topic.Publisher<MessageType> publisherImpl =
          node.createPublisher(topicDefinition, messageSerializerFactory.<MessageType>create());
      return new Publisher<MessageType>(resolveName(topicName), messageClass, publisherImpl);
    } catch (Exception e) {
      throw new RosInitException(e);
    }
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
   * @throws RosInitException
   *           The subscriber may fail if the Ros system has not been
   *           initialized or other wackyness. TODO specify exceptions that
   *           might be thrown here.
   */
  public <MessageType> Subscriber<MessageType> createSubscriber(String topicName,
      final MessageListener<MessageType> callback, Class<MessageType> messageClass)
      throws RosInitException {
    try {
      String resolvedTopicName = resolveName(topicName);
      Message message = (Message) messageClass.newInstance();
      TopicDefinition topicDefinition =
          TopicDefinition.create(new GraphName(resolvedTopicName), MessageDefinition.create(
          message.getDataType(), message.getMessageDefinition(), message.getMD5Sum()));
      org.ros.internal.node.topic.Subscriber<MessageType> subscriber =
          node.createSubscriber(topicDefinition, messageClass,
              new MessageDeserializer<MessageType>(messageClass));
      subscriber.addMessageListener(callback);
      return new Subscriber<MessageType>(resolvedTopicName, callback, messageClass, subscriber);
    } catch (Exception e) {
      throw new RosInitException(e);
    }
  }

  /**
   * Create a {@link ParameterClient} to query and set parameters on the ROS
   * parameter server.
   * 
   * @return {@link ParameterClient} with {@link NameResolver} in this
   *         namespace.
   */
  public <RequestType, ResponseType> ServiceServer createServiceServer(
      ServiceDefinition serviceDefinition,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) throws Exception {
    return node.createServiceServer(serviceDefinition, responseBuilder);
  }

  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType>
      createServiceClient(ServiceIdentifier serviceIdentifier,
          Class<ResponseMessageType> responseMessageClass) {
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
  public Time getCurrentTime() {
    return timeProvider.getCurrentTime();
  }

  /**
   * @return The fully resolved name of this namespace, e.g. "/foo/bar/boop".
   */
  public String getName() {
    return nodeName.toString();
  }

  /**
   * @return Logger for this node, which will also perform logging to /rosout.
   */
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
   * @throws RosNameException
   */
  public String resolveName(String name) {
    return resolver.resolveName(name);
  }

  public void shutdown() {
    rosoutPublisher.shutdown();
    node.shutdown();
  }

  /**
   * @return {@link URI} of {@link Master} that this node is attached to.
   */
  public URI getMasterUri() {
    return configuration.getMasterUri();
  }

  /**
   * @return {@link NameResolver} for this namespace.
   */
   public NodeNameResolver getResolver() {
    return resolver;
  }

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
