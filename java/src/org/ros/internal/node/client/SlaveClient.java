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

package org.ros.internal.node.client;

import org.ros.internal.node.RemoteException;
import org.ros.internal.node.response.IntegerResultFactory;
import org.ros.internal.node.response.ProtocolDescriptionResultFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.TopicDefinitionListResultFactory;
import org.ros.internal.node.response.UriResultFactory;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveClient extends NodeClient<org.ros.internal.node.xmlrpc.Slave> {

  private final String nodeName;

  public SlaveClient(String nodeName, URI uri) throws MalformedURLException {
    super(uri, org.ros.internal.node.xmlrpc.Slave.class);
    this.nodeName = nodeName;
  }

  public List<Object> getBusStats() {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo() {
    throw new UnsupportedOperationException();
  }

  public Response<URI> getMasterUri() throws RemoteException {
    return Response.fromList(node.getMasterUri(nodeName), new UriResultFactory());
  }

  public List<Object> shutdown(String message) {
    throw new UnsupportedOperationException();
  }

  public Response<Integer> getPid() throws RemoteException {
    return Response.fromList(node.getPid(nodeName), new IntegerResultFactory());
  }

  public Response<List<TopicDefinition>> getSubscriptions() throws RemoteException {
    return Response.fromList(node.getSubscriptions(nodeName),
        new TopicDefinitionListResultFactory());
  }

  public Response<List<TopicDefinition>> getPublications() throws RemoteException {
    return Response
        .fromList(node.getPublications(nodeName), new TopicDefinitionListResultFactory());
  }

  public List<Object> paramUpdate(String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  public Response<ProtocolDescription> requestTopic(String topic,
      Collection<String> requestedProtocols) throws RemoteException {
    return Response.fromList(
        node.requestTopic(nodeName, topic, new Object[][] {requestedProtocols.toArray()}),
        new ProtocolDescriptionResultFactory());
  }

}
