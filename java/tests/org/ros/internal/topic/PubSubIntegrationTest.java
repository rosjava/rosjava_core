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

package org.ros.internal.topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.ros.internal.transport.tcp.TcpServer;

import org.ros.message.std.Int64;

import org.junit.Before;
import org.junit.Test;
import org.ros.MessageListener;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveIdentifier;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PubSubIntegrationTest {

  private TopicDefinition topicDefinition;

  class PublisherServer {
    final TcpServer tcpServer;
    final Publisher<?> publisher;

    public PublisherServer(Publisher<?> publisher, TcpServer tcpServer) {
      this.tcpServer = tcpServer;
      this.publisher = publisher;
    }
  }

  private PublisherServer createPublisherServer() {
    // Create a new tcpRosServer for each publisher so that we can test multiple
    // connections.
    TopicManager topicManager = new TopicManager();
    ServiceManager serviceManager = new ServiceManager();
    TcpServer tcpServer = new TcpServer(topicManager, serviceManager);
    tcpServer.start(new InetSocketAddress(0));
    Publisher<Int64> publisher = new Publisher<Int64>(topicDefinition, Int64.class);
    topicManager.putPublisher(topicDefinition.getName(), publisher);
    publisher.start();
    return new PublisherServer(publisher, tcpServer);
  }

  @Before
  public void setup() {
    topicDefinition =
        new TopicDefinition("/foo",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
  }

  @Test
  public void testPubSub() throws Exception {
    Collection<PublisherServer> publisherServers = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      publisherServers.add(createPublisherServer());
    }

    SlaveIdentifier subSlaveIdentifier =
        new SlaveIdentifier("/caller", new URI("http://fake:1234"));
    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create(subSlaveIdentifier, topicDefinition, org.ros.message.std.String.class,
            Executors.newCachedThreadPool());

    for (PublisherServer publisherServer : publisherServers) {
      SlaveIdentifier publisherSlaveIdentifier =
          new SlaveIdentifier("/receiver", new URI("http://fake:5678"));
      PublisherIdentifier publisherIdentifier =
          new PublisherIdentifier(publisherSlaveIdentifier, topicDefinition);
      InetSocketAddress publisherAddress =
          new InetSocketAddress("localhost", publisherServer.tcpServer.getAddress().getPort());
      subscriber.addPublisher(publisherIdentifier, publisherAddress);
    }

    final org.ros.message.std.String helloMessage = new org.ros.message.std.String();
    helloMessage.data = "Hello, ROS!";

    final CountDownLatch messageReceived = new CountDownLatch(publisherServers.size());
    subscriber.addMessageListener(new MessageListener<org.ros.message.std.String>() {
      @Override
      public void onNewMessage(org.ros.message.std.String message) {
        assertEquals(helloMessage, message);
        messageReceived.countDown();
      }
    });

    // TODO(damonkohler): This is terrible. However, currently I have no reason
    // to add the ability to wait for a handshake to complete other than for
    // this test.
    Thread.sleep(100);

    for (PublisherServer publisherServer : publisherServers) {
      publisherServer.publisher.publish(helloMessage);
    }
    assertTrue(messageReceived.await(3, TimeUnit.SECONDS));
  }
}
