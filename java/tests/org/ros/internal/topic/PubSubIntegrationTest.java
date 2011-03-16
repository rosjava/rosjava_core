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

import org.junit.Test;
import org.ros.MessageListener;
import org.ros.internal.node.server.SlaveIdentifier;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PubSubIntegrationTest {

  @Test
  public void testPubSub() throws InterruptedException, URISyntaxException {
    Executor executor = Executors.newCachedThreadPool();
    TopicDefinition topicDefinition =
        new TopicDefinition("/foo",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    SlaveIdentifier pubSlaveIdentifier =
        new SlaveIdentifier("/receiver", new URI("http://fake:5678"));
    PublisherIdentifier publisherIdentifier =
        new PublisherIdentifier(pubSlaveIdentifier, topicDefinition);
    Publisher publisher = new Publisher(topicDefinition);
    publisher.start(new InetSocketAddress(0));

    SlaveIdentifier subSlaveIdentifier =
        new SlaveIdentifier("/caller", new URI("http://fake:1234"));
    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create(subSlaveIdentifier, topicDefinition, org.ros.message.std.String.class,
            executor);
    subscriber.addPublisher(publisherIdentifier, publisher.getAddress());

    final org.ros.message.std.String helloMessage = new org.ros.message.std.String();
    helloMessage.data = "Hello, ROS!";

    final CountDownLatch messageReceived = new CountDownLatch(1);
    subscriber.addMessageListener(new MessageListener<org.ros.message.std.String>() {
      @Override
      public void onNewMessage(org.ros.message.std.String message) {
        assertEquals(helloMessage, message);
        messageReceived.countDown();
      }
    });

    // TODO(damonkohler): This is terrible. However currently I have no reason
    // to add the ability to wait for a handshake to complete other than for
    // this test.
    Thread.sleep(100);

    publisher.publish(helloMessage);
    assertTrue(messageReceived.await(3, TimeUnit.SECONDS));
  }
}
