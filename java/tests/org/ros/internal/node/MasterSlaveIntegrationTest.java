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

package org.ros.internal.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.ros.internal.transport.tcp.TcpRosProtocolDescription;

import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;

import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;

import org.ros.internal.service.ServiceDefinition;
import org.ros.internal.service.ServiceServer;

import org.ros.internal.node.RemoteException;
import org.ros.internal.node.Response;

import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;

import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;


import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  private MasterServer masterServer;
  private MasterClient masterClient;
  private SlaveServer slaveServer;
  private SlaveClient slaveClient;

  @Before
  public void setUp() throws XmlRpcException, IOException {
    masterServer = new MasterServer("localhost", 0);
    masterServer.start();
    masterClient = new MasterClient(masterServer.getAddress());
    slaveServer = new SlaveServer("/foo", masterClient, "localhost", 0);
    slaveServer.start();
    slaveClient = new SlaveClient("/bar", slaveServer.getAddress());
  }

  @Test
  public void testGetMasterUri() throws IOException, RemoteException {
    Response<URL> response = Response.checkOk(slaveClient.getMasterUri());
    assertEquals(masterServer.getAddress(), response.getValue());
  }

  @Test
  public void testAddPublisher() throws RemoteException, IOException {
    TopicDefinition topicDefinition =
        new TopicDefinition("/hello",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    Publisher publisher = new Publisher(topicDefinition, "localhost", 0);
    slaveServer.addPublisher(publisher);
    Response<ProtocolDescription> response =
        Response.checkOk(slaveClient.requestTopic("/hello", Sets.newHashSet(ProtocolNames.TCPROS)));
    assertEquals(response.getValue(), new TcpRosProtocolDescription(publisher.getAddress()));
  }

  @Test
  public void testAddSubscriber() throws RemoteException, IOException {
    TopicDefinition topicDefinition =
        new TopicDefinition("/hello",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create("/bloop", topicDefinition, org.ros.message.std.String.class);
    List<PublisherIdentifier> publishers = slaveServer.addSubscriber(subscriber);
    assertEquals(0, publishers.size());
    Publisher publisher = new Publisher(topicDefinition, "localhost", 0);
    slaveServer.addPublisher(publisher);
    publishers = slaveServer.addSubscriber(subscriber);
    PublisherIdentifier publisherDescription =
        publisher.toPublisherIdentifier(slaveServer.toSlaveIdentifier());
    assertTrue(publishers.contains(publisherDescription));

    Response<List<TopicDefinition>> response =
        Response.checkOk(slaveClient.getPublications());
    assertEquals(1, response.getValue().size());
    assertTrue(response.getValue().contains(publisher.getTopicDefinition()));
  }

  @Test
  public void testAddService() throws IOException, RemoteException, URISyntaxException {
    ServiceDefinition serviceDefinition =
        new ServiceDefinition(AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum());
    ServiceServer<AddTwoInts.Request> server =
        new ServiceServer<AddTwoInts.Request>(AddTwoInts.Request.class, "/service",
            serviceDefinition, "localhost", 0) {
          @Override
          public Message buildResponse(AddTwoInts.Request requestMessage) {
            AddTwoInts.Response response = new AddTwoInts.Response();
            response.sum = requestMessage.a + requestMessage.b;
            return response;
          }
        };
    slaveServer.addService(server);
    Response<URI> response = Response.checkOk(masterClient.lookupService("/foo", "/service"));
    assertEquals(server.getUri(), response.getValue());
  }

}
