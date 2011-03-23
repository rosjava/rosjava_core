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

import com.google.common.collect.Lists;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.node.xmlrpc.SlaveImpl;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;
import org.ros.internal.transport.tcp.TcpRosServer;
import org.ros.message.Message;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveServer extends NodeServer {

  private final GraphName nodeName;
  private final MasterClient masterClient;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;
  private final TcpRosServer tcpRosServer;

  private static List<PublisherIdentifier> buildPublisherIdentifierList(
      Collection<URI> publisherUriList, TopicDefinition topicDefinition) {
    List<PublisherIdentifier> publishers = Lists.newArrayList();
    for (URI uri : publisherUriList) {
      SlaveIdentifier slaveIdentifier = SlaveIdentifier.createAnonymous(uri);
      publishers.add(new PublisherIdentifier(slaveIdentifier, topicDefinition));
    }
    return publishers;
  }

  public SlaveServer(GraphName nodeName, BindAddress xmlRpcServerAddress,
      AdvertiseAddress advertiseAddress, MasterClient master, TopicManager topicManager,
      ServiceManager serviceManager, TcpRosServer tcpRosServer) {
    super(xmlRpcServerAddress, advertiseAddress);
    this.nodeName = nodeName;
    this.masterClient = master;
    this.topicManager = topicManager;
    this.serviceManager = serviceManager;
    this.tcpRosServer = tcpRosServer;
  }

  /**
   * Start the XML-RPC server. This start() routine requires that the
   * {@link TcpRosServer} is initialized first so that the slave server returns
   * correct information when topics are requested.
   * 
   * @throws XmlRpcException
   * @throws IOException
   * @throws URISyntaxException
   */
  public void start() throws XmlRpcException, IOException, URISyntaxException {
    super.start(org.ros.internal.node.xmlrpc.SlaveImpl.class, new SlaveImpl(this));
    tcpRosServer.start();
  }

  @Override
  public void shutdown() {
    super.shutdown();
  }

  public void addPublisher(Publisher<?> publisher) throws MalformedURLException,
      URISyntaxException, RemoteException {
    topicManager.putPublisher(publisher.getTopicName(), publisher);
    masterClient.registerPublisher(publisher.toPublisherIdentifier(toSlaveIdentifier()));
  }

  /**
   * Informs this {@link SlaveServer} of a new {@link Subscriber}. If there are
   * multiple {@link Subscriber}s for the same topic, this should only be called
   * for the first.
   * 
   * <p>
   * This call blocks on remote call to the {@link MasterServer}.
   * 
   * @param subscriber
   * @return List of current publisher XML-RPC slave URIs for topic.
   * @throws IOException
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public List<PublisherIdentifier> addSubscriber(Subscriber<?> subscriber) throws IOException,
      URISyntaxException, RemoteException {
    topicManager.putSubscriber(subscriber.getTopicName(), subscriber);
    Response<List<URI>> response = masterClient.registerSubscriber(toSlaveIdentifier(), subscriber);
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
    serviceManager.putServiceServer(server.getName().toString(), server);
    masterClient.registerService(toSlaveIdentifier(), server);
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    // For each publication and subscription (alive and dead):
    // ((connection_id, destination_caller_id, direction, transport, topic_name,
    // connected)*)
    // TODO(kwc): returning empty list right now to keep debugging tools happy
    return Lists.newArrayList();
  }

  public URI getMasterUri(String callerId) {
    return masterClient.getRemoteUri();
  }

  public void shutdown(String callerId, String message) {
    super.shutdown();
    tcpRosServer.shutdown();
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
    return topicManager.getSubscribers();
  }

  public List<Publisher<?>> getPublications() {
    return topicManager.getPublishers();
  }

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public void publisherUpdate(String callerId, String topicName, Collection<URI> publisherUris) {
    if (topicManager.hasSubscriber(topicName)) {
      Subscriber<? extends Message> subscriber = topicManager.getSubscriber(topicName);
      TopicDefinition topicDefinition = subscriber.getTopicDefinition();
      List<PublisherIdentifier> pubIdentifiers =
          buildPublisherIdentifierList(publisherUris, topicDefinition);
      subscriber.updatePublishers(pubIdentifiers);
    }
  }

  public ProtocolDescription requestTopic(String topicName, Collection<String> protocols)
      throws ServerException {
    try {
      // Canonicalize topic name.
      topicName = new GraphName(topicName).toGlobal();
    } catch (RosNameException e) {
      throw new ServerException("Invalid topic name.");
    }
    if (!topicManager.hasPublisher(topicName)) {
      throw new ServerException("No publishers for topic: " + topicName);
    }
    for (String protocol : protocols) {
      if (protocol.equals(ProtocolNames.TCPROS)) {
        try {
          return new TcpRosProtocolDescription(tcpRosServer.getAddress());
        } catch (Exception e) {
          throw new ServerException(e);
        }
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
    return new SlaveIdentifier(nodeName, getUri());
  }

}
