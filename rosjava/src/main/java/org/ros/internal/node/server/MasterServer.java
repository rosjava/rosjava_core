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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.xmlrpc.MasterXmlRpcEndpointImpl;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The master server in the ROS graph.
 * 
 * <p>
 * This server is used, for example,to maintain lists of publishers and
 * subscribers for topics.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterServer extends NodeServer {

  /**
   * The subscriber message type given when the subscriber chooses not to commit
   * upon registration.
   */
  private static final String PUBLISHER_MESSAGE_TYPE_NOT_GIVEN = "*";

  private static final boolean DEBUG = false;

  private static final String MASTER_GRAPH_NAME = "/master";

  private static final Log log = LogFactory.getLog(MasterServer.class);

  /**
   * A map from a node name to the slave identifier for the node.
   */
  private final Map<GraphName, SlaveIdentifier> slaves;

  /**
   * A map from the name of the service to the service identifier.
   */
  private final Map<GraphName, ServiceIdentifier> services;

  /**
   * A map from topic name to publisher identifier.
   */
  private final Multimap<GraphName, PublisherIdentifier> publishers;

  /**
   * A map from topic name to subscriber identifier.
   */
  private final Multimap<GraphName, SubscriberIdentifier> subscribers;

  /**
   * A map from topic name to its most recently registered message type.
   */
  private final Map<GraphName, String> topicMessageTypes;

  /**
   * A map from topic name to whether a publisher registered the type. {code
   * true} if a publisher did it, {@code false} if a subscriber did it,
   * {@code null} if not registered yet.
   */
  private final Map<GraphName, Boolean> isPublisherTopicMessageTypes;

  /**
   * Name of the master in the graph.
   */
  private final GraphName masterName;

  public MasterServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress) {
    super(bindAddress, advertiseAddress);
    slaves = Maps.newConcurrentMap();
    services = Maps.newConcurrentMap();
    publishers =
        Multimaps.synchronizedMultimap(HashMultimap.<GraphName, PublisherIdentifier>create());
    subscribers =
        Multimaps.synchronizedMultimap(HashMultimap.<GraphName, SubscriberIdentifier>create());
    masterName = new GraphName(MASTER_GRAPH_NAME);

    topicMessageTypes = Maps.newConcurrentMap();

    // This map will always be protected by a lock on topicMessageTypes.
    isPublisherTopicMessageTypes = Maps.newHashMap();
  }

  /**
   * Start the master server.
   */
  public void start() {
    if (DEBUG) {
      log.info("Starting master server");
    }
    super.start(MasterXmlRpcEndpointImpl.class, new MasterXmlRpcEndpointImpl(this));
  }

  /**
   * Register a service with the master.
   * 
   * @param serviceIdentifier
   *          the identifier of the service
   * 
   */
  public void registerService(ServiceIdentifier serviceIdentifier) {
    services.put(serviceIdentifier.getName(), serviceIdentifier);
  }

  /**
   * Unregister a service from the master.
   * 
   * @param serviceIdentifier
   *          the identifier of the service
   * 
   * @return {@code true} if the server had been registered
   */
  public boolean unregisterService(ServiceIdentifier serviceIdentifier) {
    GraphName serviceName = serviceIdentifier.getName();
    return services.remove(serviceName) != null;
  }

  /**
   * Register a new slave for a publisher or subscriber to the master.
   * 
   * @param slaveIdentifier
   *          identifier for the slave to register
   */
  private void addSlave(SlaveIdentifier slaveIdentifier) {
    Preconditions.checkNotNull(slaveIdentifier);

    GraphName slaveName = slaveIdentifier.getNodeName();
    Preconditions.checkState(
        slaves.get(slaveName) == null || slaves.get(slaveName).equals(slaveIdentifier),
        "Failed to add slave: " + slaveIdentifier + "\nExisting slave: " + slaves.get(slaveName));
    slaves.put(slaveName, slaveIdentifier);
  }

  /**
   * Something has happened to the publishers for a topic. Tell every subscriber
   * about the current set of publishers.
   * 
   * @param topicName
   *          the topic name for the publisher
   */
  private void publisherUpdate(GraphName topicName) {
    if (DEBUG) {
      log.info("Publisher update: " + topicName);
    }
    List<URI> publisherUris = Lists.newArrayList();
    synchronized (publishers) {
      for (PublisherIdentifier publisherIdentifier : publishers.get(topicName)) {
        publisherUris.add(publisherIdentifier.getSlaveUri());
      }
    }
    for (SlaveIdentifier slaveIdentifier : slaves.values()) {
      SlaveClient client = new SlaveClient(masterName, slaveIdentifier.getUri());
      client.publisherUpdate(topicName, publisherUris);
    }
  }

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * @param subscriberIdentifier
   *          the identifier for the subscriber
   * @param topicMessageType
   *          the message type of the topic
   * @return Publishers is a list of XMLRPC API URIs for nodes currently
   *         publishing the specified topic.
   */
  public List<PublisherIdentifier> registerSubscriber(SubscriberIdentifier subscriberIdentifier,
      String topicMessageType) {
    if (DEBUG) {
      log.info("Registering subscriber: " + subscriberIdentifier);
    }

    registerTopicMessageType(subscriberIdentifier.getTopicName(), topicMessageType, false);
    subscribers.put(subscriberIdentifier.getTopicName(), subscriberIdentifier);
    addSlave(subscriberIdentifier.getSlaveIdentifier());
    synchronized (publishers) {
      return ImmutableList.copyOf(publishers.get(subscriberIdentifier.getTopicName()));
    }
  }

  /**
   * Unregister a subscriber from the master.
   * 
   * @param subscriberIdentifier
   *          the identifier for the subscriber to be unregistered
   * 
   * @return {@code true} if the subscriber had been registered.
   */
  public boolean unregisterSubscriber(SubscriberIdentifier subscriberIdentifier) {
    if (DEBUG) {
      log.info("Unregistering subscriber: " + subscriberIdentifier);
    }
    return subscribers.remove(subscriberIdentifier.getTopicName(), subscriberIdentifier);
  }

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param publisherIdentifier
   *          identifier for the publisher
   * @param topicMessageType
   *          the message type of the topic
   * 
   * @return List of current subscribers of topic in the form of XML-RPC URIs.
   */
  public List<SubscriberIdentifier> registerPublisher(PublisherIdentifier publisherIdentifier,
      String topicMessageType) {
    if (DEBUG) {
      log.info("Registering publisher: " + publisherIdentifier);
    }

    registerTopicMessageType(publisherIdentifier.getTopicName(), topicMessageType, false);
    publishers.put(publisherIdentifier.getTopicName(), publisherIdentifier);
    addSlave(publisherIdentifier.getSlaveIdentifier());
    publisherUpdate(publisherIdentifier.getTopicName());
    synchronized (subscribers) {
      return ImmutableList.copyOf(subscribers.get(publisherIdentifier.getTopicName()));
    }
  }

  /**
   * Unregister a publisher from the master.
   * 
   * @param publisherIdentifier
   *          the identifier for the publisher to be unregistered
   * 
   * @return {@code true} if the publisher had been registered.
   */
  public boolean unregisterPublisher(PublisherIdentifier publisherIdentifier) {
    if (DEBUG) {
      log.info("Unregistering publisher: " + publisherIdentifier);
    }
    return publishers.remove(publisherIdentifier.getTopicName(), publisherIdentifier);
  }

  /**
   * Returns a {@link SlaveIdentifier} for the node with the given name. This
   * API is for looking information about publishers and subscribers. Use
   * lookupService instead to lookup ROS-RPC URIs.
   * 
   * @param slaveName
   *          name of node to lookup
   * @return a {@link SlaveIdentifier} for the node with the given name, or
   *         {@code null} if there is nn nameo slave with the give
   */
  public SlaveIdentifier lookupNode(GraphName slaveName) {
    return slaves.get(slaveName);
  }

  /**
   * Get all publishers which are currently registered with the master.
   * 
   * @return a list of {@link PublisherIdentifier} instances for each registered
   *         publisher
   */
  public List<PublisherIdentifier> getRegisteredPublishers() {
    synchronized (publishers) {
      return ImmutableList.copyOf(publishers.values());
    }
  }

  /**
   * Get all subscribers which are currently registered with the master.
   * 
   * @return a list of {@link SubscriberIdentifier} instances for each
   *         registered subscriber
   */
  public List<SubscriberIdentifier> getRegisteredSubscribers() {
    synchronized (subscribers) {
      return ImmutableList.copyOf(subscribers.values());
    }
  }

  /**
   * Get a list of all topic types.
   * 
   * @param calledId
   *          ID of the caller
   * 
   * @return A list of the form [[topic 1 name, topic 1 message type], [topic 2
   *         name, topic 2 message type], ...]
   */
  public List<List<String>> getTopicTypes(GraphName calledId) {
    List<List<String>> retval = Lists.newArrayList();

    synchronized (topicMessageTypes) {
      for (Entry<GraphName, String> entry : topicMessageTypes.entrySet()) {
        retval.add(Lists.newArrayList(entry.getKey().toString(), entry.getValue()));
      }
    }

    return retval;
  }

  /**
   * Get the state of the ROS graph.
   * 
   * <p>
   * This includes information about publishers, subscribers, and services.
   * 
   * @return TODO(keith): Fill in.
   */
  public List<Object> getSystemState() {
    List<Object> retval = Lists.newArrayList();

    retval.add(getSystemStatePublishers());
    retval.add(getSystemStateSubscribers());
    retval.add(getSystemStateServices());

    return retval;
  }

  /**
   * Get the system state for publishers.
   * 
   * @return a list of the form [ [topic1,
   *         [topic1Publisher1...topic1PublisherN]] ... ] where the
   *         topicPublisherI instances are node names
   */
  private List<Object> getSystemStatePublishers() {
    List<Object> retval = Lists.newArrayList();

    synchronized (publishers) {
      for (GraphName name : publishers.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        retval.add(topicInfo);

        topicInfo.add(name.toString());
        List<String> publist = Lists.newArrayList();
        topicInfo.add(publist);

        for (PublisherIdentifier identifier : publishers.get(name)) {
          publist.add(identifier.getSlaveIdentifier().getNodeName().toString());
        }
      }
    }

    return retval;
  }

  /**
   * Get the system state for subscribers.
   * 
   * @return a list of the form [ [topic1,
   *         [topic1Subscriber1...topic1SubscriberN]] ... ] where the
   *         topicSubscriberI instances are node names
   */
  private List<Object> getSystemStateSubscribers() {
    List<Object> retval = Lists.newArrayList();

    synchronized (subscribers) {
      for (GraphName name : subscribers.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        retval.add(topicInfo);

        topicInfo.add(name.toString());
        List<Object> sublist = Lists.newArrayList();
        topicInfo.add(sublist);

        for (SubscriberIdentifier identifier : subscribers.get(name)) {
          sublist.add(identifier.getSlaveIdentifier().getNodeName().toString());
        }
      }
    }

    return retval;
  }

  /**
   * Get the system state for services.
   * 
   * @return
   */
  private List<Object> getSystemStateServices() {
    // services is of the form
    // [ [service1, [serviceProvider1...serviceProviderN]] ... ]
    // where the serviceProviderI instances are node names
    List<Object> retval = Lists.newArrayList();

    synchronized (services) {
      // TODO(keith): Could just iterate over .entries(). Why isn't this a
      // Multimap?
      for (GraphName serviceName : services.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        retval.add(topicInfo);

        topicInfo.add(serviceName.toString());
        List<Object> providerList = Lists.newArrayList();
        topicInfo.add(providerList);

        ServiceIdentifier identifier = services.get(serviceName);
        providerList.add(identifier.getName().toString());
      }
    }

    return retval;
  }

  /**
   * Lookup the provider of a particular service.
   * 
   * @param service
   *          Fully-qualified name of service
   * @return service URI that provides address and port of the service. Fails if
   *         there is no provider.
   */
  public ServiceIdentifier lookupService(GraphName service) {
    return services.get(service);
  }

  /**
   * Get a list of all topics published for the give subgraph.
   * 
   * @param caller
   *          name of the caller
   * 
   * @param subgraph
   *          subgraph containing the requested topics, relative to caller
   * 
   * @return A list of lists, where the contained lists contain, in order, topic
   *         name and topic type.
   */
  public List<Object> getPublishedTopics(GraphName caller, GraphName subgraph) {

    // TODO(keith): Use to filter topics.
    GraphName resolvedSubgraph = caller.join(subgraph);

    Set<GraphName> topicNames = Sets.newHashSet();
    for (PublisherIdentifier publisher : publishers.values()) {
      topicNames.add(publisher.getTopicName());
    }

    List<Object> retval = Lists.newArrayList();

    for (GraphName topicName : topicNames) {
      retval.add(Lists.newArrayList(topicName.toString(), topicMessageTypes.get(topicName)));
    }

    return retval;
  }

  /**
   * Register the type of a topic.
   * 
   * @param topicName
   *          name of the topic
   * @param topicMessageType
   *          the message type of the topic, subscribers can give a message type
   *          of {code *}.
   * @param isPublisher
   *          {code true} is a publisher is doing the registration,
   *          {@code false} if a subscriber is doing the registration
   */
  private void registerTopicMessageType(GraphName topicName, String topicMessageType,
      boolean isPublisher) {
    // The most recent association of topic name to message type wins.
    // However, subscription associations are always trumped by publisher
    // associations.
    synchronized (topicMessageTypes) {
      if (isPublisher) {
        // Publishers always trump
        topicMessageTypes.put(topicName, topicMessageType);
        isPublisherTopicMessageTypes.put(topicName, Boolean.TRUE);
      } else {
        // Is a subscriber.

        if (!PUBLISHER_MESSAGE_TYPE_NOT_GIVEN.equals(topicMessageType)) {
          if (topicMessageTypes.containsKey(topicName)) {
            // if has only been subscribers giving the type, register it.
            if (!isPublisherTopicMessageTypes.get(topicName)) {
              topicMessageTypes.put(topicName, topicMessageType);
            }

          } else {
            // Not registered yet, so no worry about trumping
            topicMessageTypes.put(topicName, topicMessageType);
            isPublisherTopicMessageTypes.put(topicName, Boolean.FALSE);
          }
        }
      }
    }
  }
  
}
