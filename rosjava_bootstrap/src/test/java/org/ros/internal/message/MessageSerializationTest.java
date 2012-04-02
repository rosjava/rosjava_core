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

package org.ros.internal.message;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.topic.TopicDefinitionResourceProvider;
import org.ros.message.MessageFactory;

import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationTest {

  private TopicDefinitionResourceProvider topicDefinitionResourceProvider;
  private MessageFactory messageFactory;

  @Before
  public void setUp() {
    topicDefinitionResourceProvider = new TopicDefinitionResourceProvider();
    messageFactory = new DefaultMessageFactory(topicDefinitionResourceProvider);
  }

  private void checkSerializeAndDeserialize(Message message) {
    ByteBuffer buffer = message.serialize();
    DefaultMessageDeserializer<Message> deserializer =
        new DefaultMessageDeserializer<Message>(message.getIdentifier(), messageFactory);
    assertEquals(message, deserializer.deserialize(buffer));
  }

  @Test
  public void testInt32() {
    Message message = messageFactory.newFromType("std_msgs/Int32");
    message.setInt32("data", 42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testString() {
    Message message = messageFactory.newFromType("std_msgs/String");
    message.setString("data", "Hello, ROS!");
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testNestedMessage() {
    topicDefinitionResourceProvider.add("foo/foo", "std_msgs/String data");
    Message fooMessage = messageFactory.newFromType("foo/foo");
    Message stringMessage = messageFactory.newFromType("std_msgs/String");
    stringMessage.setString("data", "Hello, ROS!");
    fooMessage.setMessage("data", stringMessage);
    checkSerializeAndDeserialize(fooMessage);
  }

  @Test
  public void testNestedMessageArray() {
    topicDefinitionResourceProvider.add("foo/foo", "std_msgs/String[] data");
    Message fooMessage = messageFactory.newFromType("foo/foo");
    Message stringMessageA = messageFactory.newFromType("std_msgs/String");
    stringMessageA.setString("data", "Hello, ROS!");
    Message stringMessageB = messageFactory.newFromType("std_msgs/String");
    stringMessageB.setString("data", "Goodbye, ROS!");
    fooMessage.setMessageList("data", Lists.newArrayList(stringMessageA, stringMessageB));
    checkSerializeAndDeserialize(fooMessage);
  }

  @Test
  public void testInt32Array() {
    topicDefinitionResourceProvider.add("foo/foo", "int32[] data");
    Message message = messageFactory.newFromType("foo/foo");
    message.setInt32List("data", Lists.newArrayList(1, 2, 3, 4, 5));
    checkSerializeAndDeserialize(message);
  }
}
