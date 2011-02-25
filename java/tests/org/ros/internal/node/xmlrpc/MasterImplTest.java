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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.mockito.Matchers;
import org.ros.internal.node.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.SubscriberIdentifier;
import org.ros.internal.topic.TopicDefinition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImplTest {

  @Test
  public void testRegisterPublisherWithNoSubscribers() throws URISyntaxException {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerPublisher(Matchers.<String>any(), Matchers.<PublisherIdentifier>any()))
        .thenReturn(Lists.<SubscriberIdentifier>newArrayList());
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerPublisher("/caller", "/foo", "/bar", "http://baz");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), Lists.newArrayList());
  }

  @Test
  public void testRegisterPublisher() throws URISyntaxException {
    MasterServer mockMaster = mock(MasterServer.class);
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/slave", new URI("http://api"));
    TopicDefinition topicDefinition = new TopicDefinition("/topic",
        MessageDefinition.createMessageDefinition("msg"));
    SubscriberIdentifier subscriberDescription = new SubscriberIdentifier(slaveIdentifier,
        topicDefinition);
    when(mockMaster.registerPublisher(Matchers.<String>any(), Matchers.<PublisherIdentifier>any()))
        .thenReturn(Lists.<SubscriberIdentifier>newArrayList(subscriberDescription));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master
        .registerPublisher("/slave", "/topic", "/topicType", "http://api");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2),
        Lists.newArrayList(subscriberDescription.getSlaveUri().toString()));
  }

  @Test
  public void testRegisterSubscriberWithNoSubscribers() throws URISyntaxException {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerSubscriber(Matchers.<SubscriberIdentifier>any())).thenReturn(
        Lists.<PublisherIdentifier>newArrayList());
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerSubscriber("/caller", "/foo", "/bar", "http://baz");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), Lists.newArrayList());
  }

  @Test
  public void testRegisterSubscriber() throws URISyntaxException {
    MasterServer mockMaster = mock(MasterServer.class);
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/slave", new URI("http://api"));
    TopicDefinition topicDefinition = new TopicDefinition("/topic",
        MessageDefinition.createMessageDefinition("msg"));
    PublisherIdentifier publisherDescription = new PublisherIdentifier(slaveIdentifier,
        topicDefinition);
    when(mockMaster.registerSubscriber(Matchers.<SubscriberIdentifier>any())).thenReturn(
        Lists.<PublisherIdentifier>newArrayList(publisherDescription));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerSubscriber("/slave", "/topic", "/topicType",
        "http://api");
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), Lists.newArrayList(publisherDescription.getSlaveUri().toString()));
  }

}
