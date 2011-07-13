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

import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.xmlrpc.MasterImpl;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterServer extends NodeServer {

  private final Map<String, SlaveIdentifier> slaves;
  private final Map<String, ServiceIdentifier> services;
  private final Multimap<String, PublisherIdentifier> publishers;
  private final Multimap<String, SubscriberIdentifier> subscribers;
  private final GraphName masterName;

  public MasterServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress) {
    super(bindAddress, advertiseAddress);
    slaves = Maps.newConcurrentMap();
    services = Maps.newConcurrentMap();
    publishers = Multimaps.synchronizedMultimap(HashMultimap.<String, PublisherIdentifier>create());
    subscribers =
        Multimaps.synchronizedMultimap(HashMultimap.<String, SubscriberIdentifier>create());
    masterName = new GraphName("/master");
  }

  public void start() {
    super.start(org.ros.internal.node.xmlrpc.MasterImpl.class, new MasterImpl(this));
  }

  public void registerService(ServiceIdentifier description) {
    services.put(description.getName().toString(), description);
  }

  public int unregisterService(ServiceIdentifier serviceIdentifier) {
    String name = serviceIdentifier.getName().toString();
    if (services.containsKey(name)) {
      services.remove(name);
      return 1;
    }
    return 0;
  }

  private void addSlave(SlaveIdentifier slaveIdentifier) {
    String name = slaveIdentifier.getName().toString();
    Preconditions.checkState(slaves.get(name) == null || slaves.get(name).equals(slaveIdentifier),
        "Failed to add slave: " + slaveIdentifier + "\nExisting slave: " + slaves.get(name));
    slaves.put(name, slaveIdentifier);
  }

  private void publisherUpdate(String topicName) {
    List<URI> publisherUris = Lists.newArrayList();
    synchronized (publishers) {
      for (PublisherIdentifier publisherIdentifier : publishers.get(topicName)) {
        publisherUris.add(publisherIdentifier.getUri());
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
   * @return Publishers is a list of XMLRPC API URIs for nodes currently
   *         publishing the specified topic.
   */
  public List<PublisherIdentifier> registerSubscriber(SubscriberIdentifier subscriberIdentifier) {
    subscribers.put(subscriberIdentifier.getTopicName().toString(), subscriberIdentifier);
    addSlave(subscriberIdentifier.getSlaveIdentifier());
    synchronized (publishers) {
      return ImmutableList.copyOf(publishers.get(subscriberIdentifier.getTopicName().toString()));
    }
  }

  public int unregisterSubscriber(SubscriberIdentifier subscriberIdentifier) {
    String topicName = subscriberIdentifier.getTopicName().toString();
    if (subscribers.containsKey(topicName)) {
      subscribers.remove(topicName, subscriberIdentifier);
      return 1;
    }
    return 0;
  }

  /**
   * Register the caller as a publisher the topic.
   * 
   * @return List of current subscribers of topic in the form of XML-RPC URIs.
   */
  public List<SubscriberIdentifier> registerPublisher(PublisherIdentifier publisher) {
    publishers.put(publisher.getTopicName().toString(), publisher);
    addSlave(publisher.getSlaveIdentifier());
    publisherUpdate(publisher.getTopicName().toString());
    synchronized (subscribers) {
      return ImmutableList.copyOf(subscribers.get(publisher.getTopicName().toString()));
    }
  }

  public int unregisterPublisher(PublisherIdentifier publisherIdentifier) {
    String topicName = publisherIdentifier.getTopicName().toString();
    if (publishers.containsKey(topicName)) {
      publishers.remove(topicName, publisherIdentifier);
      return 1;
    }
    return 0;
  }

  /**
   * Returns a {@link SlaveIdentifier} for the node with the given name. This
   * API is for looking information about publishers and subscribers. Use
   * lookupService instead to lookup ROS-RPC URIs.
   * 
   * @param slaveName
   *          name of node to lookup
   * @return a {@link SlaveIdentifier} for the node with the given name
   */
  public SlaveIdentifier lookupNode(String slaveName) {
    return slaves.get(slaveName);
  }

  public List<PublisherIdentifier> getRegisteredPublishers() {
    synchronized (publishers) {
      return ImmutableList.copyOf(publishers.values());
    }
  }

  public List<SubscriberIdentifier> getRegisteredSubscribers() {
    synchronized (subscribers) {
      return ImmutableList.copyOf(subscribers.values());
    }
  }

  public List<Object> getSystemState(String callerId) {
    return null;
  }

  /**
   * Lookup the provider of a particular service.
   * 
   * @param callerId
   *          ROS caller ID
   * @param service
   *          Fully-qualified name of service
   * @return service URI that provides address and port of the service. Fails if
   *         there is no provider.
   */
  public ServiceIdentifier lookupService(String callerId, String service) {
    return services.get(service);
  }

}
