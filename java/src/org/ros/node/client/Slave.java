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

package org.ros.node.client;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ros.node.Response;
import org.ros.transport.ProtocolDescription;
import org.ros.transport.ProtocolNames;
import org.ros.transport.tcp.TcpRosProtocolDescription;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Slave extends Node<org.ros.node.xmlrpc.Slave> {

  public Slave(URL url) {
    super(url, org.ros.node.xmlrpc.Slave.class);
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    throw new UnsupportedOperationException();
  }

  public Response<URL> getMasterUri(String callerId) throws MalformedURLException {
    List<Object> response = node.getMasterUri(callerId);
    return new Response<URL>((Integer) response.get(0), (String) response.get(1), new URL(
        (String) response.get(2)));
  }

  public List<Object> shutdown(String callerId, String message) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getPid(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getSubscriptions(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getPublications(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String callerId, String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  public Response<ProtocolDescription> requestTopic(String callerId, String topic,
      Set<String> requestedProtocols) {
    List<Object> response =
        node.requestTopic(callerId, topic, new Object[][] {requestedProtocols.toArray()});
    List<Object> protocolParameters = Arrays.asList((Object[]) response.get(2));
    Preconditions.checkState(protocolParameters.size() == 3);
    Preconditions.checkState(protocolParameters.get(0).equals(ProtocolNames.TCPROS));
    InetSocketAddress address =
        InetSocketAddress.createUnresolved((String) protocolParameters.get(1),
            (Integer) protocolParameters.get(2));
    return new Response<ProtocolDescription>((Integer) response.get(0), (String) response.get(1),
        new TcpRosProtocolDescription(address));
  }
}
