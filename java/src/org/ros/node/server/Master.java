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

package org.ros.node.server;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.node.xmlrpc.MasterImpl;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Master extends Node {
  
  public Master(String hostname, int port) {
    super(hostname, port);
  }

  public void start() throws XmlRpcException, IOException {
    super.start(org.ros.node.xmlrpc.MasterImpl.class, new MasterImpl(this));
  }

  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi) {
    return null;
  }

  public List<Object> unregisterService(String callerId, String service, String serviceApi) {
    return null;
  }

  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) {
    return null;
  }

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi) {
    return null;
  }

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param callerId
   *          ROS caller ID
   * @param topic
   *          Fully-qualified name of topic to register.
   * @param topicType
   *          Datatype for topic. Must be a package-resource name, i.e. the .msg
   *          name.
   * @param callerApi
   *          API URI of publisher to register.
   * @return List of current subscribers of topic in the form of XML-RPC URIs.
   */
  public List<URL> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) {
    return null;
  }

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi) {
    return null;
  }

  public List<Object> lookupNode(String callerId, String nodeName) {
    return null;
  }

  public List<Object> getPublishedTopics(String callerId, String subgraph) {
    return null;
  }

  public List<Object> getSystemState(String callerId) {
    return null;
  }

  public List<Object> getUri(String callerId) {
    return null;
  }

  public List<Object> lookupService(String callerId, String service) {
    return null;
  }
}