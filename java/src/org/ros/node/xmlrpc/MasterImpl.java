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

import org.ros.message.MessageDescription;
import org.ros.node.Response;
import org.ros.node.server.SlaveDescription;
import org.ros.topic.PublisherDescription;
import org.ros.topic.SubscriberDescription;
import org.ros.topic.TopicDescription;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MasterImpl implements Master {

  private final org.ros.node.server.Master master;

  public MasterImpl(org.ros.node.server.Master master) {
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
    throw new UnsupportedOperationException();
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
    SlaveDescription slaveDescription = new SlaveDescription(callerId, new URL(callerApi));
    TopicDescription topicDescription = new TopicDescription(topic,
        MessageDescription.createMessageDescription(topicType));
    PublisherDescription description = new PublisherDescription(slaveDescription, topicDescription);
    List<SubscriberDescription> subscribers = master.registerPublisher(callerId, description);
    List<String> urls = Lists.newArrayList();
    for (SubscriberDescription subscriberDescription : subscribers) {
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
      String callerApi) {
    throw new UnsupportedOperationException();
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
    SlaveDescription slaveDescription = new SlaveDescription(callerId, new URL(callerApi));
    TopicDescription topicDescription = new TopicDescription(topic,
        MessageDescription.createMessageDescription(topicType));
    List<PublisherDescription> publishers = master.registerSubscriber(new SubscriberDescription(
        slaveDescription, topicDescription));
    List<String> urls = Lists.newArrayList();
    for (PublisherDescription publisherDescription : publishers) {
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
