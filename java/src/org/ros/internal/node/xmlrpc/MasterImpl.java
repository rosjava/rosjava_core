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

import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.topic.MessageDefinition;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.topic.TopicDefinition;

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
    return Response.createSuccess("Success", identifier.getUri().toString()).toList();
  }

  @Override
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) throws URISyntaxException {
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier(callerId, new URI(callerApi));
    TopicDefinition topicDefinition =
        new TopicDefinition(topic, MessageDefinition.createMessageDefinition(topicType));
    PublisherIdentifier description = new PublisherIdentifier(slaveIdentifier, topicDefinition);
    List<SubscriberIdentifier> subscribers = master.registerPublisher(callerId, description);
    List<String> urls = Lists.newArrayList();
    for (SubscriberIdentifier subscriberDescription : subscribers) {
      urls.add(subscriberDescription.getSlaveUri().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> registerService(String callerId, String serviceName, String serviceApi,
      String callerApi) throws URISyntaxException {
    // TODO(damonkohler): Pull out factory methods to avoid passing in the null
    // type and md5Checksum here.
    ServiceIdentifier description =
        new ServiceIdentifier(new URI(serviceApi), new ServiceDefinition(serviceName, null, null));
    master.registerService(description);
    return Response.createSuccess("Success", 0).toList();
  }

  @Override
  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) throws URISyntaxException {
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier(callerId, new URI(callerApi));
    TopicDefinition topicDefinition =
        new TopicDefinition(topic, MessageDefinition.createMessageDefinition(topicType));
    List<PublisherIdentifier> publishers =
        master.registerSubscriber(new SubscriberIdentifier(slaveIdentifier, topicDefinition));
    List<String> urls = Lists.newArrayList();
    for (PublisherIdentifier publisherDescription : publishers) {
      urls.add(publisherDescription.getSlaveUri().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> unregisterService(String callerId, String service, String serviceApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

}
