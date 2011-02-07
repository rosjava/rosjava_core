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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Slave extends Node {

  public List<Object> getBusStats(String callerId);

  public List<Object> getBusInfo(String callerId);

  public List<Object> getMasterUri(String callerId);

  public List<Object> shutdown(String callerId, String message);

  public List<Object> getPid(String callerId);

  public List<Object> getSubscriptions(String callerId);

  public List<Object> getPublications(String callerId);

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue);

  public List<Object> publisherUpdate(String callerId, String topic, Object[] publishers);

  /**
   * Publisher node API method called by a subscriber node. This requests that
   * source allocate a channel for communication. Subscriber provides a list of
   * desired protocols for communication. Publisher returns the selected
   * protocol along with any additional params required for establishing
   * connection. For example, for a TCP/IP-based connection, the source node may
   * return a port number of TCP/IP server.
   * 
   * @param callerId ROS caller ID
   * @param topic topic name
   * @param protocols list of desired protocols for communication in order of
   *        preference
   * @return protocolParams or empty list if there are no compatible protocols
   */
  public List<Object> requestTopic(String callerId, String topic, Object[] protocols);

}
