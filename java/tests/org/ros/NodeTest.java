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

package org.ros;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.node.NodeSocketAddress;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.message.std_msgs.Int64;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeTest {

  void checkHostName(String hostName) {
    assertTrue(!hostName.equals("0.0.0.0"));
    assertTrue(!hostName.equals("0:0:0:0:0:0:0:0"));
  }

  @Test
  public void testPublicAddresses() throws RosInitException, RosNameException, RemoteException,
      XmlRpcException, IOException, URISyntaxException {
    MasterServer master = new MasterServer(NodeSocketAddress.createDefault(0));
    master.start();
    URI masterUri = master.getUri();
    checkHostName(masterUri.getHost());

    // Make sure that none of the publicly reported addresses are bind
    // addresses.
    Map<String, String> env = new HashMap<String, String>();
    env.put("ROS_MASTER_URI", masterUri.toString());
    CommandLineLoader loader = new CommandLineLoader(new String[] {}, env);
    NodeContext nodeContext = loader.createContext();

    Node node = new Node("test_addresses", nodeContext);
    node.init();
    node.createPublisher("test_addresses_pub", Int64.class);

    URI uri = node.getUri();
    int port = uri.getPort();
    assertTrue(port > 0);
    checkHostName(uri.getHost());

    // check the TCPROS server address via the XMLRPC api.
    SlaveClient slaveClient = new SlaveClient("test_addresses", uri);
    Response<ProtocolDescription> response = slaveClient.requestTopic("test_addresses_pub",
        Lists.newArrayList(ProtocolNames.TCPROS));
    ProtocolDescription result = response.getResult();
    InetSocketAddress tcpRosAddress = (InetSocketAddress) result.getAddress();
    checkHostName(tcpRosAddress.getHostName());
  }
}
