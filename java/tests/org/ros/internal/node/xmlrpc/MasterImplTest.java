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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.ros.internal.message.MessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicIdentifier;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImplTest {

  @Test
  public void testRegisterPublisherWithNoSubscribers() throws XmlRpcTimeoutException,
      RemoteException {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerPublisher(Matchers.<PublisherIdentifier>any())).thenReturn(
        Lists.<SubscriberIdentifier>newArrayList());
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerPublisher("/caller", "/foo", "/bar", "http://baz");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(), response.get(2));
  }

  @Test
  public void testRegisterPublisher() throws XmlRpcTimeoutException, RemoteException {
    MasterServer mockMaster = mock(MasterServer.class);
    final SlaveIdentifier slaveIdentifier =
        SlaveIdentifier.createFromStrings("/slave", "http://api");
    final TopicDefinition topicDefinition =
        TopicDefinition
            .create(new GraphName("/topic"), MessageDefinition.createFromTypeName("msg"));
    SubscriberIdentifier subscriberDescription =
        new SubscriberIdentifier(slaveIdentifier, topicDefinition);
    when(mockMaster.registerPublisher(argThat(new ArgumentMatcher<PublisherIdentifier>() {
      @Override
      public boolean matches(Object argument) {
        PublisherIdentifier publisherIdentifier = (PublisherIdentifier) argument;
        return publisherIdentifier.getTopicIdentifier().equals(topicDefinition.getIdentifier())
            && publisherIdentifier.getSlaveIdentifier().equals(slaveIdentifier);
      }
    }))).thenReturn(Lists.<SubscriberIdentifier>newArrayList(subscriberDescription));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response =
        master.registerPublisher("/slave", "/topic", "/topicType", "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(subscriberDescription.getSlaveUri().toString()),
        response.get(2));
  }

  @Test
  public void testRegisterSubscriberWithNoSubscribers() {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerSubscriber(Matchers.<SubscriberIdentifier>any())).thenReturn(
        Lists.<PublisherIdentifier>newArrayList());
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerSubscriber("/caller", "/foo", "/bar", "http://baz");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(), response.get(2));
  }

  @Test
  public void testRegisterSubscriber() {
    MasterServer mockMaster = mock(MasterServer.class);
    SlaveIdentifier slaveIdentifier = SlaveIdentifier.createFromStrings("/slave", "http://api");
    PublisherIdentifier publisherIdentifier =
        new PublisherIdentifier(slaveIdentifier, new TopicIdentifier(new GraphName("/topic")));
    when(mockMaster.registerSubscriber(Matchers.<SubscriberIdentifier>any())).thenReturn(
        Lists.<PublisherIdentifier>newArrayList(publisherIdentifier));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response =
        master.registerSubscriber("/slave", "/topic", "/topicType", "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(publisherIdentifier.getUri().toString()), response.get(2));
  }

}
