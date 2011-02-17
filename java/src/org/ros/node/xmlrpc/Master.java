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

package org.ros.node.xmlrpc;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Master extends Node {

  /**
   * Register the caller as a provider of the specified service.
   * 
   * @param callerId ROS caller ID
   * @param service Fully-qualified name of service
   * @param serviceApi XML-RPC URI of caller node
   * @param callerApi
   * @return ignore
   * @throws MalformedURLException 
   */
  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi) throws MalformedURLException;

  public List<Object> unregisterService(String callerId, String service, String serviceApi);

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * 
   * @param callerId ROS caller ID
   * @param topic Fully-qualified name of topic
   * @param topicType Datatype for topic. Must be a package-resource name, i.e.
   *        the .msg name
   * @param callerApi API URI of subscriber to register. Will be used for new
   *        publisher notifications
   * @return publishers as a list of XMLRPC API URIs for nodes currently
   *         publishing the specified topic
   * @throws MalformedURLException
   */
  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) throws MalformedURLException;

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi);

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param callerId ROS caller ID
   * @param topic Fully-qualified name of topic to register.
   * @param topicType Datatype for topic. Must be a package-resource name, i.e.
   *        the .msg name.
   * @param callerApi API URI of publisher to register.
   * @return List of current subscribers of topic in the form of XMLRPC URIs.
   * @throws MalformedURLException
   */
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) throws MalformedURLException;

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi);

  public List<Object> lookupNode(String callerId, String nodeName);

  public List<Object> getPublishedTopics(String callerId, String subgraph);

  public List<Object> getSystemState(String callerId);

  public List<Object> getUri(String callerId);

  public List<Object> lookupService(String callerId, String service);

}
