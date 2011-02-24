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

import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.topic.Subscriber.SubscriberListener;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PubSubIntegrationTest {

  @Test
  public void testPubSub() throws IOException, InterruptedException {
    TopicDefinition topicDefinition =
        new TopicDefinition("/foo",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    Publisher publisher = new Publisher(topicDefinition, "localhost", 0);
    publisher.start();

    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create("/caller", topicDefinition,
            org.ros.message.std.String.class);
    subscriber.start(publisher.getAddress());

    final CountDownLatch messageReceived = new CountDownLatch(1);
    subscriber.addListener(new SubscriberListener<org.ros.message.std.String>() {
      @Override
      public void onNewMessage(org.ros.message.std.String message) {
        assertEquals(message.data, "Hello, ROS!");
        messageReceived.countDown();
      }
    });

    org.ros.message.std.String message = new org.ros.message.std.String();
    message.data = "Hello, ROS!";
    publisher.publish(message);
    assertTrue(messageReceived.await(3, TimeUnit.SECONDS));
  }
}
