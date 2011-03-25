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

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RuntimeGeneratedMessageTest {

  private RuntimeGeneratedMessageFactory factory;

  @Test
  public void testCreateEmptyMessage() throws IOException {
    factory = new RuntimeGeneratedMessageFactory("");
    factory.createMessage();
  }

  @Test
  public void testCreateEmptyMessageWithBlankLines() throws IOException {
    factory = new RuntimeGeneratedMessageFactory("\n\n\n\n\n");
    factory.createMessage();
  }

  @Test
  public void testString() throws IOException {
    factory = new RuntimeGeneratedMessageFactory("string data");
    String data = "Hello, ROS!";
    Message message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testStringWithComments() throws IOException {
    factory = new RuntimeGeneratedMessageFactory("# foo\nstring data\n    # string other data");
    String data = "Hello, ROS!";
    Message message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getString("data"));
  }

  @Test
  public void testInt8() throws IOException {
    factory = new RuntimeGeneratedMessageFactory("int8 data");
    int data = 42;
    Message message = factory.createMessage();
    message.set("data", data);
    assertEquals(data, message.getInt("data"));
  }

}
