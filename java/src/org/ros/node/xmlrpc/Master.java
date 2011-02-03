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

import java.util.List;

public interface Master extends Node {

  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi);

  public List<Object> unregisterService(String callerId, String service, String serviceApi);

  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi);

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi);

  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi);

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi);

  public List<Object> lookupNode(String callerId, String nodeName);

  public List<Object> getPublishedTopics(String callerId, String subgraph);

  public List<Object> getSystemState(String callerId);

  public List<Object> getUri(String callerId);

  public List<Object> lookupService(String callerId, String service);

}