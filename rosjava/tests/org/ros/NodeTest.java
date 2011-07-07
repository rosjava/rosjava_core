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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.ros.internal.loader.CommandLineLoader;

import org.ros.node.NodeConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.ros.exception.RosInitException;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.message.MessageListener;
import org.ros.message.std_msgs.Int64;
import org.ros.node.Node;
import org.ros.node.Publisher;
import org.ros.node.Subscriber;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeTest {

  private MasterServer master;
  private URI masterUri;
  private NodeConfiguration nodeConfiguration;

  @Before
  public void setUp() throws RosInitException {
    master = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    master.start();
    masterUri = master.getUri();
    checkHostName(masterUri.getHost());
    // Make sure that none of the publicly reported addresses are bind
    // addresses.
    Map<String, String> env = new HashMap<String, String>();
    env.put("ROS_MASTER_URI", masterUri.toString());
    CommandLineLoader loader = new CommandLineLoader(Lists.<String>newArrayList("Foo"), env);
    nodeConfiguration = loader.createConfiguration();
  }

  @Test
  public void testResolveName() throws RosInitException {
    nodeConfiguration.setParentResolver(Ros.newNameResolver("/ns1"));
    Node node = Ros.newNode("test_resolver", nodeConfiguration);

    assertEquals("/foo", node.resolveName("/foo"));
    assertEquals("/ns1/foo", node.resolveName("foo"));
    assertEquals("/ns1/test_resolver/foo", node.resolveName("~foo"));

    Publisher<Int64> pub = node.newPublisher("pub", "std_msgs/Int64");
    assertEquals("/ns1/pub", pub.getTopicName());
    pub = node.newPublisher("/pub", "std_msgs/Int64");
    assertEquals("/pub", pub.getTopicName());
    pub = node.newPublisher("~pub", "std_msgs/Int64");
    assertEquals("/ns1/test_resolver/pub", pub.getTopicName());

    MessageListener<Int64> callback = new MessageListener<Int64>() {
      @Override
      public void onNewMessage(Int64 message) {
      }
    };

    Subscriber<Int64> sub = node.newSubscriber("sub", "std_msgs/Int64", callback);
    assertEquals("/ns1/sub", sub.getTopicName());
    sub = node.newSubscriber("/sub", "std_msgs/Int64", callback);
    assertEquals("/sub", sub.getTopicName());
    sub = node.newSubscriber("~sub", "std_msgs/Int64", callback);
    assertEquals("/ns1/test_resolver/sub", sub.getTopicName());
  }

  void checkHostName(String hostName) {
    assertTrue(!hostName.equals("0.0.0.0"));
    assertTrue(!hostName.equals("0:0:0:0:0:0:0:0"));
  }

  @Test
  public void testPublicAddresses() throws RosInitException, RemoteException {
    MasterServer master =
        new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    master.start();
    URI masterUri = master.getUri();
    checkHostName(masterUri.getHost());

    // Make sure that none of the publicly reported addresses are bind
    // addresses.
    Map<String, String> env = new HashMap<String, String>();
    env.put("ROS_MASTER_URI", masterUri.toString());
    CommandLineLoader loader = new CommandLineLoader(Lists.<String>newArrayList("Foo"), env);
    NodeConfiguration nodeConfiguration = loader.createConfiguration();

    Node node = Ros.newNode("test_addresses", nodeConfiguration);
    node.newPublisher("test_addresses_pub", "std_msgs/Int64");

    URI uri = node.getUri();
    int port = uri.getPort();
    assertTrue(port > 0);
    checkHostName(uri.getHost());

    // Check the TCPROS server address via the XML-RPC API.
    SlaveClient slaveClient = new SlaveClient(Ros.newGraphName("test_addresses"), uri);
    Response<ProtocolDescription> response =
        slaveClient.requestTopic("test_addresses_pub", Lists.newArrayList(ProtocolNames.TCPROS));
    ProtocolDescription result = response.getResult();
    InetSocketAddress tcpRosAddress = result.getAdverstiseAddress().toInetSocketAddress();
    checkHostName(tcpRosAddress.getHostName());
  }

}
