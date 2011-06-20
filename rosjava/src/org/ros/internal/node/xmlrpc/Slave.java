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

import org.ros.internal.exception.RemoteException;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Slave extends Node {

  /**
   * Retrieve transport/topic statistics.
   * 
   * @param callerId
   *          ROS caller ID.
   * @return stats in the form of <br>
   *         [publishStats, subscribeStats, serviceStats]
   *         <p>
   *         where <br>
   *         publishStats: [[topicName, messageDataSent, pubConnectionData]...]
   *         <br>
   *         subscribeStats: [[topicName, subConnectionData]...] <br>
   *         serviceStats: (proposed) [numRequests, bytesReceived, bytesSent] <br>
   * 
   *         pubConnectionData: [connectionId, bytesSent, numSent, connected] <br>
   *         subConnectionData: [connectionId, bytesReceived, dropEstimate,
   *         connected] <br>
   *         dropEstimate: -1 if no estimate.
   * @throws XmlRpcTimeoutException
   * @throws RemoteException
   */
  public List<Object> getBusStats(String callerId) throws XmlRpcTimeoutException, RemoteException;

  /**
   * Retrieve transport/topic connection information.
   * 
   * @param callerId
   *          ROS caller ID.
   * @return busInfo in the form of:<br>
   *         [[connectionId1, destinationId1, direction1, transport1, topic1,
   *         connected1]... ]
   *         <p>
   *         connectionId is defined by the node and is opaque. destinationId is
   *         the XMLRPC URI of the destination.
   *         <p>
   *         direction is one of 'i', 'o', or 'b' (in, out, both).
   *         <p>
   *         transport is the transport type (e.g. 'TCPROS'). topic is the topic
   *         name.
   *         <p>
   *         connected1 indicates connection status. Note that this field is
   *         only provided by slaves written in Python at the moment (cf.
   *         rospy/masterslave.py in _TopicImpl.get_stats_info() vs.
   *         roscpp/publication.cpp in Publication::getInfo()).
   * @throws XmlRpcTimeoutException
   * @throws RemoteException
   */
  public List<Object> getBusInfo(String callerId) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> getMasterUri(String callerId) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> shutdown(String callerId, String message) throws XmlRpcTimeoutException,
      RemoteException;

  public List<Object> getPid(String callerId) throws XmlRpcTimeoutException, RemoteException;

  public List<Object> getSubscriptions(String callerId) throws XmlRpcTimeoutException,
      RemoteException;

  /**
   * Retrieve a list of topics that this node publishes.
   * 
   * @param callerId
   *          ROS caller ID.
   * @return topicList is a list of topics published by this node and is of the
   *         form [ [topic1, topicType1]...[topicN, topicTypeN]]]
   */
  public List<Object> getPublications(String callerId) throws XmlRpcTimeoutException,
      RemoteException;

  public List<Object> paramUpdate(String callerId, String parameterKey, Object parameterValue)
      throws XmlRpcTimeoutException, RemoteException;

  public List<Object> publisherUpdate(String callerId, String topic, Object[] publishers)
      throws XmlRpcTimeoutException, RemoteException;

  /**
   * Publisher node API method called by a subscriber node. This requests that
   * source allocate a channel for communication. Subscriber provides a list of
   * desired protocols for communication. Publisher returns the selected
   * protocol along with any additional params required for establishing
   * connection. For example, for a TCP/IP-based connection, the source node may
   * return a port number of TCP/IP server.
   * 
   * @param callerId
   *          ROS caller ID
   * @param topic
   *          topic name
   * @param protocols
   *          list of desired protocols for communication in order of preference
   * @return protocolParams or empty list if there are no compatible protocols
   */
  public List<Object> requestTopic(String callerId, String topic, Object[] protocols)
      throws XmlRpcTimeoutException, RemoteException;

}
