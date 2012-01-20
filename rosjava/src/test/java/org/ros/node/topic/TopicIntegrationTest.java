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

import org.junit.Test;
import org.ros.RosTest;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.node.topic.DefaultSubscriber;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Make sure publishers can talk with subscribers over a network connection.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicIntegrationTest extends RosTest {

  @Test
  public void testOnePublisherToOneSubscriber() throws InterruptedException {
    final org.ros.message.std_msgs.String helloMessage = new org.ros.message.std_msgs.String();
    helloMessage.data = "Hello, ROS!";

    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        Publisher<org.ros.message.std_msgs.String> publisher =
            node.newPublisher("foo", "std_msgs/String");
        publisher.setLatchMode(true);
        publisher.publish(helloMessage);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("publisher");
      }
    }, nodeConfiguration);

    final CountDownLatch messageReceived = new CountDownLatch(1);
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        Subscriber<org.ros.message.std_msgs.String> subscriber =
            node.newSubscriber("foo", "std_msgs/String");
        subscriber.addMessageListener(new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onNewMessage(org.ros.message.std_msgs.String message) {
            assertEquals(helloMessage, message);
            messageReceived.countDown();
          }
        });
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("subscriber");
      }
    }, nodeConfiguration);

    assertTrue(messageReceived.await(10, TimeUnit.SECONDS));
  }

  /**
   * This is a regression test.
   * 
   * @see <a
   *      href="http://answers.ros.org/question/3591/rosjava-subscriber-unreliable">bug
   *      report</a>
   * 
   * @throws InterruptedException
   */
  @Test
  public void testSubscriberStartsBeforePublisher() throws InterruptedException {
    final org.ros.message.std_msgs.String helloMessage = new org.ros.message.std_msgs.String();
    helloMessage.data = "Hello, ROS!";

    final CountDownSubscriberListener<org.ros.message.std_msgs.String> subscriberListener =
        CountDownSubscriberListener.newDefault();
    final CountDownLatch messageReceived = new CountDownLatch(1);
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        Subscriber<org.ros.message.std_msgs.String> subscriber =
            node.newSubscriber("foo", "std_msgs/String");
        subscriber.addSubscriberListener(subscriberListener);
        subscriber.addMessageListener(new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onNewMessage(org.ros.message.std_msgs.String message) {
            assertEquals(helloMessage, message);
            messageReceived.countDown();
          }
        });
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("subscriber");
      }
    }, nodeConfiguration);

    subscriberListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS);

    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        Publisher<org.ros.message.std_msgs.String> publisher =
            node.newPublisher("foo", "std_msgs/String");
        publisher.setLatchMode(true);
        publisher.publish(helloMessage);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("publisher");
      }
    }, nodeConfiguration);

    assertTrue(messageReceived.await(10, TimeUnit.SECONDS));
  }

  @Test
  public void testAddDisconnectedPublisher() {
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        DefaultSubscriber<org.ros.message.std_msgs.String> subscriber =
            (DefaultSubscriber<org.ros.message.std_msgs.String>) node
                .<org.ros.message.std_msgs.String>newSubscriber("foo", "std_msgs/String");
        try {
          subscriber.addPublisher(PublisherIdentifier.newFromStrings("foo", "http://foo", "foo"),
              new InetSocketAddress(1234));
          fail();
        } catch (RuntimeException e) {
          // Connecting to a disconnected publisher should fail.
        }
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("subscriber");
      }
    }, nodeConfiguration);
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
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(final Node node) {
        final Publisher<org.ros.message.test_ros.TestHeader> publisher =
            node.newPublisher("foo", "test_ros/TestHeader");
        CancellableLoop cancellableLoop = new CancellableLoop() {
          @Override
          public void loop() throws InterruptedException {
            org.ros.message.test_ros.TestHeader headerMessage =
                node.getMessageFactory().newMessage("test_ros/TestHeader");
            headerMessage.header.frame_id = "frame";
            headerMessage.header.stamp = node.getCurrentTime();
            publisher.publish(headerMessage);
            // There needs to be some time between messages in order to
            // guarantee that the timestamp increases.
            Thread.sleep(1);
          }
        };
        node.executeCancellableLoop(cancellableLoop);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("publisher");
      }
    }, nodeConfiguration);

    final Listener listener = new Listener();
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        Subscriber<org.ros.message.test_ros.TestHeader> subscriber =
            node.newSubscriber("foo", "test_ros/TestHeader");
        subscriber.addMessageListener(listener);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("subscriber");
      }
    }, nodeConfiguration);

    assertTrue(listener.await(10, TimeUnit.SECONDS));
  }
}
