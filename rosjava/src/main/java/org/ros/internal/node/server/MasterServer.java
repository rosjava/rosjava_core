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
import org.ros.internal.node.topic.Topic;
import org.ros.internal.node.xmlrpc.MasterXmlRpcEndpointImpl;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.service.ServiceServer;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The {@link MasterServer} provides naming and registration services to the
 * rest of the {@link Node}s in the ROS system. It tracks {@link Publisher}s and
 * {@link Subscriber}s to {@link Topic}s as well as {@link ServiceServer}s. The
 * role of the {@link MasterServer} is to enable individual ROS {@link Node}s to
 * locate one another. Once these {@link Node}s have located each other they
 * communicate with each other peer-to-peer.
 * 
 * @see http://www.ros.org/wiki/Master
 * 
 * @author damonkohler@google.com (Damon Kohler)
 * @author khughes@google.com (Keith M. Hughes)
 */
public class MasterServer extends NodeServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(MasterServer.class);

  /**
   * The node name (i.e. the callerId XML-RPC field) used when the
   * {@link MasterServer} contacts a {@link SlaveServer}.
   */
  private static final GraphName MASTER_NODE_NAME = new GraphName("/master");

  /**
   * A {@link Map} from a node name to the {@link SlaveIdentifier} for the
   * {@link Node}.
   */
  private final Map<GraphName, SlaveIdentifier> slaves;

  /**
   * A {@link Map} from the name of the {@link ServiceServer} to the
   * {@link ServiceIdentifier}.
   */
  private final Map<GraphName, ServiceIdentifier> services;

  /**
   * A {@link Map} from {@link Topic} name to {@link PublisherIdentifier}.
   */
  private final Multimap<GraphName, PublisherIdentifier> publishers;

  /**
   * A {@link Map} from {@link Topic} name to {@link SubscriberIdentifier}.
   */
  private final Multimap<GraphName, SubscriberIdentifier> subscribers;

  /**
   * A {@link Map} from {@link Topic} name to its most recently registered
   * message type.
   */
  private final Map<GraphName, String> topicMessageTypes;

  /**
   * A {@link Map} from {@link Topic} name to whether a {@link Publisher}
   * registered the type. {@code true} if a publisher did it, {@code false} if a
   * subscriber did it, {@code null} if not registered yet.
   */
  private final Map<GraphName, Boolean> isPublisherTopicMessageTypes;

  public MasterServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress) {
    super(bindAddress, advertiseAddress);
    slaves = Maps.newConcurrentMap();
    services = Maps.newConcurrentMap();
    publishers =
        Multimaps.synchronizedMultimap(HashMultimap.<GraphName, PublisherIdentifier>create());
    subscribers =
        Multimaps.synchronizedMultimap(HashMultimap.<GraphName, SubscriberIdentifier>create());
    topicMessageTypes = Maps.newConcurrentMap();
    // This map will always be protected by a lock on topicMessageTypes.
    isPublisherTopicMessageTypes = Maps.newHashMap();
  }

  /**
   * Start the master server.
   */
  public void start() {
    if (DEBUG) {
      log.info("Starting master server.");
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
      SlaveClient client = new SlaveClient(MASTER_NODE_NAME, slaveIdentifier.getUri());
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
   * Unregister a {@link Subscriber}.
   * 
   * @param subscriberIdentifier
   *          the identifier for the {@link Subscriber} to be unregistered
   * 
   * @return {@code true} if the {@link Subscriber} had been registered,
   *         {@code false} otherwise
   */
  public boolean unregisterSubscriber(SubscriberIdentifier subscriberIdentifier) {
    if (DEBUG) {
      log.info("Unregistering subscriber: " + subscriberIdentifier);
    }
    return subscribers.remove(subscriberIdentifier.getTopicName(), subscriberIdentifier);
  }

  /**
   * Register the caller as a {@link Publisher} the {@link Topic}.
   * 
   * @param publisherIdentifier
   *          identifier for the {@link Publisher}
   * @param topicMessageType
   *          the message type of the {@link Topic}
   * 
   * @return a {@link List} of the current {@link Subscriber}s to the
   *         {@link Publisher}'s {@link Topic} in the form of XML-RPC
   *         {@link URI}s for each {@link Subscriber}'s {@link SlaveServer}
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
   * Unregister a {@link Publisher}.
   * 
   * @param publisherIdentifier
   *          the identifier for the {@link Publisher} to be unregistered
   * @return {@code true} if the {@link Publisher} had been registered,
   *         {@code false} otherwise
   */
  public boolean unregisterPublisher(PublisherIdentifier publisherIdentifier) {
    if (DEBUG) {
      log.info("Unregistering publisher: " + publisherIdentifier);
    }
    return publishers.remove(publisherIdentifier.getTopicName(), publisherIdentifier);
  }

  /**
   * Returns a {@link SlaveIdentifier} for the {@link Node} with the given name.
   * This API is for looking information about {@link Publisher}s and
   * {@link Subscriber}s. Use {@link #lookupService(GraphName)} instead to
   * lookup ROS-RPC {@link URI}s for {@link ServiceServer}s.
   * 
   * @param nodeName
   *          name of {@link Node} to lookup
   * @return a {@link SlaveIdentifier} for the {@link Node} with the given name,
   *         or {@code null} if there is no {@link Node} with the given name
   */
  public SlaveIdentifier lookupNode(GraphName nodeName) {
    return slaves.get(nodeName);
  }

  /**
   * Get all {@link Publisher}s which are currently registered.
   * 
   * @return a {@link List} of {@link PublisherIdentifier} instances for each
   *         registered {@link Publisher}
   */
  public List<PublisherIdentifier> getRegisteredPublishers() {
    synchronized (publishers) {
      return ImmutableList.copyOf(publishers.values());
    }
  }

  /**
   * Get all {@link Subscriber}s which are currently registered.
   * 
   * @return a list of {@link SubscriberIdentifier} instances for each
   *         registered {@link Subscriber}
   */
  public List<SubscriberIdentifier> getRegisteredSubscribers() {
    synchronized (subscribers) {
      return ImmutableList.copyOf(subscribers.values());
    }
  }

  /**
   * Get a {@link List} of all {@link Topic} message types.
   * 
   * @param calledId
   *          the {@link Node} name of the caller
   * @return a list of the form [[topic 1 name, topic 1 message type], [topic 2
   *         name, topic 2 message type], ...]
   */
  public List<List<String>> getTopicTypes(GraphName calledId) {
    List<List<String>> result = Lists.newArrayList();
    synchronized (topicMessageTypes) {
      for (Entry<GraphName, String> entry : topicMessageTypes.entrySet()) {
        result.add(Lists.newArrayList(entry.getKey().toString(), entry.getValue()));
      }
    }
    return result;
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
    List<Object> result = Lists.newArrayList();
    result.add(getSystemStatePublishers());
    result.add(getSystemStateSubscribers());
    result.add(getSystemStateServices());
    return result;
  }

  /**
   * Get the system state for {@link Publisher}s.
   * 
   * @return a {@link List} of the form [ [topic1,
   *         [topic1Publisher1...topic1PublisherN]] ... ] where the
   *         topicPublisherI instances are {@link Node} names
   */
  private List<Object> getSystemStatePublishers() {
    List<Object> result = Lists.newArrayList();
    synchronized (publishers) {
      for (GraphName name : publishers.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        result.add(topicInfo);
        topicInfo.add(name.toString());
        List<String> publist = Lists.newArrayList();
        topicInfo.add(publist);
        for (PublisherIdentifier identifier : publishers.get(name)) {
          publist.add(identifier.getSlaveIdentifier().getNodeName().toString());
        }
      }
    }
    return result;
  }

  /**
   * Get the system state for {@link Subscriber}s.
   * 
   * @return a {@link List} of the form [ [topic1,
   *         [topic1Subscriber1...topic1SubscriberN]] ... ] where the
   *         topicSubscriberI instances are {@link Node} names
   */
  private List<Object> getSystemStateSubscribers() {
    List<Object> result = Lists.newArrayList();
    synchronized (subscribers) {
      for (GraphName name : subscribers.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        result.add(topicInfo);

        topicInfo.add(name.toString());
        List<Object> sublist = Lists.newArrayList();
        topicInfo.add(sublist);

        for (SubscriberIdentifier identifier : subscribers.get(name)) {
          sublist.add(identifier.getSlaveIdentifier().getNodeName().toString());
        }
      }
    }
    return result;
  }

  /**
   * Get the system state for {@link Service}s.
   * 
   * @return a {@link List} of the form [ [service1,
   *         [serviceProvider1...serviceProviderN]] ... ] where the
   *         serviceProviderI instances are {@link Node} names
   */
  private List<Object> getSystemStateServices() {
    List<Object> result = Lists.newArrayList();
    synchronized (services) {
      // TODO(keith): Could just iterate over .entries(). Why isn't this a
      // Multimap?
      for (GraphName serviceName : services.keySet()) {
        List<Object> topicInfo = Lists.newArrayList();
        result.add(topicInfo);

        topicInfo.add(serviceName.toString());
        List<Object> providerList = Lists.newArrayList();
        topicInfo.add(providerList);

        ServiceIdentifier identifier = services.get(serviceName);
        providerList.add(identifier.getName().toString());
      }
    }
    return result;
  }

  /**
   * Lookup the provider of a particular service.
   * 
   * @param serviceName
   *          fully-qualified name of service
   * @return {@link ServiceIdentifier} of the {@link SlaveServer} with the
   *         provided name
   */
  public ServiceIdentifier lookupService(GraphName serviceName) {
    return services.get(serviceName);
  }

  /**
   * Get a list of all topics published for the give subgraph.
   * 
   * @param caller
   *          name of the caller
   * @param subgraph
   *          subgraph containing the requested {@link Topic}s, relative to
   *          caller
   * @return a {@link List} of {@link List}s where the nested {@link List}s
   *         contain, in order, the {@link Topic} name and {@link Topic} message
   *         type
   */
  public List<Object> getPublishedTopics(GraphName caller, GraphName subgraph) {
    // TODO(keith): Filter topics according to subgraph.
    Set<GraphName> topicNames = Sets.newHashSet();
    for (PublisherIdentifier publisher : publishers.values()) {
      topicNames.add(publisher.getTopicName());
    }
    List<Object> result = Lists.newArrayList();
    for (GraphName topicName : topicNames) {
      result.add(Lists.newArrayList(topicName.toString(), topicMessageTypes.get(topicName)));
    }
    return result;
  }

  /**
   * Register the message type of a {@link Topic}.
   * 
   * @param topicName
   *          name of the {@link Topic}
   * @param topicMessageType
   *          the message type of the {@link Topic}, {@link Subscriber}s can
   *          give a message type of {@value Topic#WILDCARD_MESSAGE_TYPE}
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
        // Publishers always trump.
        topicMessageTypes.put(topicName, topicMessageType);
        isPublisherTopicMessageTypes.put(topicName, Boolean.TRUE);
      } else {
        // Is a subscriber.
        if (!Topic.WILDCARD_MESSAGE_TYPE.equals(topicMessageType)) {
          if (topicMessageTypes.containsKey(topicName)) {
            // if has only been subscribers giving the type, register it.
            if (!isPublisherTopicMessageTypes.get(topicName)) {
              topicMessageTypes.put(topicName, topicMessageType);
            }
          } else {
            // Not registered yet, so no worry about trumping.
            topicMessageTypes.put(topicName, topicMessageType);
            isPublisherTopicMessageTypes.put(topicName, Boolean.FALSE);
          }
        }
      }
    }
  }
}
