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
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;
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
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class Node implements Namespace {

  private final NodeContext context;
  private final NameResolver resolver;
  private final GraphName nodeName;
  private final org.ros.internal.node.Node node;
  private final RosoutLogger log;
  private final TimeProvider timeProvider;

  /**
   * @param name Node name. This identifies this node to the rest of the ROS
   *        graph.
   * @param context
   * @throws RosNameException If node name is invalid.
   * @throws RosInitException
   */
  public Node(String name, NodeContext context) throws RosNameException, RosInitException {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(name);
    this.context = context;
    NameResolver parentResolver = context.getParentResolver();
    nodeName = new GraphName(parentResolver.resolveName(name));
    resolver = parentResolver.createResolver(nodeName.toString());

    // TODO (kwc): implement simulated time.
    timeProvider = new WallclockProvider();
    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);

    try {
      if (context.getHostName().equals("localhost") || context.getHostName().startsWith("127.0.0.")) {
        // If we are advertising as localhost, explicitly bind to loopback-only.
        // NOTE: technically 127.0.0.0/8 is loopback, not 127.0.0.1/24.
        node =
            org.ros.internal.node.Node.createPrivate(nodeName.toString(),
                context.getRosMasterUri(), context.getXmlRpcPort(), context.getTcpRosPort());
      } else {
        node =
            org.ros.internal.node.Node.createPublic(nodeName.toString(), context.getRosMasterUri(),
                context.getXmlRpcPort(), context.getTcpRosPort());
      }
    } catch (Exception e) {
      throw new RosInitException(e);
    }

    // TODO(damonkohler): Move the creation and management of the RosoutLogger
    // into the internal.Node class.
    Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher =
        createPublisher("/rosout", org.ros.message.rosgraph_msgs.Log.class);
    log.setRosoutPublisher(rosoutPublisher);
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topicName,
      Class<MessageType> messageClass) throws RosInitException, RosNameException {
    try {
      String resolvedTopicName = resolveName(topicName);
      Message m = messageClass.newInstance();
      TopicDefinition topicDefinition =
          new TopicDefinition(resolvedTopicName, MessageDefinition.createFromMessage(m));
      org.ros.internal.node.topic.Publisher<MessageType> publisherImpl =
          node.createPublisher(topicDefinition, messageClass);
      return new Publisher<MessageType>(resolveName(topicName), messageClass, publisherImpl);
    } catch (RosNameException e) {
      throw e;
    } catch (Exception e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topicName,
      final MessageListener<MessageType> callback, Class<MessageType> messageClass)
      throws RosInitException, RosNameException {
    try {
      Message m = messageClass.newInstance();
      String resolvedTopicName = resolveName(topicName);
      TopicDefinition topicDefinition =
          new TopicDefinition(resolvedTopicName, MessageDefinition.createFromMessage(m));
      org.ros.internal.node.topic.Subscriber<MessageType> subscriberImpl =
          node.createSubscriber(topicDefinition, messageClass);

      // Add the callback to the impl.
      subscriberImpl.addMessageListener(callback);
      // Create the user-facing Subscriber handle. This is little more than a
      // lightweight wrapper around the internal implementation so that we can
      // track callback references.
      Subscriber<MessageType> subscriber =
          new Subscriber<MessageType>(resolvedTopicName, callback, messageClass, subscriberImpl);
      return subscriber;
    } catch (RosNameException e) {
      throw e;
    } catch (Exception e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <RequestMessageType extends Message> ServiceServer<RequestMessageType> createServiceServer(
      ServiceDefinition serviceDefinition, Class<RequestMessageType> requestMessageClass,
      ServiceResponseBuilder<RequestMessageType> responseBuilder) throws Exception {
    return node.createServiceServer(serviceDefinition, requestMessageClass, responseBuilder);
  }

  @Override
  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, Class<ResponseMessageType> responseMessageClass) {
    return node.createServiceClient(serviceIdentifier, responseMessageClass);
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
  public String resolveName(String name) throws RosNameException {
    return resolver.resolveName(getName(), name);
  }

  public void stop() {
    node.stop();
  }

  /**
   * @return {@link URI} of ROS Master that this node is attached to.
   */
  public URI getMasterUri() {
    return context.getRosMasterUri();
  }

  @Override
  public NameResolver getResolver() {
    return resolver;
  }

  @Override
  public ParameterClient createParameterClient() {
    // TODO(kwc) allow user to specify an additional namespace when creating a
    // parameter client.
    try {
      return ParameterClient.createFromNode(this);
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

}
