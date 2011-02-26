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

import org.ros.exceptions.RosNameException;

import org.ros.MessageListener;

import org.ros.Publisher;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.message.Message;

/**
 * Interface for the ROS namespace spec.
 * 
 */
public interface Namespace {

  /**
   * @param <MessageType>
   *          The message type to create the publisher for
   * @param topic_name
   *          the topic name, will be pushed down under this namespace unless
   *          '/' is prepended
   * @param clazz
   *          the Class object used to deal with type eraser
   * @return A handle to a publisher that may be used to publish messages of
   *         type MessageType
   * @throws RosInitException May throw if the system is not in a proper state.
   * @throws RosNameException May throw if the name is invalid.
   */
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topic_name,
      Class<MessageType> clazz) throws RosInitException, RosNameException;

  /**
   * 
   * @param <MessageType>
   *          The message type to create the Subscriber for.
   * @param topic_name
   *          The topic name to be subscribed to. This may be "bar" "/foo/bar"
   *          "~my" and will be auto resolved.
   * @param callback
   *          The callback to be registered to this subscription. This will be
   *          called asynchronously any time that a message is published on the
   *          topic.
   * @param clazz
   *          The class of the message type that is being published on the
   *          topic.
   * @return A handle to a Subscriber that may be used to subscribe messages of
   *         type MessageType.
   * @throws RosInitException
   *           The subscriber may fail if the Ros system has not been
   *           initialized or other wackyness. TODO specify exceptions that
   *           might be thrown here.
   * @throws RosNameException May throw if the topic name is invalid.
   */
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topic_name,
      MessageListener<MessageType> callback, Class<MessageType> clazz) throws RosInitException, RosNameException;

  /**
   * @return The fully resolved name of this namespace, e.g. "/foo/bar/boop".
   */
  public String getName();

  /**
   * Resolve the given name, using ROS conventions, into a full ROS namespace
   * name. Will be relative to the current namespace unless prepended by a "/"
   * 
   * @param name
   *          The name to resolve.
   * @return Fully resolved ros namespace name.
   * @throws RosNameException 
   */
  public String resolveName(String name) throws RosNameException;
}