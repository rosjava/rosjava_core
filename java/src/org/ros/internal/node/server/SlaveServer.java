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

package org.ros.internal.node.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.Response;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.xmlrpc.SlaveImpl;
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;
import org.ros.message.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveServer extends NodeServer {

  private final String name;
  private final MasterClient master;
  private final Map<String, Publisher> publishers;
  private final Map<String, Subscriber<?>> subscribers;

  public SlaveServer(String name, MasterClient master, String hostname, int port) {
    super(hostname, port);
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(name.startsWith("/"));
    this.name = name;
    this.master = master;
    publishers = Maps.newConcurrentMap();
    subscribers = Maps.newConcurrentMap();
  }

  public void start() throws XmlRpcException, IOException, URISyntaxException {
    super.start(org.ros.internal.node.xmlrpc.SlaveImpl.class, new SlaveImpl(this));
  }

  public void addPublisher(Publisher publisher) throws MalformedURLException, RemoteException,
      URISyntaxException {
    String topic = publisher.getTopicName();
    publishers.put(topic, publisher);
    Response.checkOk(master.registerPublisher(toSlaveIdentifier(), publisher));
  }

  public List<PublisherIdentifier> addSubscriber(Subscriber<?> subscriber) throws RemoteException,
      IOException, URISyntaxException {
    String topic = subscriber.getTopicName();
    subscribers.put(topic, subscriber);
    Response<List<URI>> response =
        Response.checkOk(master.registerSubscriber(toSlaveIdentifier(), subscriber));
    List<PublisherIdentifier> publishers = Lists.newArrayList();
    for (URI uri : response.getResult()) {
      // TODO(damonkohler): What should we supply as the name of this slave?
      // It's not given to us in the response.
      SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/unnamed", uri);
      MessageDefinition messageDefinition =
          MessageDefinition.createMessageDefinition(subscriber.getTopicMessageType());
      TopicDefinition topicDefinition = new TopicDefinition(topic, messageDefinition);
      publishers.add(new PublisherIdentifier(slaveIdentifier, topicDefinition));
    }
    return publishers;
  }

  /**
   * @param server
   * @throws URISyntaxException
   * @throws MalformedURLException
   * @throws RemoteException 
   */
  public void addService(ServiceServer<? extends Message> server) throws URISyntaxException,
      MalformedURLException, RemoteException {
    Response.checkOk(master.registerService(toSlaveIdentifier(), server));
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    throw new UnsupportedOperationException();
  }

  public URI getMasterUri(String callerId) {
    return master.getRemoteUri();
  }

  public void shutdown(String callerId, String message) {
    super.shutdown();
  }

  public List<Object> getPid(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Subscriber<?>> getSubscriptions() {
    return ImmutableList.copyOf(subscribers.values());
  }

  public List<Publisher> getPublications() {
    return ImmutableList.copyOf(publishers.values());
  }

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String callerId, String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  // TODO(damonkohler): Support multiple publishers for a particular topic.
  public ProtocolDescription requestTopic(String topic, Set<String> protocols) {
    Preconditions.checkArgument(publishers.containsKey(topic));
    // TODO(damonkohler): Pull out list of supported protocols.
    Preconditions.checkArgument(protocols.contains(ProtocolNames.TCPROS));
    return new TcpRosProtocolDescription(publishers.get(topic).getAddress());
  }

  /**
   * @return
   * @throws MalformedURLException
   * @throws URISyntaxException
   */
  public SlaveIdentifier toSlaveIdentifier() throws URISyntaxException, MalformedURLException {
    return new SlaveIdentifier(name, getUri());
  }

}
