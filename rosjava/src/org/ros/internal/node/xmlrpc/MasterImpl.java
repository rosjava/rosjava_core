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

package org.ros.internal.node.xmlrpc;

import com.google.common.collect.Lists;

import org.ros.internal.exception.RemoteException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.topic.TopicIdentifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImpl implements Master {

  private final MasterServer master;

  public MasterImpl(MasterServer master) {
    this.master = master;
  }

  @Override
  public List<Object> getPublishedTopics(String callerId, String subgraph) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> getSystemState(String callerId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> getUri(String callerId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> lookupNode(String callerId, String nodeName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> lookupService(String callerId, String service) {
    ServiceIdentifier identifier = master.lookupService(callerId, service);
    if (identifier != null) {
      return Response.createSuccess("Success", identifier.getUri().toString()).toList();
    }
    return Response.createError("No such service.", null).toList();
  }

  @Override
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) throws XmlRpcTimeoutException, RemoteException {
    SlaveIdentifier slaveIdentifier = SlaveIdentifier.createFromStrings(callerId, callerApi);
    PublisherIdentifier publisherIdentifier =
        new PublisherIdentifier(slaveIdentifier, new TopicIdentifier(new GraphName(topic)));
    List<SubscriberIdentifier> subscribers = master.registerPublisher(publisherIdentifier);
    List<String> urls = Lists.newArrayList();
    for (SubscriberIdentifier subscriberIdentifier : subscribers) {
      urls.add(subscriberIdentifier.getUri().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> unregisterPublisher(String callerId, String topicName, String callerApi) {
    PublisherIdentifier publisherIdentifier =
        PublisherIdentifier.createFromStrings(callerId, callerApi, topicName);
    return Response.createSuccess("Success", master.unregisterPublisher(publisherIdentifier))
        .toList();
  }

  @Override
  public List<Object> registerService(String callerId, String serviceName, String serviceApi,
      String callerApi) {
    ServiceIdentifier serviceIdentifier;
    try {
      serviceIdentifier =
          new ServiceIdentifier(new GraphName(serviceName), new URI(serviceApi));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    master.registerService(serviceIdentifier);
    return Response.createSuccess("Success", 0).toList();
  }

  @Override
  public List<Object> unregisterService(String callerId, String serviceName, String serviceApi) {
    ServiceIdentifier serviceIdentifier;
    try {
      serviceIdentifier = new ServiceIdentifier(new GraphName(serviceName), new URI(serviceApi));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    int result = master.unregisterService(serviceIdentifier);
    return Response.createSuccess("Success", result).toList();
  }

  @Override
  public List<Object> registerSubscriber(String callerId, String topicName, String topicType,
      String callerApi) {
    List<PublisherIdentifier> publishers =
        master.registerSubscriber(SubscriberIdentifier.createFromStrings(callerId, callerApi,
            topicName));
    List<String> urls = Lists.newArrayList();
    for (PublisherIdentifier publisherIdentifier : publishers) {
      urls.add(publisherIdentifier.getUri().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> unregisterSubscriber(String callerId, String topicName, String callerApi) {
    SubscriberIdentifier subscriberIdentifier =
        SubscriberIdentifier.createFromStrings(callerId, callerApi, topicName);
    return Response.createSuccess("Success", master.unregisterSubscriber(subscriberIdentifier))
        .toList();
  }

}
