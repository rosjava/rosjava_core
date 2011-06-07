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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.MessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
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
    String hostName = InetAddress.getLocalHost().getCanonicalHostName();
    Node node =
        Node.createPublic(new GraphName("/node_name"), masterServer.getUri(), hostName, 0, 0);
    try {
      node.start();
      fail();
    } catch (RuntimeException e) {
      // Calling start() while the node is running must fail.
    }
  }

  @Test
  public void testFailIfStoppedWhileNotRunning() throws UnknownHostException {
    String hostName = InetAddress.getLocalHost().getCanonicalHostName();
    Node node =
        Node.createPublic(new GraphName("/node_name"), masterServer.getUri(), hostName, 0, 0);
    node.shutdown();
    try {
      node.shutdown();
      fail();
    } catch (RuntimeException e) {
      // Calling stop() while the node is not running must fail.
    }
  }

  @Test
  public void testCreatePublic() throws Exception {
    String hostName = InetAddress.getLocalHost().getCanonicalHostName();
    Node node =
        Node.createPublic(new GraphName("/node_name"), masterServer.getUri(), hostName, 0, 0);

    InetSocketAddress tcpRosAddress = node.getSlaveServer().getAddress();
    assertTrue(tcpRosAddress.getPort() > 0);
    assertEquals(tcpRosAddress.getHostName(), hostName);

    URI uri = node.getSlaveServer().getUri();
    assertTrue(uri.getPort() > 0);
    assertEquals(hostName, uri.getHost());

    node.shutdown();
  }

  @Test
  public void testCreatePrivate() {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);

    InetSocketAddress tcpRosAddress = node.getSlaveServer().getAddress();
    assertTrue(tcpRosAddress.getPort() > 0);
    assertTrue(tcpRosAddress.getAddress().isLoopbackAddress());

    URI uri = node.getSlaveServer().getUri();
    assertTrue(uri.getPort() > 0);
    assertTrue(new InetSocketAddress(uri.getHost(), uri.getPort()).getAddress().isLoopbackAddress());

    node.shutdown();
  }

  @Test
  public void testPubSubRegistration() throws InterruptedException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    TopicDefinition topicDefinition =
        new TopicDefinition(new GraphName("/foo"), MessageDefinition.create(
            org.ros.message.std_msgs.String.__s_getDataType(),
            org.ros.message.std_msgs.String.__s_getMessageDefinition(),
            org.ros.message.std_msgs.String.__s_getMD5Sum()));

    Publisher<org.ros.message.std_msgs.String> publisher =
        node.createPublisher(topicDefinition,
            new MessageSerializer<org.ros.message.std_msgs.String>());
    assertTrue(publisher.awaitRegistration(1, TimeUnit.SECONDS));

    Subscriber<org.ros.message.std_msgs.String> subscriber =
        node.createSubscriber(topicDefinition, org.ros.message.std_msgs.String.class,
            new MessageDeserializer<org.ros.message.std_msgs.String>(
                org.ros.message.std_msgs.String.class));
    assertTrue(subscriber.awaitRegistration(1, TimeUnit.SECONDS));

    assertEquals(1, masterServer.getRegisteredPublishers().size());
    assertEquals(1, masterServer.getRegisteredSubscribers().size());

    node.shutdown();

    assertEquals(0, masterServer.getRegisteredPublishers().size());
    assertEquals(0, masterServer.getRegisteredSubscribers().size());
  }

}
