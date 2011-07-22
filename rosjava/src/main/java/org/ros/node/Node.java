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

package org.ros.node;

import org.apache.commons.logging.Log;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.xmlrpc.Master;
import org.ros.message.MessageListener;
import org.ros.message.Service;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.net.URI;

/**
 * A node in the ROS graph.
 * 
 * <p>
 * Nodes provide for communication with the ROS master. They also serve as
 * factories for:
 * 
 * <ul>
 * <li>{@link Publisher}</li>,
 * <li>{@link Subscriber}</li>
 * <li>{@link ServiceServer}</li>
 * <li>{@link ServiceClient}</li>
 * <li>{@link ParameterTree}</li>
 * </ul>
 * 
 * <p>
 * Lifetime of any objects created from a node are controlled by the creating
 * node.
 * 
 * @author Keith M. Hughes
 * @since Jun 20, 2011
 */
public interface Node {

  /**
   * @return the fully resolved name of this {@link Node}, e.g. "/foo/bar/boop"
   */
  GraphName getName();

  /**
   * Resolve the given name, using ROS conventions, into a full ROS namespace
   * name. Will be relative to the current namespace unless the name is global.
   * 
   * @param name
   *          the name to resolve
   * @return fully resolved ros namespace name
   */
  GraphName resolveName(GraphName name);

  /**
   * @see #resolveName(GraphName)
   */
  GraphName resolveName(String name);

  /**
   * @return {@link DefaultNameResolver} for this namespace.
   */
  NodeNameResolver getResolver();

  /**
   * @return the {@link URI} of this {@link Node}
   */
  URI getUri();

  /**
   * Is the node ok?
   * 
   * <p>
   * "ok" means that the node is in the running state and registered with the
   * master.
   * 
   * @return True if the node is OK, false otherwise.
   */
  boolean isOk();

  /**
   * Shut the node down.
   */
  void shutdown();

  /**
   * Poll for whether or not Node is current fully registered with
   * {@link MasterServer}.
   * 
   * @return true if Node is fully registered with {@link MasterServer}.
   *         {@code isRegistered()} can go to false if new publisher or
   *         subscribers are created.
   */
  boolean isRegistered();

  /**
   * Poll for whether or not registration with the {@link MasterServer} is
   * proceeding normally. If this returns false, it means that the
   * {@link MasterServer} is out of contact or is misbehaving.
   * 
   * @return true if Node registrations are proceeding normally with
   *         {@link MasterServer}.
   */
  boolean isRegistrationOk();

  /**
   * @return {@link URI} of {@link Master} that this node is attached to.
   */
  URI getMasterUri();

  /**
   * Returns the current time. In ROS, time can be wallclock (actual) or
   * simulated, so it is important to use {@code Node.getCurrentTime()} instead
   * of using the standard Java routines for determining the current time.
   * 
   * @return the current time
   */
  Time getCurrentTime();

  /**
   * @return Logger for this node, which will also perform logging to /rosout.
   */
  Log getLog();

  /**
   * @param <MessageType>
   *          the message type to create the publisher for
   * @param topicName
   *          the topic name, will be pushed down under this namespace unless
   *          '/' is prepended.
   * @param messageType
   *          the message data type (e.g. "std_msgs/String")
   * @return a {@link Publisher} for the specified topic
   */
  <MessageType> Publisher<MessageType> newPublisher(GraphName topicName, String messageType);

  /**
   * @see #newPublisher(GraphName, String)
   */
  <MessageType> Publisher<MessageType> newPublisher(String topicName, String messageType);

  /**
   * @param <MessageType>
   *          the message type to create the {@link Subscriber} for
   * @param topicName
   *          the topic name to be subscribed to, this will be auto resolved
   * @param messageType
   *          the message data type (e.g. "std_msgs/String")
   * @param listener
   *          the {@link MessageListener} to be added to this {@link Subscriber}
   *          , will be called asynchronously any time that a message is
   *          published on the specified topic
   * @return a {@link Subscriber} for the specified topic
   */
  <MessageType> Subscriber<MessageType> newSubscriber(GraphName topicName, String messageType,
      MessageListener<MessageType> listener);

  /**
   * @see #newSubscriber(GraphName, String, MessageListener)
   */
  <MessageType> Subscriber<MessageType> newSubscriber(String topicName, String messageType,
      MessageListener<MessageType> listener);

  /**
   * Create a {@link ServiceServer}.
   * 
   * @param <RequestType>
   *          type for the request
   * @param <ResponseType>
   *          type for the response
   * @param serviceName
   *          the name of the service
   * @param serviceType
   *          the type of the service (e.g. "test_ros/AddTwoInts")
   * @param responseBuilder
   *          called for every request to build a response
   * @return a {@link ServiceServer}
   */
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      GraphName serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder);

  /**
   * @see #newServiceServer(GraphName, String, ServiceResponseBuilder)
   */
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> newServiceServer(
      String serviceName, String serviceType,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder);

  /**
   * Create a {@link ServiceClient}.
   * 
   * @param <RequestType>
   *          type for the request
   * @param <ResponseType>
   *          type for the response
   * @param serviceName
   *          the name of the service
   * @param serviceType
   *          the type of the service (e.g. "test_ros/AddTwoInts")
   * @return a {@link ServiceClient}
   * @throws ServiceNotFoundException
   *           thrown if no matching service could be found
   */
  <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      GraphName serviceName, String serviceType) throws ServiceNotFoundException;

  /**
   * @see #newServiceClient(GraphName, String)
   */
  <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> newServiceClient(
      String serviceName, String serviceType) throws ServiceNotFoundException;

  /**
   * @param serviceName
   *          the name of the service to lookup
   * @return {@link URI} of the {@Service} provider or null if the
   *         {@link Service} does not exist
   */
  URI lookupService(GraphName serviceName);

  /**
   * @see #lookupService(GraphName)
   */
  URI lookupService(String serviceName);

  /**
   * Create a {@link ParameterTree} to query and set parameters on the ROS
   * parameter server.
   * 
   * @return {@link ParameterTree} with {@link NameResolver} in this namespace.
   */
  ParameterTree newParameterTree();

  /**
   * Is the node running?
   * 
   * <p>
   * A running node may not be fully initialized yet, it is either in the
   * process of starting up or is running.
   * 
   * @return True if the node is running, false otherwise.
   */
  boolean isRunning();

}
