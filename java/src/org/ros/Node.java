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
import org.apache.xmlrpc.XmlRpcException;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.RosoutLogger;
import org.ros.internal.node.client.TimeProvider;
import org.ros.internal.node.client.WallclockProvider;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.TopicDefinition;
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.namespace.Namespace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class Node implements Namespace {
  /**
   * The node's context for name resolution and possibly other global
   * configuration issues (rosparam)
   */
  private final NodeContext context;
  /** The node's namespace name. */
  private final GraphName nodeName;
  /** Factory for generating internal Publisher and Subscriber instances. */
  private org.ros.internal.node.Node node;

  /**
   * Log for client. This will send messages to /rosout.
   */
  private RosoutLogger log;
  private boolean initialized;
  private TimeProvider timeProvider;

  /**
   * @param name
   * @param context
   * @throws RosNameException
   */
  public Node(String name, NodeContext context) throws RosNameException {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(name);
    this.context = context;
    nodeName = new GraphName(this.context.getResolver().resolveName(name));

    // TODO (kwc): implement simulated time.
    timeProvider = new WallclockProvider();
    // Log for /rosout.
    log = new RosoutLogger(LogFactory.getLog(nodeName.toString()), timeProvider);
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topicName,
      Class<MessageType> messageClass) throws RosInitException, RosNameException {
    if (!initialized || node == null) {
      // kwc: this is not a permanent constraint. In the future, with more state
      // tracking, it is possible to allow Publisher handles to be created
      // before node.init().
      throw new RosInitException("Please call node.init()");
    }
    try {
      String resolvedTopicName = resolveName(topicName);
      Message m = messageClass.newInstance();
      TopicDefinition topicDefinition =
          new TopicDefinition(resolvedTopicName, MessageDefinition.createFromMessage(m));
      org.ros.internal.topic.Publisher<MessageType> publisherImpl =
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

    if (!initialized || node == null) {
      // kwc: this is not a permanent constraint. In the future, with more state
      // tracking, it is possible to allow Publisher handles to be created
      // before node.init().
      throw new RosInitException("Please call node.init()");
    }

    try {
      Message m = messageClass.newInstance();
      String resolvedTopicName = resolveName(topicName);
      TopicDefinition topicDefinition =
          new TopicDefinition(resolvedTopicName, MessageDefinition.createFromMessage(m));
      org.ros.internal.topic.Subscriber<MessageType> subscriberImpl =
          node.createSubscriber(topicDefinition, messageClass);

      // Add the callback to the impl.
      subscriberImpl.addMessageListener(callback);
      // Create the user-facing Subscriber handle. This is little more than a
      // lightweight wrapper around the internal implementation so that we can
      // track callback references.
      Subscriber<MessageType> subscriber =
          new Subscriber<MessageType>(resolvedTopicName, callback, messageClass, subscriberImpl);
      return subscriber;

    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    } catch (RemoteException e) {
      throw new RosInitException(e);
    } catch (InstantiationException e) {
      throw new RosInitException(e);
    } catch (IllegalAccessException e) {
      throw new RosInitException(e);
    }
  }

  /**
   * Provide the current time. In ROS, time can be wallclock (actual) or
   * simulated, so it is important to use currentTime() instead of normal Java
   * routines for determining the current time.
   * 
   * @return Current ROS clock time.
   */
  public Time currentTime() {
    return timeProvider.currentTime();
  }

  @Override
  public String getName() {
    return nodeName.toString();
  }

  /**
   * This starts up a connection with the master.
   * 
   * @throws RosInitException
   */
  public void init() throws RosInitException {
    if (initialized) {
      throw new RosInitException("already initialized");
    }
    try {
      InetSocketAddress tcpRosServerBindAddress;
      if (context.getHostName().equals("localhost") || context.getHostName().startsWith("127.0.0.")) {
        // If we are advertising as localhost, explicitly bind to loopback-only.
        // NOTE: technically 127.0.0.0/8 is loopback, not 127.0.0.1/24.
        tcpRosServerBindAddress =
            new InetSocketAddress(InetAddress.getByName("localhost"), context.getTcpRosPort());
      } else {
        tcpRosServerBindAddress = new InetSocketAddress(context.getTcpRosPort());
      }

      // Create factory and job queue for generating publisher/subscriber impls.
      node =
          new org.ros.internal.node.Node(nodeName.toString(), context.getRosMasterUri(),
              new InetSocketAddress(context.getHostName(), context.getXmlRpcPort()),
              tcpRosServerBindAddress);
      // Explicitly start TCPROS resources for now.
      node.start(context.getHostName());

      initialized = true;

      // initialized must be true to start creating publishers.
      Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher =
          createPublisher("/rosout", org.ros.message.rosgraph_msgs.Log.class);
      log.setRosoutPublisher(rosoutPublisher);

    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (RosNameException e) {
      throw new RosInitException(e);
    } catch (XmlRpcException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    }
  }

  /**
   * @return Logger for this node, which will also perform logging to /rosout.
   */
  public Log getLog() {
    return log;
  }

  @Override
  public String resolveName(String name) throws RosNameException {
    return context.getResolver().resolveName(getName(), name);
  }

  public void shutdown() {
    if (initialized) {
      node.stop();
    }
  }

}
