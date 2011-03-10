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
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.xmlrpc.SlaveImpl;
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;
import org.ros.message.Message;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveServer extends NodeServer {

  private final String name;
  private final MasterClient master;
  private final Map<String, Publisher> publishers;
  private final Map<String, Subscriber<?>> subscribers;

  private static final List<PublisherIdentifier> buildPublisherIdentifierList(
      Collection<URI> publisherUriList, TopicDefinition topicDefinition) {
    List<PublisherIdentifier> publishers = Lists.newArrayList();
    for (URI uri : publisherUriList) {
      SlaveIdentifier slaveIdentifier = new SlaveIdentifier(SlaveIdentifier.UNKNOWN_NAME, uri);
      publishers.add(new PublisherIdentifier(slaveIdentifier, topicDefinition));
    }
    return publishers;
  }

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

  public void addPublisher(Publisher publisher) throws MalformedURLException, URISyntaxException,
      RemoteException {
    String topic = publisher.getTopicName();
    publishers.put(topic, publisher);
    master.registerPublisher(toSlaveIdentifier(), publisher);
  }

  /**
   * Informs this {@link SlaveServer} of a new {@link Subscriber}. If there are multiple
   * {@link Subscriber}s for the same topic, this should only be called for the first.
   * 
   * <p>This call blocks on a call to the ROS master.
   * 
   * @param subscriber
   * @return List of current publisher XML-RPC slave URIs for topic.
   * @throws IOException
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public List<PublisherIdentifier> addSubscriber(Subscriber<?> subscriber) throws IOException,
      URISyntaxException, RemoteException {
    subscribers.put(subscriber.getTopicName(), subscriber);
    Response<List<URI>> response = master.registerSubscriber(toSlaveIdentifier(), subscriber);
    List<PublisherIdentifier> publishers =
        buildPublisherIdentifierList(response.getResult(), subscriber.getTopicDefinition());
    subscriber.updatePublishers(publishers);
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
    master.registerService(toSlaveIdentifier(), server);
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    // For each publication and subscription (alive and dead):
    // ((connection_id, destination_caller_id, direction, transport, topic_name,
    // connected)*)
    // TODO(kwc): returning empty list right now to keep debugging tools happy
    return new ArrayList<Object>();
  }

  public URI getMasterUri(String callerId) {
    return master.getRemoteUri();
  }

  public void shutdown(String callerId, String message) {
    super.shutdown();
  }

  /**
   * @param callerId
   * @return PID of node process
   * @throws UnsupportedOperationException If PID cannot be retrieved on this
   *         platform.
   */
  public Integer getPid(String callerId) {
    // kwc: java has no standard way of getting pid, apparently. This is the
    // recommended solution, but this needs to be tested on Android.
    // MF.getName() returns '1234@localhost'.
    String mxName = ManagementFactory.getRuntimeMXBean().getName();
    int idx = mxName.indexOf('@');
    if (idx > 0) {
      try {
        return Integer.parseInt(mxName.substring(0, idx));
      } catch (NumberFormatException e) {
        // handled by exception below
      }
    }
    throw new UnsupportedOperationException("cannot retrieve pid on this platform");
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

  public void publisherUpdate(String callerId, String topic, Collection<URI> publisherUris) {
    if (subscribers.containsKey(topic)) {
      Subscriber<?> subscriber = subscribers.get(topic);
      TopicDefinition topicDefinition = subscriber.getTopicDefinition();
      List<PublisherIdentifier> pubIdentifiers =
          buildPublisherIdentifierList(publisherUris, topicDefinition);
      subscriber.updatePublishers(pubIdentifiers);
    }
  }

  // TODO(damonkohler): Support multiple publishers for a particular topic.
  public ProtocolDescription requestTopic(String topic, Collection<String> protocols)
      throws ServerException {
    if (!publishers.containsKey(topic)) {
      throw new ServerException("No publishers for topic " + topic);
    }
    for (String protocol : protocols) {
      if (ProtocolNames.SUPPORTED.contains(protocol)) {
        return new TcpRosProtocolDescription(publishers.get(topic).getAddress());
      }
    }
    throw new ServerException("No supported protocols specified.");
  }

  /**
   * @return a {@link SlaveIdentifier} for this {@link SlaveServer}
   * @throws MalformedURLException
   * @throws URISyntaxException
   */
  public SlaveIdentifier toSlaveIdentifier() throws URISyntaxException, MalformedURLException {
    return new SlaveIdentifier(name, getUri());
  }

}
