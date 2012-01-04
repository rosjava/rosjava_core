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
import org.ros.concurrent.CancellableLoop;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.xmlrpc.Master;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializationFactory;
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
import java.util.concurrent.ExecutorService;

/**
 * A node in the ROS graph.
 * 
 * <p>
 * Nodes serve as factories for:
 * 
 * <ul>
 * <li>{@link Publisher}</li>
 * <li>{@link Subscriber}</li>
 * <li>{@link ServiceServer}</li>
 * <li>{@link ServiceClient}</li>
 * <li>{@link ParameterTree}</li>
 * </ul>
 * 
 * @author khughes@google.com (Keith M. Hughes)
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
   * Shut the node down.
   */
  void shutdown();

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
   * @param <T>
   *          the message type to create the publisher for
   * @param topicName
   *          the topic name, will be pushed down under this namespace unless
   *          '/' is prepended.
   * @param messageType
   *          the message data type (e.g. "std_msgs/String")
   * @return a {@link Publisher} for the specified topic
   */
  <T> Publisher<T> newPublisher(GraphName topicName, String messageType);

  /**
   * @see #newPublisher(GraphName, String)
   */
  <T> Publisher<T> newPublisher(String topicName, String messageType);

  /**
   * @param <T>
   *          the message type to create the {@link Subscriber} for
   * @param topicName
   *          the topic name to be subscribed to, this will be auto resolved
   * @param messageType
   *          the message data type (e.g. "std_msgs/String")
   * @return a {@link Subscriber} for the specified topic
   */
  <T> Subscriber<T> newSubscriber(GraphName topicName, String messageType);

  /**
   * @see #newSubscriber(GraphName, String, MessageListener,
   *      Collection<SubscriberListener>)
   */
  <T> Subscriber<T> newSubscriber(String topicName, String messageType);

  /**
   * Create a {@link ServiceServer}.
   * 
   * @param serviceName
   *          the name of the service
   * @param serviceType
   *          the type of the service (e.g. "test_ros/AddTwoInts")
   * @param serviceResponseBuilder
   *          called for every request to build a response
   * @return a {@link ServiceServer}
   */
  <T, S> ServiceServer<T, S> newServiceServer(GraphName serviceName, String serviceType,
      ServiceResponseBuilder<T, S> serviceResponseBuilder);

  /**
   * @see Node#newServiceServer(GraphName, String, ServiceResponseBuilder)
   */
  <T, S> ServiceServer<T, S> newServiceServer(String serviceName, String serviceType,
      ServiceResponseBuilder<T, S> serviceResponseBuilder);

  /**
   * Create a {@link ServiceClient}.
   * 
   * @param serviceName
   *          the name of the service
   * @param serviceType
   *          the type of the service (e.g. "test_ros/AddTwoInts")
   * @return a {@link ServiceClient}
   * @throws ServiceNotFoundException
   *           thrown if no matching service could be found
   */
  <T, S> ServiceClient<T, S> newServiceClient(GraphName serviceName, String serviceType)
      throws ServiceNotFoundException;

  /**
   * @see #newServiceClient(GraphName, String)
   */
  <T, S> ServiceClient<T, S> newServiceClient(String serviceName, String serviceType)
      throws ServiceNotFoundException;

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
   * @return the {@link MessageSerializationFactory} used by this node
   */
  MessageSerializationFactory getMessageSerializationFactory();

  /**
   * @return the {@link MessageFactory} used by this node
   */
  MessageFactory getMessageFactory();

  /**
   * Add a new {@link NodeListener} to the {@link Node}.
   * 
   * @param listener
   *          the {@link NodeListener} to add
   */
  void addListener(NodeListener listener);

  /**
   * Remove a {@link NodeListener} from the {@link Node}.
   * 
   * <p>
   * If the given {@link NodeListener} is not registered, this has no effect.
   * 
   * @param listener
   *          the {@link NodeListener} to remove
   */
  void removeListener(NodeListener listener);

  /**
   * Executes a {@link CancellableLoop} using the {@link Node}'s
   * {@link ExecutorService}. The {@link CancellableLoop} will be canceled when
   * the {@link Node} starts shutting down.
   * 
   * <p>
   * Any blocking calls executed in the provided {@link CancellableLoop} can
   * potentially delay {@link Node} shutdown and should be avoided.
   * 
   * @param cancellableLoop
   *          the {@link CancellableLoop} to execute
   */
  void execute(CancellableLoop cancellableLoop);
}
