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

import com.google.common.collect.Lists;

import org.ros.internal.node.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.Topic;
import org.ros.internal.topic.TopicDefinition;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterClient extends NodeClient<org.ros.internal.node.xmlrpc.Master> {

  /**
   * Create a new {@link MasterClient} connected to the specified
   * {@link MasterServer} URI.
   * 
   * @param uri the {@link URI} of the {@link MasterServer} to connect to
   * @throws MalformedURLException
   */
  public MasterClient(URI uri) throws MalformedURLException {
    super(uri, org.ros.internal.node.xmlrpc.Master.class);
  }

  /**
   * Register the caller as a provider of the specified service.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link ServiceServer} is
   *        running
   * @param service the {@link ServiceServer} to register
   * @return a {@link Response} with a void result
   * @throws URISyntaxException
   */
  public Response<Void> registerService(SlaveIdentifier slave, ServiceServer<?> service)
      throws URISyntaxException {
    List<Object> response =
        node.registerService(slave.getName(), service.getName(), service.getUri().toString(), slave
            .getUri().toString());
    return new Response<Void>((Integer) response.get(0), (String) response.get(1), null);
  }

  /**
   * Unregister the caller as a provider of the specified service.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link ServiceServer} is
   *        running
   * @param service the {@link ServiceServer} to unregister
   * @return a {@link Response} with the number of unregistered services as the
   *         result
   * @throws URISyntaxException
   */
  public Response<Integer> unregisterService(SlaveIdentifier slave, ServiceServer<?> service)
      throws URISyntaxException {
    List<Object> response =
        node.unregisterService(slave.getName(), service.getName(), service.getUri().toString());
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * @param slave the {@link SlaveIdentifier} that the {@link Subscriber} is
   *        running on
   * @param subscriber the {@link Subscriber} to register
   * @return a {@link Response} with a {@link List} or {@link SlaveServer}
   *         XML-RPC API URIs for nodes currently publishing the specified topic
   *         as the result
   * @throws URISyntaxException
   */
  public Response<List<URI>> registerSubscriber(SlaveIdentifier slave, Subscriber<?> subscriber)
      throws URISyntaxException {
    List<Object> response =
        node.registerSubscriber(slave.getName(), subscriber.getTopicName(),
            subscriber.getTopicMessageType(), slave.getUri().toString());
    List<Object> values = Arrays.asList((Object[]) response.get(2));
    List<URI> uris = Lists.newArrayList();
    for (Object value : values) {
      uris.add(new URI((String) value));
    }
    return new Response<List<URI>>((Integer) response.get(0), (String) response.get(1), uris);
  }

  /**
   * Unregister the specified subscriber.
   * 
   * @param slave the {@link SlaveIdentifier} where the subscriber is running
   * @param subscriber the {@link Subscriber} to unregister
   * @return a {@link Response} with the number of unregistered subscribers as
   *         the result
   */
  public Response<Integer> unregisterSubscriber(SlaveIdentifier slave, Subscriber<?> subscriber) {
    List<Object> response =
        node.unregisterSubscriber(slave.getName(), subscriber.getTopicName(), slave.getUri()
            .toString());
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  /**
   * Register the specified {@link Publisher}.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link Publisher} is
   *        running
   * @param publisher the publisher to register
   * @return a {@link Response} with a {@link List} of the current
   *         {@link SlaveServer} URIs which have {@link Subscriber}s for the
   *         published {@link Topic}.
   * @throws URISyntaxException
   */
  public Response<List<URI>> registerPublisher(SlaveIdentifier slave, Publisher publisher)
      throws URISyntaxException {
    String topicName = publisher.getTopicName();
    String messageType = publisher.getTopicMessageType();
    List<Object> response =
        node.registerPublisher(slave.getName(), topicName, messageType, slave.getUri().toString());
    List<Object> values = Arrays.asList((Object[]) response.get(2));
    List<URI> uris = Lists.newArrayList();
    for (Object value : values) {
      uris.add(new URI((String) value));
    }
    return new Response<List<URI>>((Integer) response.get(0), (String) response.get(1), uris);
  }

  /**
   * Unregister the specified {@link Publisher}.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link Publisher} is
   *        running
   * @param publisher the {@link Publisher} to unregister
   * @return a {@link Response} with the number of unregistered {@link Publisher}s as the result
   */
  public Response<Integer> unregisterPublisher(SlaveIdentifier slave, Publisher publisher) {
    List<Object> response =
        node.unregisterPublisher(slave.getName(), publisher.getTopicName(), slave.getUri()
            .toString());
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  public Response<URI> lookupNode(String callerId, String nodeName) throws URISyntaxException {
    List<Object> response = node.lookupNode(callerId, nodeName);
    return new Response<URI>((Integer) response.get(0), (String) response.get(1), new URI(
        (String) response.get(2)));
  }

  public Response<List<TopicDefinition>> getPublishedTopics(String callerId, String subgraph) {
    throw new UnsupportedOperationException();
  }

  public Response<Object> getSystemState(String callerId) {
    throw new UnsupportedOperationException();
  }

  public Response<URI> getUri(String callerId) throws URISyntaxException {
    List<Object> response = node.getUri(callerId);
    return new Response<URI>((Integer) response.get(0), (String) response.get(1), new URI(
        (String) response.get(2)));
  }

  public Response<URI> lookupService(String callerId, String service) throws URISyntaxException {
    List<Object> response = node.lookupService(callerId, service);
    String value = (String) response.get(2);
    return new Response<URI>((Integer) response.get(0), (String) response.get(1), new URI(value));
  }

}
