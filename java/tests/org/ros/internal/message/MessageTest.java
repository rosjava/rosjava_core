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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

  private MessageFactory<FooMessage> factory;

  @Test
  public void testCreateEmptyMessage() throws IOException {
    factory = new MessageFactory<FooMessage>("", FooMessage.class);
    factory.createMessage();
  }

  @Test
  public void testCreateEmptyMessageWithBlankLines() throws IOException {
    factory = new MessageFactory<FooMessage>("\n\n\n\n\n", FooMessage.class);
    factory.createMessage();
  }

  @Test
  public void testString() throws IOException {
    factory = new MessageFactory<FooMessage>("string data", FooMessage.class);
    String data = "Hello, ROS!";
    FooMessage message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testStringWithComments() throws IOException {
    factory =
        new MessageFactory<FooMessage>("# foo\nstring data\n    # string other data",
            FooMessage.class);
    String data = "Hello, ROS!";
    FooMessage message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testInt8() throws IOException {
    factory = new MessageFactory<FooMessage>("int8 data", FooMessage.class);
    int data = 42;
    FooMessage message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getInt("data"));
  }

  @Test
  public void testNestedMessage() throws IOException {
    factory = new MessageFactory<FooMessage>("BarMessage bar", FooMessage.class);
    MessageFactory<BarMessage> barFactory =
        new MessageFactory<BarMessage>("int8 data", BarMessage.class);
    FooMessage message = factory.createMessage();
    BarMessage barMessage = barFactory.createMessage();
    message.set("bar", barMessage);
    int data = 42;
    barMessage.set("data", data);
    assertEquals(data, message.getMessage("bar", BarMessage.class).getInt("data"));
  }

  @Test
  public void testConstantInt8() throws IOException {
    factory = new MessageFactory<FooMessage>("int8 data=42", FooMessage.class);
    FooMessage message = factory.createMessage();
    assertEquals(42, message.getInt("data"));
  }

  @Test
  public void testConstantString() throws IOException {
    factory =
        new MessageFactory<FooMessage>("string data=Hello, ROS! # comment ", FooMessage.class);
    FooMessage message = factory.createMessage();
    assertEquals("Hello, ROS! # comment", message.getString("data"));
  }

  @Test
  public void testCreateAllStdMsgs() throws IOException {
    URL resource = this.getClass().getResource("/data/std_msgs");
    MessageLoader loader = new MessageLoader();
    File searchPath = new File(resource.getPath());
    loader.addSearchPath(searchPath);
    loader.updateMessageDefinitions();
    Map<String, String> definitions = loader.getMessageDefinitions();
    for (Entry<String, String> definition : definitions.entrySet()) {
      factory = new MessageFactory<FooMessage>(definition.getValue(), FooMessage.class);
      FooMessage message = factory.createMessage();
      System.out.println(definition.getKey() + ": " + definition.getValue());
    }
  }

}
