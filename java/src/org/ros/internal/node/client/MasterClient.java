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

import org.ros.internal.node.RemoteException;
import org.ros.internal.node.response.IntegerResultFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.UriListResultFactory;
import org.ros.internal.node.response.UriResultFactory;
import org.ros.internal.node.response.VoidResultFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.Topic;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.message.Message;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Provides access to the XML-RPC API exposed by a {@link MasterServer}.
 * 
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
   * Registers the given {@link ServiceServer}.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link ServiceServer} is
   *        running
   * @param service the {@link ServiceServer} to register
   * @return a {@link Response} with a void result
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<Void> registerService(SlaveIdentifier slave,
      ServiceServer<? extends Message> service) throws URISyntaxException, RemoteException {
    return Response.fromListChecked(node.registerService(slave.getName(), service.getName(), service
        .getUri().toString(), slave.getUri().toString()), new VoidResultFactory());
  }

  /**
   * Unregisters the specified {@link ServiceServer}.
   * 
   * @param slave the {@link SlaveIdentifier} where the {@link ServiceServer} is
   *        running
   * @param service the {@link ServiceServer} to unregister
   * @return a {@link Response} with the number of unregistered services as the
   *         result
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<Integer> unregisterService(SlaveIdentifier slave,
      ServiceServer<? extends Message> service) throws URISyntaxException, RemoteException {
    return Response.fromListChecked(
        node.unregisterService(slave.getName(), service.getName(), service.getUri().toString()),
        new IntegerResultFactory());
  }

  /**
   * Registers the given {@link Subscriber}. In addition to receiving a list of
   * current {@link Publisher}s, the {@link Subscriber}s {@link SlaveServer}
   * will also receive notifications of new {@link Publisher}s via the
   * publisherUpdate API.
   * 
   * @param slave the {@link SlaveIdentifier} that the {@link Subscriber} is
   *        running on
   * @param subscriber the {@link Subscriber} to register
   * @return a {@link Response} with a {@link List} or {@link SlaveServer}
   *         XML-RPC API URIs for nodes currently publishing the specified topic
   *         as the result
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<List<URI>> registerSubscriber(SlaveIdentifier slave, Subscriber<?> subscriber)
      throws URISyntaxException, RemoteException {
    return Response.fromListChecked(
        node.registerSubscriber(slave.getName(), subscriber.getTopicName(),
            subscriber.getTopicMessageType(), slave.getUri().toString()),
        new UriListResultFactory());
  }

  /**
   * Unregisters the specified {@link Subscriber}.
   * 
   * @param slave the {@link SlaveIdentifier} where the subscriber is running
   * @param subscriber the {@link Subscriber} to unregister
   * @return a {@link Response} with the number of unregistered subscribers as
   *         the result
   * @throws RemoteException
   */
  public Response<Integer> unregisterSubscriber(SlaveIdentifier slave, Subscriber<?> subscriber)
      throws RemoteException {
    return Response.fromListChecked(node.unregisterSubscriber(slave.getName(), subscriber.getTopicName(),
        slave.getUri().toString()), new IntegerResultFactory());
  }

  /**
   * Registers the specified {@link PublisherIdentifier}.
   * 
   * @param publisher the {@link PublisherIdentifier} of the {@link Publisher}
   *        to register
   * @return a {@link Response} with a {@link List} of the current
   *         {@link SlaveServer} URIs which have {@link Subscriber}s for the
   *         published {@link Topic}.
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<List<URI>> registerPublisher(PublisherIdentifier publisher)
      throws URISyntaxException, RemoteException {
    return Response.fromListChecked(
        node.registerPublisher(publisher.getNodeName(), publisher.getTopicName(),
            publisher.getTopicMessageType(), publisher.getSlaveUri().toString()),
        new UriListResultFactory());
  }

  /**
   * Unregisters the specified {@link PublisherIdentifier}.
   * 
   * @param publisher the {@link PublisherIdentifier} of the {@link Publisher}
   *        to unregister
   * @return a {@link Response} with the number of unregistered
   *         {@link Publisher}s as the result
   * @throws RemoteException
   */
  public Response<Integer> unregisterPublisher(PublisherIdentifier publisher)
      throws RemoteException {
    return Response.fromListChecked(node.unregisterPublisher(publisher.getNodeName(),
        publisher.getTopicName(), publisher.getSlaveUri().toString()), new IntegerResultFactory());
  }

  /**
   * Returns the {@link URI} of the {@link SlaveServer} with the given name.
   * 
   * @param slave the {@link SlaveIdentifier} of the caller
   * @param nodeName the name of the {@link SlaveServer} to lookup
   * @return a {@link Response} with the {@link URI} of the {@link SlaveServer}
   *         as a result
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<URI> lookupNode(SlaveIdentifier slave, String nodeName)
      throws URISyntaxException, RemoteException {
    return Response.fromListChecked(node.lookupNode(slave.getName(), nodeName), new UriResultFactory());
  }

  /**
   * Returns the {@link URI} of the {@link MasterServer}.
   * 
   * @param slave the {@link SlaveIdentifier} of the caller
   * @return the {@link URI} of the {@link MasterServer}
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<URI> getUri(SlaveIdentifier slave) throws URISyntaxException, RemoteException {
    return Response.fromListChecked(node.getUri(slave.getName()), new UriResultFactory());
  }

  /**
   * Returns the {@link URI} of the {@link ServiceServer} with the given name.
   * 
   * @param slave the {@link SlaveIdentifier} of the caller
   * @param serviceName the name of the {@link ServiceServer} to look up
   * @return a {@link Response} with the {@link URI} of the
   *         {@link ServiceServer} as a result
   * @throws URISyntaxException
   * @throws RemoteException
   */
  public Response<URI> lookupService(SlaveIdentifier slave, String serviceName)
      throws URISyntaxException, RemoteException {
    return Response.fromListChecked(node.lookupService(slave.getName(), serviceName),
        new UriResultFactory());
  }

  public Response<List<TopicDefinition>> getPublishedTopics(SlaveIdentifier slave, String subgraph) {
    throw new UnsupportedOperationException();
  }

  public Response<Object> getSystemState(SlaveIdentifier slave) {
    throw new UnsupportedOperationException();
  }

}
