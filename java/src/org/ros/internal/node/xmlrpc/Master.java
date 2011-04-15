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

package org.ros.internal.node.xmlrpc;

import org.ros.internal.node.RemoteException;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Master extends Node {

  /**
   * Register the caller as a provider of the specified service.
   * 
   * @param callerId
   *          ROS caller ID
   * @param service
   *          Fully-qualified name of service
   * @param serviceApi
   *          XML-RPC URI of caller node
   * @param callerApi
   * @return ignore
   * @throws URISyntaxException
   */
  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi) throws URISyntaxException, XmlRpcTimeoutException, RemoteException;

  public List<Object> unregisterService(String callerId, String service, String serviceApi)
      throws XmlRpcTimeoutException, RemoteException;

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * 
   * @param callerId
   *          ROS caller ID
   * @param topic
   *          Fully-qualified name of topic
   * @param topicType
   *          topic type, must be a package-resource name, i.e. the .msg name
   * @param callerApi
   *          API URI of subscriber to register. Will be used for new publisher
   *          notifications
   * @return publishers as a list of XMLRPC API URIs for nodes currently
   *         publishing the specified topic
   * @throws URISyntaxException
   */
  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) throws URISyntaxException, XmlRpcTimeoutException, RemoteException;

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi)
      throws XmlRpcTimeoutException, RemoteException;

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param callerId
   *          ROS caller ID
   * @param topic
   *          fully-qualified name of topic to register
   * @param topicType
   *          topic type, must be a package-resource name, i.e. the .msg name.
   * @param callerApi
   *          API URI of publisher to register
   * @return list of current subscribers of topic in the form of XML-RPC URIs
   * @throws URISyntaxException
   */
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) throws URISyntaxException, RemoteException, XmlRpcTimeoutException;

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi)
      throws XmlRpcTimeoutException, RemoteException;

  public List<Object> lookupNode(String callerId, String nodeName) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> getPublishedTopics(String callerId, String subgraph)
      throws XmlRpcTimeoutException, RemoteException;

  public List<Object> getSystemState(String callerId) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> getUri(String callerId) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> lookupService(String callerId, String service) throws XmlRpcTimeoutException, RemoteException;

}
