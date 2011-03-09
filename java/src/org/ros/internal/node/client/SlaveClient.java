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

import org.ros.internal.node.RemoteException;
import org.ros.internal.node.Response;
import org.ros.internal.node.ResultFactory;
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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveClient extends NodeClient<org.ros.internal.node.xmlrpc.Slave> {

  private final class TopicDefinitionListResultFactory
      implements
        ResultFactory<List<TopicDefinition>> {
    @Override
    public List<TopicDefinition> create(Object value) {
      List<TopicDefinition> descriptions = Lists.newArrayList();
      List<Object> topics = Arrays.asList((Object[]) value);
      for (Object topic : topics) {
        String name = (String) ((Object[]) topic)[0];
        String type = (String) ((Object[]) topic)[1];
        descriptions
            .add(new TopicDefinition(name, MessageDefinition.createMessageDefinition(type)));
      }
      return descriptions;
    }
  }

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
    return Response.fromList(node.getMasterUri(nodeName), new ResultFactory<URI>() {
      @Override
      public URI create(Object value) {
        try {
          return new URI((String) value);
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public List<Object> shutdown(String message) {
    throw new UnsupportedOperationException();
  }

  public Response<Integer> getPid() throws RemoteException {
    return Response.fromList(node.getPid(nodeName), new ResultFactory<Integer>() {
      @Override
      public Integer create(Object value) {
        return (Integer) value;
      }
    });
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
        new ResultFactory<ProtocolDescription>() {
          @Override
          public ProtocolDescription create(Object value) {
            List<Object> protocolParameters = Arrays.asList((Object[]) value);
            Preconditions.checkState(protocolParameters.size() == 3);
            Preconditions.checkState(protocolParameters.get(0).equals(ProtocolNames.TCPROS));
            InetSocketAddress address =
                InetSocketAddress.createUnresolved((String) protocolParameters.get(1),
                    (Integer) protocolParameters.get(2));
            return new TcpRosProtocolDescription(address);
          }
        });
  }

}
