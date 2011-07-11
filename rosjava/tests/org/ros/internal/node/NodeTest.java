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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.net.InetAddresses;

import org.junit.Before;
import org.junit.Test;
import org.ros.Ros;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;
import org.ros.node.Node;
import org.ros.node.Publisher;
import org.ros.node.Subscriber;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeTest {

  private MasterServer masterServer;

  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    masterServer.start();
  }

  @Test
  public void testFailIfStartedWhileRunning() throws UnknownHostException {
    String host = InetAddress.getLocalHost().getCanonicalHostName();
    try {
      Ros.newNode("/node_name", Ros.newPublicNodeConfiguration(host, masterServer.getUri()));
      fail();
    } catch (RuntimeException e) {
      // Calling start() while the node is running must fail.
    }
  }

  @Test
  public void testFailIfStoppedWhileNotRunning() throws UnknownHostException {
    String host = InetAddress.getLocalHost().getCanonicalHostName();
    try {
      Ros.newNode("/node_name", Ros.newPublicNodeConfiguration(host, masterServer.getUri()));
      fail();
    } catch (RuntimeException e) {
      // Calling stop() while the node is not running must fail.
    }
  }

  @Test
  public void testCreatePublic() throws Exception {
    String host = InetAddress.getLocalHost().getCanonicalHostName();
    assertFalse(InetAddresses.isInetAddress(host));
    Node node =
        Ros.newNode("/node_name", Ros.newPublicNodeConfiguration(host, masterServer.getUri()));
    InetSocketAddress nodeAddress = node.getAddress();
    assertTrue(nodeAddress.getPort() > 0);
    assertEquals(nodeAddress.getHostName(), host);
    node.shutdown();
  }

  @Test
  public void testCreatePublicWithIpv4() throws Exception {
    String host = "1.2.3.4";
    Node node =
        Ros.newNode("/node_name", Ros.newPublicNodeConfiguration(host, masterServer.getUri()));
    InetSocketAddress nodeAddress = node.getAddress();
    assertTrue(nodeAddress.getPort() > 0);
    assertEquals(nodeAddress.getHostName(), host);
    node.shutdown();
  }

  @Test
  public void testCreatePublicWithIpv6() throws Exception {
    String host = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    Node node =
        Ros.newNode("/node_name", Ros.newPublicNodeConfiguration(host, masterServer.getUri()));
    InetSocketAddress nodeAddress = node.getAddress();
    assertTrue(nodeAddress.getPort() > 0);
    assertEquals(nodeAddress.getHostName(), host);
    node.shutdown();
  }

  @Test
  public void testCreatePrivate() {
    Node node = Ros.newNode("/node_name", Ros.newPrivateNodeConfiguration(masterServer.getUri()));
    InetSocketAddress nodeAddress = node.getAddress();
    assertTrue(nodeAddress.getPort() > 0);
    assertTrue(nodeAddress.getAddress().isLoopbackAddress());
    node.shutdown();
  }

  @Test
  public void testPubSubRegistration() throws InterruptedException {
    Node node = Ros.newNode("/node_name", Ros.newPrivateNodeConfiguration(masterServer.getUri()));

    Publisher<org.ros.message.std_msgs.String> publisher =
        node.newPublisher("/foo", "std_msgs/String");
    assertTrue(publisher.awaitRegistration(1, TimeUnit.SECONDS));

    Subscriber<org.ros.message.std_msgs.String> subscriber =
        node.newSubscriber("/foo", "std_msgs/String", null);
    assertTrue(subscriber.awaitRegistration(1, TimeUnit.SECONDS));

    assertEquals(1, masterServer.getRegisteredPublishers().size());
    assertEquals(1, masterServer.getRegisteredSubscribers().size());

    node.shutdown();

    assertEquals(0, masterServer.getRegisteredPublishers().size());
    assertEquals(0, masterServer.getRegisteredSubscribers().size());
  }

}
