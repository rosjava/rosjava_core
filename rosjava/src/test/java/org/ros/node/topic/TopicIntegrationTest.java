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

package org.ros.node.topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.RepeatingPublisher;
import org.ros.message.MessageListener;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicIntegrationTest {

  private MasterServer masterServer;
  private NodeFactory nodeFactory;
  private NodeConfiguration nodeConfiguration;
  private ExecutorService executorService;

  @Before
  public void setUp() {
    executorService = Executors.newCachedThreadPool();
    masterServer = new MasterServer(BindAddress.newPublic(), AdvertiseAddress.newPublic());
    masterServer.start();
    nodeConfiguration =
        NodeConfiguration.newPrivate(masterServer.getUri()).setExecutorService(executorService);
    nodeFactory = new DefaultNodeFactory();
  }

  @Test
  public void testOnePublisherToOneSubscriber() throws InterruptedException {
    nodeConfiguration.setNodeName("publisher");
    Node publisherNode = nodeFactory.newNode(nodeConfiguration);

    CountDownPublisherListener publisherListener = new CountDownPublisherListener();
    Publisher<org.ros.message.std_msgs.String> publisher =
        publisherNode.newPublisher("foo", "std_msgs/String");
    publisher.addPublisherListener(publisherListener);

    final org.ros.message.std_msgs.String helloMessage = new org.ros.message.std_msgs.String();
    helloMessage.data = "Hello, ROS!";

    final CountDownLatch messageReceived = new CountDownLatch(1);
    nodeConfiguration.setNodeName("subscriber");
    Node subscriberNode = nodeFactory.newNode(nodeConfiguration);

    CountDownSubscriberListener subscriberListener = new CountDownSubscriberListener();
    Subscriber<org.ros.message.std_msgs.String> subscriber =
        subscriberNode.newSubscriber("foo", "std_msgs/String");
    subscriber.addMessageListener(new MessageListener<org.ros.message.std_msgs.String>() {
      @Override
      public void onNewMessage(org.ros.message.std_msgs.String message) {
        assertEquals(helloMessage, message);
        messageReceived.countDown();
      }
    });
    subscriber.addSubscriberListener(subscriberListener);

    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));
    assertTrue(subscriberListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));

    RepeatingPublisher<org.ros.message.std_msgs.String> repeatingPublisher =
        new RepeatingPublisher<org.ros.message.std_msgs.String>(publisher, helloMessage, 1000,
            executorService);
    repeatingPublisher.start();

    assertTrue(messageReceived.await(1, TimeUnit.SECONDS));

    repeatingPublisher.cancel();
    publisher.shutdown();
  }

  @Test
  public void testAddDisconnectedPublisher() {
    nodeConfiguration.setNodeName("subscriber");
    Node subscriberNode = nodeFactory.newNode(nodeConfiguration);
    org.ros.internal.node.topic.DefaultSubscriber<org.ros.message.std_msgs.String> subscriber =
        (org.ros.internal.node.topic.DefaultSubscriber<org.ros.message.std_msgs.String>) subscriberNode
            .<org.ros.message.std_msgs.String>newSubscriber("foo", "std_msgs/String");

    try {
      subscriber.addPublisher(PublisherIdentifier.newFromStrings("foo", "http://foo", "foo"),
          new InetSocketAddress(1234));
      fail();
    } catch (RuntimeException e) {
      // Connecting to a disconnected publisher should fail.
    }
  }

  private class Listener implements MessageListener<org.ros.message.test_ros.TestHeader> {

    private final CountDownLatch latch = new CountDownLatch(10);

    private org.ros.message.test_ros.TestHeader lastMessage;

    @Override
    public void onNewMessage(org.ros.message.test_ros.TestHeader message) {
      if (lastMessage != null) {
        assertTrue(String.format("message seq %d <= previous seq %d", message.header.seq,
            lastMessage.header.seq), message.header.seq > lastMessage.header.seq);
        assertTrue(message.header.stamp.compareTo(lastMessage.header.stamp) > 0);
      }
      lastMessage = message;
      latch.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
      return latch.await(timeout, unit);
    }
  }

  @Test
  public void testHeader() throws InterruptedException {
    nodeConfiguration.setNodeName("publisher");
    final Node publisherNode = nodeFactory.newNode(nodeConfiguration);
    CountDownPublisherListener publisherListener = new CountDownPublisherListener();
    final Publisher<org.ros.message.test_ros.TestHeader> publisher =
        publisherNode.newPublisher("foo", "test_ros/TestHeader");
    publisher.addPublisherListener(publisherListener);

    nodeConfiguration.setNodeName("subscriber");
    Node subscriberNode = nodeFactory.newNode(nodeConfiguration);
    Listener listener = new Listener();
    CountDownSubscriberListener subscriberListener = new CountDownSubscriberListener();
    Subscriber<org.ros.message.test_ros.TestHeader> subscriber =
        subscriberNode.newSubscriber("foo", "test_ros/TestHeader");
    subscriber.addMessageListener(listener);
    subscriber.addSubscriberListener(subscriberListener);

    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.DAYS));
    assertTrue(subscriberListener.awaitMasterRegistrationSuccess(1, TimeUnit.DAYS));

    CancellableLoop publisherLoop = new CancellableLoop() {
      @Override
      public void loop() throws InterruptedException {
        org.ros.message.test_ros.TestHeader headerMessage =
            publisherNode.getMessageFactory().newMessage("test_ros/TestHeader");
        headerMessage.header.frame_id = "frame";
        headerMessage.header.stamp = publisherNode.getCurrentTime();
        publisher.publish(headerMessage);
        // There needs to be some time between messages in order to
        // guarantee that the timestamp increases.
        Thread.sleep(1);
      }
    };
    executorService.execute(publisherLoop);
    assertTrue(listener.await(1, TimeUnit.DAYS));
    publisherLoop.cancel();

    publisherNode.shutdown();
    subscriberNode.shutdown();
  }
}
