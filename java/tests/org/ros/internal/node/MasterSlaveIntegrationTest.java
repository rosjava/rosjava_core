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

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpRosServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  private MasterServer masterServer;
  private MasterClient masterClient;
  private SlaveServer slaveServer;
  private SlaveClient slaveClient;

  @Before
  public void setUp() throws XmlRpcException, IOException, URISyntaxException {
    masterServer = new MasterServer(new InetSocketAddress(0));
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    TopicManager topicManager = new TopicManager();
    ServiceManager serviceManager = new ServiceManager();
    slaveServer =
        new SlaveServer("/foo", new InetSocketAddress(0), masterClient, topicManager,
            serviceManager,
            new TcpRosServer(new InetSocketAddress(0), topicManager, serviceManager));
    slaveServer.start();
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

}
