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

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterSlaveIntegrationTest {

  @Test
  public void testMasterSlaveGetMasterUri() throws XmlRpcException, IOException, RemoteException {
    org.ros.node.server.Master masterServer = new org.ros.node.server.Master("localhost", 0);
    masterServer.start();

    org.ros.node.client.Master masterClient =
        new org.ros.node.client.Master(masterServer.getAddress());
    org.ros.node.server.Slave slaveServer =
        new org.ros.node.server.Slave("/foo", masterClient, "localhost", 0);
    slaveServer.start();

    org.ros.node.client.Slave slaveClient = new org.ros.node.client.Slave(slaveServer.getAddress());
    Response<URL> response = Response.checkOk(slaveClient.getMasterUri("/foo"));
    assertEquals(masterServer.getAddress(), response.getValue());
  }

}
