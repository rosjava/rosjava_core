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
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.Subscriber;
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

  public MasterClient(URI uri) throws MalformedURLException {
    super(uri, org.ros.internal.node.xmlrpc.Master.class);
  }

  /**
   * Register the caller as a provider of the specified service.
   * 
   * @param callerId ROS caller ID
   * @param service
   * @param uri XML-RPC URI of caller node
   * @return
   * @throws MalformedURLException
   * @throws URISyntaxException 
   */
  public Response<Integer> registerService(String callerId, ServiceServer<?> service, URI uri)
      throws MalformedURLException, URISyntaxException {
    List<Object> response = node
        .registerService(callerId, service.getName(), service.getUri()
            .toString(), uri.toString());
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  public Response<Integer> unregisterService(String callerId, String service, String serviceApi) {
    List<Object> response = node.unregisterService(callerId, service, serviceApi);
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * @param callerId ROS caller ID
   * @param subscriber
   * @param callerApi API URI of subscriber to register (used for new publisher
   *        notifications)
   * @return Publishers for topic as a list of XML-RPC API URIs for nodes
   *         currently publishing the specified topic.
   * @throws MalformedURLException
   * @throws URISyntaxException 
   */
  public Response<List<URI>> registerSubscriber(String callerId, Subscriber<?> subscriber,
      URI callerApi) throws MalformedURLException, URISyntaxException {
    List<Object> response =
        node.registerSubscriber(callerId, subscriber.getTopicName(),
            subscriber.getTopicMessageType(), callerApi.toString());
    List<Object> values = Arrays.asList((Object[]) response.get(2));
    List<URI> uris = Lists.newArrayList();
    for (Object value : values) {
      uris.add(new URI((String) value));
    }
    return new Response<List<URI>>((Integer) response.get(0), (String) response.get(1), uris);
  }

  public Response<Integer> unregisterSubscriber(String callerId, String topic, String callerApi) {
    List<Object> response = node.unregisterSubscriber(callerId, topic, callerApi);
    return new Response<Integer>((Integer) response.get(0), (String) response.get(1),
        (Integer) response.get(2));
  }

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param callerId ROS caller ID
   * @param publisher the publisher to register
   * @param uri API URL of publisher to register
   * @return List of current subscribers of topic in the form of XML-RPC URIs
   * @throws MalformedURLException
   * @throws URISyntaxException 
   */
  public Response<List<URI>> registerPublisher(String callerId, Publisher publisher, URI uri)
      throws MalformedURLException, URISyntaxException {
    String topicName = publisher.getTopicName();
    String messageType = publisher.getTopicMessageType();
    List<Object> response =
        node.registerPublisher(callerId, topicName, messageType, uri.toString());
    List<Object> values = Arrays.asList((Object[]) response.get(2));
    List<URI> uris = Lists.newArrayList();
    for (Object value : values) {
      uris.add(new URI((String) value));
    }
    return new Response<List<URI>>((Integer) response.get(0), (String) response.get(1), uris);
  }

  public Response<Integer> unregisterPublisher(String callerId, String topic, String callerApi) {
    List<Object> response = node.unregisterPublisher(callerId, topic, callerApi);
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
