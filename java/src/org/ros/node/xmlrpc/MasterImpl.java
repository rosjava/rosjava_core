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

package org.ros.node.xmlrpc;

import com.google.common.collect.Lists;

import org.ros.node.Response;
import org.ros.node.server.MasterServer;
import org.ros.node.server.SlaveIdentifier;
import org.ros.topic.MessageDefinition;
import org.ros.topic.PublisherIdentifier;
import org.ros.topic.ServiceDefinition;
import org.ros.topic.ServiceIdentifier;
import org.ros.topic.SubscriberIdentifier;
import org.ros.topic.TopicDefinition;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImpl implements Master {

  private final MasterServer master;

  public MasterImpl(MasterServer master) {
    this.master = master;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#getPublishedTopics(java.lang.String,
   * java.lang.String)
   */
  @Override
  public List<Object> getPublishedTopics(String callerId, String subgraph) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#getSystemState(java.lang.String)
   */
  @Override
  public List<Object> getSystemState(String callerId) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#getUri(java.lang.String)
   */
  @Override
  public List<Object> getUri(String callerId) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#lookupNode(java.lang.String,
   * java.lang.String)
   */
  @Override
  public List<Object> lookupNode(String callerId, String nodeName) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#lookupService(java.lang.String,
   * java.lang.String)
   */
  @Override
  public List<Object> lookupService(String callerId, String service) {
    List<ServiceIdentifier> services = master.lookupService(callerId, service);
    List<String> urls = Lists.newArrayList();
    for (ServiceIdentifier description : services) {
      urls.add(description.getUrl().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#registerPublisher(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) throws MalformedURLException {
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier(callerId, new URL(callerApi));
    TopicDefinition topicDefinition =
        new TopicDefinition(topic, MessageDefinition.createMessageDefinition(topicType));
    PublisherIdentifier description = new PublisherIdentifier(slaveIdentifier, topicDefinition);
    List<SubscriberIdentifier> subscribers = master.registerPublisher(callerId, description);
    List<String> urls = Lists.newArrayList();
    for (SubscriberIdentifier subscriberDescription : subscribers) {
      urls.add(subscriberDescription.getSlaveUrl().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#registerService(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi) throws MalformedURLException {
    ServiceIdentifier description =
        new ServiceIdentifier(new SlaveIdentifier(callerId, new URL(callerApi)),
            new ServiceDefinition(service, null), new URL(serviceApi));
    master.registerService(description);
    return Response.createSuccess("Success", 0).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#registerSubscriber(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) throws MalformedURLException {
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier(callerId, new URL(callerApi));
    TopicDefinition topicDefinition =
        new TopicDefinition(topic, MessageDefinition.createMessageDefinition(topicType));
    List<PublisherIdentifier> publishers =
        master.registerSubscriber(new SubscriberIdentifier(slaveIdentifier, topicDefinition));
    List<String> urls = Lists.newArrayList();
    for (PublisherIdentifier publisherDescription : publishers) {
      urls.add(publisherDescription.getSlaveUrl().toString());
    }
    return Response.createSuccess("Success", urls).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#unregisterPublisher(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#unregisterService(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> unregisterService(String callerId, String service, String serviceApi) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Master#unregisterSubscriber(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

}
