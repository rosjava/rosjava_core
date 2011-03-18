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

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.service.ServiceDefinition;
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.topic.TopicManager;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpRosProtocolDescription;
import org.ros.internal.transport.tcp.TcpServer;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;
import org.ros.message.std_msgs.Int64;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  private MasterServer masterServer;
  private MasterClient masterClient;
  private SlaveServer slaveServer;
  private SlaveClient slaveClient;
  private TopicManager topicManager;
  private ServiceManager serviceManager;
  private TcpServer tcpServer;

  @Before
  public void setUp() throws XmlRpcException, IOException, URISyntaxException {
    masterServer = new MasterServer(new InetSocketAddress(0));
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    tcpServer = new TcpServer(topicManager, serviceManager);
    tcpServer.start(new InetSocketAddress(0));
    slaveServer = new SlaveServer("/foo", masterClient, new InetSocketAddress(0));
    slaveServer.start(tcpServer.getAddress());
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    
    slaveClient = new SlaveClient("/bar", slaveServer.getUri());
  }

  @Test
  public void testGetMasterUri() throws IOException, RemoteException, URISyntaxException {
    Response<URI> response = slaveClient.getMasterUri();
    assertEquals(masterServer.getUri(), response.getResult());
  }

  @Test
  public void testGetPid() throws RemoteException {
    Response<Integer> response = slaveClient.getPid();
    assertTrue(response.getResult() > 0);
  }

  @Test
  public void testAddPublisher() throws Exception {
    TopicDefinition topicDefinition =
        new TopicDefinition("/hello",
            MessageDefinition.createFromMessage(new org.ros.message.std_msgs.String()));
    Publisher<Int64> publisher = new Publisher<Int64>(topicDefinition, Int64.class);
    topicManager.putPublisher(topicDefinition.getName(), publisher);
    try {
      slaveServer.addPublisher(publisher);
      Response<ProtocolDescription> response =
          slaveClient.requestTopic("/hello", Sets.newHashSet(ProtocolNames.TCPROS));
      assertEquals(new TcpRosProtocolDescription(tcpServer.getAddress()), response.getResult());
    } finally {
      publisher.shutdown();
    }
  }

  @Test
  public void testAddSubscriber() throws RemoteException, IOException, URISyntaxException {
    TopicDefinition topicDefinition =
        new TopicDefinition("/hello",
            MessageDefinition.createFromMessage(new org.ros.message.std_msgs.String()));
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/bloop", new URI("http://fake:1234"));
    Subscriber<org.ros.message.std_msgs.String> subscriber =
        Subscriber.create(slaveIdentifier, topicDefinition, org.ros.message.std_msgs.String.class,
            Executors.newCachedThreadPool());
    topicManager.putSubscriber(topicDefinition.getName(), subscriber);
    List<PublisherIdentifier> publishers = slaveServer.addSubscriber(subscriber);
    assertEquals(0, publishers.size());
    Publisher<Int64> publisher = new Publisher<Int64>(topicDefinition, Int64.class);
    slaveServer.addPublisher(publisher);
    topicManager.putPublisher(topicDefinition.getName(), publisher);
    publishers = slaveServer.addSubscriber(subscriber);
    PublisherIdentifier publisherDescription =
        publisher.toPublisherIdentifier(SlaveIdentifier.createAnonymous(slaveServer.getUri()));
    assertTrue(publishers.contains(publisherDescription));

    Response<List<TopicDefinition>> response = slaveClient.getPublications();
    assertEquals(1, response.getResult().size());
    assertTrue(response.getResult().contains(publisher.getTopicDefinition()));
  }

  @Test
  public void testAddService() throws IOException, RemoteException, URISyntaxException {
    ServiceDefinition serviceDefinition =
        new ServiceDefinition(AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum());
    ServiceServer<AddTwoInts.Request> server =
        new ServiceServer<AddTwoInts.Request>(AddTwoInts.Request.class, "/service",
            serviceDefinition) {
          @Override
          public Message buildResponse(AddTwoInts.Request requestMessage) {
            AddTwoInts.Response response = new AddTwoInts.Response();
            response.sum = requestMessage.a + requestMessage.b;
            return response;
          }
        };
        
    ServiceManager serviceManager = new ServiceManager();
    TcpServer tcpServer = new TcpServer(new TopicManager(), serviceManager);
    tcpServer.start(new InetSocketAddress(0));
    server.setAddress(tcpServer.getAddress());
    serviceManager.putService("/service", server);
    slaveServer.addService(server);
    
    Response<URI> response =
        masterClient.lookupService(
            SlaveIdentifier.createAnonymous(new URI("http://localhost:1234")), "/service");
    assertEquals(server.getUri(), response.getResult());
  }

}
