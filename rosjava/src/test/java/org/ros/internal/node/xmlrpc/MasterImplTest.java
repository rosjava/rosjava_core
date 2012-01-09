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
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterImplTest {

  @Test
  public void testGetUri() throws Exception {
    URI testUri = new URI("http://foo.bar:8080");
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.getUri()).thenReturn(testUri);
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.getUri("/caller");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(testUri.toString(), response.get(2));
  }

  @Test
  public void testLookupNodeExisting() throws Exception {
    MasterServer mockMaster = mock(MasterServer.class);
    final SlaveIdentifier slaveIdentifier = SlaveIdentifier.newFromStrings("/foo", "http://bar");
    when(mockMaster.lookupNode(argThat(new ArgumentMatcher<GraphName>() {
      @Override
      public boolean matches(Object argument) {
        GraphName graphName = (GraphName) argument;
        return graphName.equals(slaveIdentifier.getNodeName());
      }
    }))).thenReturn(slaveIdentifier);
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.lookupNode("/caller", slaveIdentifier.getNodeName().toString());
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(slaveIdentifier.getUri().toString(), response.get(2));
  }

  @Test
  public void testLookupNodeNotExisting() throws Exception {
    MasterServer mockMaster = mock(MasterServer.class);

    when(mockMaster.lookupNode(Matchers.<GraphName>any())).thenReturn(null);
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.lookupNode("/caller", "/foo");
    assertEquals(StatusCode.ERROR.toInt(), response.get(0));
    assertEquals("null", response.get(2));
  }

  @Test
  public void testRegisterPublisherWithNoSubscribers() {
    MasterServer mockMaster = mock(MasterServer.class);
    when(mockMaster.registerPublisher(Matchers.<PublisherIdentifier>any(), Matchers.<String>any()))
        .thenReturn(Lists.<SubscriberIdentifier>newArrayList());
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.registerPublisher("/caller", "/foo", "/bar", "http://baz");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(), response.get(2));
  }

  @Test
  public void testRegisterPublisher() {
    MasterServer mockMaster = mock(MasterServer.class);
    final String messageType = "/topicType";
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
    }), argThat(new ArgumentMatcher<String>() {
      @Override
      public boolean matches(Object argument) {
        String messageTypeArg = (String) argument;
        return messageType.equals(messageTypeArg);
      }
    }))).thenReturn(Lists.<SubscriberIdentifier>newArrayList(subscriberIdentifier));
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response =
        master.registerPublisher("/slave", "/topic", messageType, "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(subscriberIdentifier.getUri().toString()), response.get(2));
  }

  @Test
  public void testRegisterSubscriberWithNoSubscribers() {
    MasterServer mockMaster = mock(MasterServer.class);
    when(
        mockMaster.registerSubscriber(Matchers.<SubscriberIdentifier>any(), Matchers.<String>any()))
        .thenReturn(Lists.<PublisherIdentifier>newArrayList());
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.registerSubscriber("/caller", "/foo", "/bar", "http://baz");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(), response.get(2));
  }

  @Test
  public void testRegisterSubscriber() {
    MasterServer mockMaster = mock(MasterServer.class);
    final String topicType = "/topicType";
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
    }), argThat(new ArgumentMatcher<String>() {
      @Override
      public boolean matches(Object argument) {
        String topicTypeArg = (String) argument;
        return topicType.equals(topicTypeArg);
      }
    }))).thenReturn(Lists.<PublisherIdentifier>newArrayList(publisherIdentifier));
    MasterXmlRpcEndpointImpl master = new MasterXmlRpcEndpointImpl(mockMaster);
    List<Object> response = master.registerSubscriber("/slave", "/topic", topicType, "http://api");
    assertEquals(StatusCode.SUCCESS.toInt(), response.get(0));
    assertEquals(Lists.newArrayList(publisherIdentifier.getSlaveUri().toString()), response.get(2));
  }
}
