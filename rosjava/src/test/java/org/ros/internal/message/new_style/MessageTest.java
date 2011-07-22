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

package org.ros.internal.message.new_style;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageTest {

  private interface FooMessage extends Message {
  }

  private interface BarMessage extends Message {
  }

  private MessageLoader loader;
  private DefaultedClassMap<Message> classes;
  private MessageFactory factory;

  @Before
  public void setUp() {
    loader = new MessageLoader();
    URL resource = this.getClass().getResource("/data/std_msgs");
    File searchPath = new File(resource.getPath());
    loader.addSearchPath(searchPath);
    loader.updateMessageDefinitions();
    classes = DefaultedClassMap.create(Message.class);
    classes.put("foo", FooMessage.class);
    classes.put("bar", BarMessage.class);
    factory = new MessageFactory(loader, classes);
  }

  @Test
  public void testCreateEmptyMessage() {
    loader.addMessageDefinition("foo", "");
    factory.createMessage("foo");
  }

  @Test
  public void testCreateEmptyMessageWithBlankLines() {
    loader.addMessageDefinition("foo", "\n\n\n\n\n");
    factory.createMessage("foo");
  }

  @Test
  public void testString() {
    String data = "Hello, ROS!";
    Message message = factory.createMessage("std_msgs/String");
    message.setString("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testStringWithComments() {
    loader.addMessageDefinition("foo", "# foo\nstring data\n    # string other data");
    String data = "Hello, ROS!";
    FooMessage message = factory.createMessage("foo");
    message.setString("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testInt8() {
    byte data = 42;
    Message message = factory.createMessage("std_msgs/Int8");
    message.setInt8("data", data);
    assertEquals(data, message.getInt8("data"));
  }

  @Test
  public void testNestedMessage() {
    loader.addMessageDefinition("foo", "bar data");
    loader.addMessageDefinition("bar", "int8 data");
    FooMessage fooMessage = factory.createMessage("foo");
    BarMessage barMessage = factory.createMessage("bar");
    fooMessage.setMessage("data", barMessage);
    byte data = 42;
    barMessage.setInt8("data", data);
    assertEquals(data, fooMessage.getMessage("data").getInt8("data"));
  }

  @Test
  public void testConstantInt8() {
    loader.addMessageDefinition("foo", "int8 data=42");
    FooMessage message = factory.createMessage("foo");
    assertEquals(42, message.getInt8("data"));
  }

  @Test
  public void testConstantString() {
    loader.addMessageDefinition("foo", "string data=Hello, ROS! # comment ");
    FooMessage message = factory.createMessage("foo");
    assertEquals("Hello, ROS! # comment", message.getString("data"));
  }

  public void testInt8List() {
    loader.addMessageDefinition("foo", "int8[] data");
    FooMessage message = factory.createMessage("foo");
    ArrayList<Byte> data = Lists.newArrayList((byte) 1, (byte) 2, (byte) 3);
    message.setInt8List("data", data);
    assertEquals(data, message.getInt8List("data"));
  }

  @Test
  public void testCreateAllStdMsgs() {
    URL resource = this.getClass().getResource("/data/std_msgs");
    File searchPath = new File(resource.getPath());
    loader.addSearchPath(searchPath);
    loader.updateMessageDefinitions();
    Map<String, String> definitions = loader.getMessageDefinitions();
    for (Entry<String, String> definition : definitions.entrySet()) {
      factory.createMessage(definition.getKey());
    }
  }

  @Test
  public void testCreateAllTestRospyMsgs() {
    URL resource = this.getClass().getResource("/data/test_rospy");
    File searchPath = new File(resource.getPath());
    loader.addSearchPath(searchPath);
    resource = this.getClass().getResource("/data/test_ros");
    searchPath = new File(resource.getPath());
    loader.addSearchPath(searchPath);
    loader.updateMessageDefinitions();
    Map<String, String> definitions = loader.getMessageDefinitions();
    for (Entry<String, String> definition : definitions.entrySet()) {
      factory.createMessage(definition.getKey());
    }
  }

}
