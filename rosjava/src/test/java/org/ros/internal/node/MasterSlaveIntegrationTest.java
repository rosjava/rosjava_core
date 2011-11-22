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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.TopicManager;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  private MasterServer masterServer;
  private MasterClient masterClient;
  private SlaveServer slaveServer;
  private SlaveClient slaveClient;
  private ExecutorService executorService;

  @Before
  public void setUp() {
	executorService = Executors.newCachedThreadPool();
    masterServer = new MasterServer(BindAddress.newPublic(), AdvertiseAddress.newPublic());
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    TopicManager topicManager = new TopicManager();
    ServiceManager serviceManager = new ServiceManager();
    ParameterManager parameterManager = new ParameterManager();
    slaveServer =
        new SlaveServer(new GraphName("/foo"), BindAddress.newPublic(),
            AdvertiseAddress.newPublic(), BindAddress.newPublic(),
            AdvertiseAddress.newPublic(), masterClient, topicManager, serviceManager,
            parameterManager, executorService);
    slaveServer.start();
    slaveClient = new SlaveClient(new GraphName("/bar"), slaveServer.getUri());
  }

  @After
  public void tearDown() {
    masterServer.shutdown();
    executorService.shutdown();
  }

  @Test
  public void testGetMasterUri() {
    Response<URI> response = slaveClient.getMasterUri();
    assertEquals(masterServer.getUri(), response.getResult());
  }

  @Test
  public void testGetPid() {
    Response<Integer> response = slaveClient.getPid();
    assertTrue(response.getResult() > 0);
  }

}
