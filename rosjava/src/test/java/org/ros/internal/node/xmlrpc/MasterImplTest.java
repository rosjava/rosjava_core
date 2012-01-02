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

import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;

import com.google.common.collect.Lists;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImplTest {

  @Test
  public void testRegisterPublisherWithNoSubscribers() {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerPublisher(Matchers.<PublisherIdentifier>any())).thenReturn(
        Lists.<SubscriberIdentifier>newArrayList());
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response = master.registerPublisher("/caller", "/foo", "/bar", "http://baz");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(), response.get(2));
  }

  @Test
  public void testRegisterPublisher() {
    MasterServer mockMaster = mock(MasterServer.class);
    final SubscriberIdentifier subscriberIdentifier =
        SubscriberIdentifier.newFromStrings("/slave", "http://api", "/topic");
    when(mockMaster.registerPublisher(argThat(new ArgumentMatcher<PublisherIdentifier>() {
      @Override
      public boolean matches(Object argument) {
        PublisherIdentifier publisherIdentifier = (PublisherIdentifier) argument;
        return publisherIdentifier.getTopicIdentifier().equals(
            subscriberIdentifier.getTopicIdentifier())
            && publisherIdentifier.getSlaveIdentifier().equals(
                subscriberIdentifier.getSlaveIdentifier());
      }
    }))).thenReturn(Lists.<SubscriberIdentifier>newArrayList(subscriberIdentifier));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response =
        master.registerPublisher("/slave", "/topic", "/topicType", "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(subscriberIdentifier.getUri().toString()), response.get(2));
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
    final PublisherIdentifier publisherIdentifier =
        PublisherIdentifier.newFromStrings("/slave", "http://api", "/topic");
    when(mockMaster.registerSubscriber(argThat(new ArgumentMatcher<SubscriberIdentifier>() {
      @Override
      public boolean matches(Object argument) {
        SubscriberIdentifier subscriberIdentifier = (SubscriberIdentifier) argument;
        return subscriberIdentifier.getTopicIdentifier().equals(
            subscriberIdentifier.getTopicIdentifier())
            && subscriberIdentifier.getSlaveIdentifier().equals(
                subscriberIdentifier.getSlaveIdentifier());
      }
    }))).thenReturn(Lists.<PublisherIdentifier>newArrayList(publisherIdentifier));
    MasterImpl master = new MasterImpl(mockMaster);
    List<Object> response =
        master.registerSubscriber("/slave", "/topic", "/topicType", "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(publisherIdentifier.getSlaveUri().toString()), response.get(2));
  }
}
