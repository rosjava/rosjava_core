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

package org.ros.namespace;

import org.ros.MessageListener;
import org.ros.ParameterClient;
import org.ros.Publisher;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.message.Message;

/**
 * Interface for the ROS namespace specification.
 * 
 * @see "http://www.ros.org/wiki/Names"
 */
public interface Namespace {

  /**
   * This is the global namespace, think root "/".
   */
  public final static String GLOBAL_NS = "/";

  /**
   * @param <MessageType> The message type to create the publisher for
   * @param topic_name the topic name, will be pushed down under this namespace
   *        unless '/' is prepended
   * @param clazz the Class object used to deal with type eraser
   * @return A handle to a publisher that may be used to publish messages of
   *         type MessageType
   * @throws RosInitException May throw if the system is not in a proper state.
   */
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topic_name,
      Class<MessageType> clazz) throws RosInitException;

  /**
   * @param <MessageType> The message type to create the Subscriber for.
   * @param topic_name The topic name to be subscribed to. This may be "bar"
   *        "/foo/bar" "~my" and will be auto resolved.
   * @param callback The callback to be registered to this subscription. This
   *        will be called asynchronously any time that a message is published
   *        on the topic.
   * @param clazz The class of the message type that is being published on the
   *        topic.
   * @return A handle to a Subscriber that may be used to subscribe messages of
   *         type MessageType.
   * @throws RosInitException The subscriber may fail if the Ros system has not
   *         been initialized or other wackyness. TODO specify exceptions that
   *         might be thrown here.
   */
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topic_name,
      MessageListener<MessageType> callback, Class<MessageType> clazz) throws RosInitException;

  /**
   * Create a {@link ParameterClient} to query and set parameters on the ROS
   * parameter server.
   * 
   * @return {@link ParameterClient} with {@link NameResolver} in this
   *         namespace.
   */
  public ParameterClient createParameterClient();

  /**
   * @return The fully resolved name of this namespace, e.g. "/foo/bar/boop".
   */
  public String getName();

  /**
   * Resolve the given name, using ROS conventions, into a full ROS namespace
   * name. Will be relative to the current namespace unless the name is global.
   * 
   * @param name The name to resolve.
   * @return Fully resolved ros namespace name.
   * @throws RosNameException
   */
  public String resolveName(String name) throws RosNameException;

  /**
   * @return {@link NameResolver} for this namespace.
   */
  public NameResolver getResolver();

  /**
   * @param serviceDefinition
   * @param requestMessageClass
   * @param responseBuilder
   * @return
   * @throws Exception
   */
  public <RequestMessageType extends Message> ServiceServer<RequestMessageType> createServiceServer(
      ServiceDefinition serviceDefinition, Class<RequestMessageType> requestMessageClass,
      ServiceResponseBuilder<RequestMessageType> responseBuilder) throws Exception;

  /**
   * @param serviceIdentifier
   * @param responseMessageClass
   * @return
   */
  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, Class<ResponseMessageType> responseMessageClass);

}
