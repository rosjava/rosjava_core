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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.ros.internal.node.Response;
import org.ros.internal.node.StatusCode;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

  public Response<URI> getMasterUri() throws URISyntaxException {
    List<Object> response = node.getMasterUri(nodeName);
    return new Response<URI>((Integer) response.get(0), (String) response.get(1), new URI(
        (String) response.get(2)));
  }

  public List<Object> shutdown(String message) {
    throw new UnsupportedOperationException();
  }

  public Response<Integer> getPid() {
    List<Object> response = node.getPid(nodeName);
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  public Response<List<TopicDefinition>> getSubscriptions() {
    List<Object> response = node.getSubscriptions(nodeName);
    List<TopicDefinition> descriptions = Lists.newArrayList();
    List<Object> topics = Arrays.asList((Object[]) response.get(2));
    for (Object topic : topics) {
      String name = (String) ((Object[]) topic)[0];
      String type = (String) ((Object[]) topic)[1];
      descriptions.add(new TopicDefinition(name, MessageDefinition.createMessageDefinition(type)));
    }
    return new Response<List<TopicDefinition>>((Integer) response.get(0), (String) response.get(1),
        descriptions);
  }

  public Response<List<TopicDefinition>> getPublications() {
    List<Object> response = node.getPublications(nodeName);
    List<TopicDefinition> descriptions = Lists.newArrayList();
    List<Object> topics = Arrays.asList((Object[]) response.get(2));
    for (Object topic : topics) {
      String name = (String) ((Object[]) topic)[0];
      String type = (String) ((Object[]) topic)[1];
      descriptions.add(new TopicDefinition(name, MessageDefinition.createMessageDefinition(type)));
    }
    return new Response<List<TopicDefinition>>((Integer) response.get(0), (String) response.get(1),
        descriptions);
  }

  public List<Object> paramUpdate(String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  public Response<ProtocolDescription> requestTopic(String topic, Collection<String> requestedProtocols) {
    List<Object> response = node.requestTopic(nodeName, topic,
        new Object[][] { requestedProtocols.toArray() });
    List<Object> protocolParameters = Arrays.asList((Object[]) response.get(2));
    Preconditions.checkState(protocolParameters.size() == 3);
    Preconditions.checkState(protocolParameters.get(0).equals(ProtocolNames.TCPROS));
    InetSocketAddress address = InetSocketAddress.createUnresolved(
        (String) protocolParameters.get(1), (Integer) protocolParameters.get(2));
    return new Response<ProtocolDescription>((Integer) response.get(0), (String) response.get(1),
        new TcpRosProtocolDescription(address));
  }

}
