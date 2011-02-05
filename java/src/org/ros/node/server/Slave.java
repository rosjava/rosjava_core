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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.node.RemoteException;
import org.ros.node.Response;
import org.ros.node.xmlrpc.SlaveImpl;
import org.ros.topic.Publisher;
import org.ros.topic.Subscriber;
import org.ros.transport.ProtocolDescription;
import org.ros.transport.ProtocolNames;
import org.ros.transport.TcpRosDescription;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Slave extends Node {

  private final String name;
  private final org.ros.node.client.Master master;
  private final Map<String, Publisher> publishers;
  private final Map<String, Subscriber<?>> subscribers;

  private URL masterUrl;

  public Slave(String name, org.ros.node.client.Master master, String hostname, int port) {
    super(hostname, port);
    this.name = name;
    this.master = master;
    publishers = Maps.newConcurrentMap();
    subscribers = Maps.newConcurrentMap();
  }

  public void start() throws XmlRpcException, IOException {
    super.start(org.ros.node.xmlrpc.SlaveImpl.class, new SlaveImpl(this));
  }

  public void addPublisher(Publisher publisher) throws MalformedURLException, RemoteException {
    String topic = publisher.getTopicName();
    publishers.put(topic, publisher);
    Response.checkOk(master.registerPublisher(name, publisher, getAddress()));
  }

  public void addSubscriber(Subscriber<?> subscriber) throws RemoteException, IOException {
    String topic = subscriber.getTopicName();
    subscribers.put(topic, subscriber);
    Response<List<URL>> response = Response.checkOk(master.registerSubscriber(name, topic,
        subscriber.getTopicType(), getAddress()));
    // TODO(damonkohler): Provide possibility for choosing a publisher and
    // handling the case in which there are no publishers.
    Preconditions.checkState(response.getValue().size() > 0);
    org.ros.node.client.Slave slave = new org.ros.node.client.Slave(response.getValue().get(0));
    Response<ProtocolDescription> requestTopicResponse = Response.checkOk(slave.requestTopic(name,
        topic, Sets.newHashSet(ProtocolNames.TCPROS)));
    subscriber.start(requestTopicResponse.getValue().getAddress());
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    throw new UnsupportedOperationException();
  }

  public void setMasterUri(URL url) {
    masterUrl = url;
  }

  public URL getMasterUri(String callerId) {
    return masterUrl;
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

  public ProtocolDescription requestTopic(String topic, Set<String> protocols) {
    Preconditions.checkArgument(publishers.containsKey(topic));
    // TODO(damonkohler): Pull out list of supported protocols.
    Preconditions.checkArgument(protocols.contains(ProtocolNames.TCPROS));
    return new TcpRosDescription(publishers.get(topic).getAddress());
  }
}
