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

package org.ros.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.ros.message.MessageDescription;
import org.ros.topic.Publisher;
import org.ros.topic.PublisherDescription;
import org.ros.topic.Subscriber;
import org.ros.topic.TopicDescription;
import org.ros.transport.ProtocolDescription;
import org.ros.transport.ProtocolNames;
import org.ros.transport.tcp.TcpRosProtocolDescription;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  private org.ros.node.server.Master masterServer;
  private org.ros.node.client.Master masterClient;
  private org.ros.node.server.Slave slaveServer;
  private org.ros.node.client.Slave slaveClient;

  @Before
  public void setUp() throws XmlRpcException, IOException {
    masterServer = new org.ros.node.server.Master("localhost", 0);
    masterServer.start();
    masterClient = new org.ros.node.client.Master(masterServer.getAddress());
    slaveServer = new org.ros.node.server.Slave("/foo", masterClient, "localhost", 0);
    slaveServer.start();
    slaveClient = new org.ros.node.client.Slave(slaveServer.getAddress());
  }

  @Test
  public void testGetMasterUri() throws IOException, RemoteException {
    Response<URL> response = Response.checkOk(slaveClient.getMasterUri("/caller"));
    assertEquals(masterServer.getAddress(), response.getValue());
  }

  @Test
  public void testAddPublisher() throws RemoteException, IOException {
    TopicDescription topicDescription =
        new TopicDescription("/hello",
            MessageDescription.createFromMessage(new org.ros.message.std.String()));
    Publisher publisher = new Publisher(topicDescription, "localhost", 0);
    slaveServer.addPublisher(publisher);
    Response<ProtocolDescription> response =
        Response.checkOk(slaveClient.requestTopic("/caller", "/hello",
            Sets.newHashSet(ProtocolNames.TCPROS)));
    assertEquals(response.getValue(), new TcpRosProtocolDescription(publisher.getAddress()));
  }

  @Test
  public void testAddSubscriber() throws RemoteException, IOException {
    TopicDescription topicDescription =
        new TopicDescription("/hello",
            MessageDescription.createFromMessage(new org.ros.message.std.String()));
    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create("/bloop", topicDescription, org.ros.message.std.String.class);
    List<PublisherDescription> publishers = slaveServer.addSubscriber(subscriber);
    assertTrue(publishers.size() == 0);
    Publisher publisher = new Publisher(topicDescription, "localhost", 0);
    slaveServer.addPublisher(publisher);
    publishers = slaveServer.addSubscriber(subscriber);
    PublisherDescription publisherDescription =
        publisher.toPublisherDescription(slaveServer.toSlaveDescription());
    assertTrue(publishers.contains(publisherDescription));
  }

}
