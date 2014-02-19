/*
 * Copyright (C) 2012 Google Inc.
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

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.message.Duration;
import org.ros.message.Time;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author mick.gaillard@gmail.com (Mickael Gaillard)
 */
public class MessageSerializationTest {

  private MessageDefinitionReflectionProvider messageDefinitionReflectionProvider;
  private DefaultMessageFactory defaultMessageFactory;
  private DefaultMessageSerializer serializer;

  private interface Nested extends Message {
    static final java.lang.String _TYPE = "test/Nested";
    static final java.lang.String _DEFINITION = "std_msgs/String data\n";

    std_msgs.String getData();

    void setData(std_msgs.String value);
  }

  private interface NestedList extends Message {
    static final java.lang.String _TYPE = "test/NestedList";
    static final java.lang.String _DEFINITION = "std_msgs/String[] data\n";

    List<std_msgs.String> getData();

    void setData(List<std_msgs.String> value);
  }

  @Before
  public void before() {
    messageDefinitionReflectionProvider = new MessageDefinitionReflectionProvider();
    messageDefinitionReflectionProvider.add(Nested._TYPE, Nested._DEFINITION);
    messageDefinitionReflectionProvider.add(NestedList._TYPE, NestedList._DEFINITION);
    defaultMessageFactory = new DefaultMessageFactory(messageDefinitionReflectionProvider);
    defaultMessageFactory.getMessageInterfaceClassProvider().add(Nested._TYPE, Nested.class);
    defaultMessageFactory.getMessageInterfaceClassProvider()
        .add(NestedList._TYPE, NestedList.class);
    serializer = new DefaultMessageSerializer();
  }

  private <T extends Message> void checkSerializeAndDeserialize(T message) {
    ChannelBuffer buffer = MessageBuffers.dynamicBuffer();
    serializer.serialize(message, buffer);
    dumpBuffer(buffer);
    DefaultMessageDeserializer<T> deserializer =
        new DefaultMessageDeserializer<T>(message.toRawMessage().getIdentifier(),
            defaultMessageFactory);
    assertEquals(message, deserializer.deserialize(buffer));
  }

  @Test
  public void testBool() {
    std_msgs.Bool message = defaultMessageFactory.newFromType(std_msgs.Bool._TYPE);
    message.setData(true);
    checkSerializeAndDeserialize(message);
    message.setData(false);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testInt8() {
    std_msgs.Int8 message = defaultMessageFactory.newFromType(std_msgs.Int8._TYPE);
    message.setData((byte) 42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testUint8() {
    std_msgs.UInt8 message = defaultMessageFactory.newFromType(std_msgs.UInt8._TYPE);
    message.setData((byte) 42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testInt16() {
    std_msgs.Int16 message = defaultMessageFactory.newFromType(std_msgs.Int16._TYPE);
    message.setData((short) 42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testUInt16() {
    std_msgs.UInt16 message = defaultMessageFactory.newFromType(std_msgs.UInt16._TYPE);
    message.setData((short) 42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testInt32() {
    std_msgs.Int32 message = defaultMessageFactory.newFromType(std_msgs.Int32._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testUInt32() {
    std_msgs.UInt32 message = defaultMessageFactory.newFromType(std_msgs.UInt32._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testInt64() {
    std_msgs.Int64 message = defaultMessageFactory.newFromType(std_msgs.Int64._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testUInt64() {
    std_msgs.UInt64 message = defaultMessageFactory.newFromType(std_msgs.UInt64._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testFloat32() {
    std_msgs.Float32 message = defaultMessageFactory.newFromType(std_msgs.Float32._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testFloat64() {
    std_msgs.Float64 message = defaultMessageFactory.newFromType(std_msgs.Float64._TYPE);
    message.setData(42);
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testString() {
    std_msgs.String message = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
    message.setData("Hello, ROS!");
    checkSerializeAndDeserialize(message);
  }
  
  @Test
  public void testStringUTF8() {
    std_msgs.String message = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
    message.setData("éêè €àáßëœ 文字化け");
    checkSerializeAndDeserialize(message);

    // i18n test case
    // base on http://www.inter-locale.com/whitepaper/learn/learn-to-test.html

    // Combining Marks and Accents test
    message.setData("àéîōũ");
    checkSerializeAndDeserialize(message);

    // DOS 860 test
    message.setData("você nós mãe avô irmã criança");
    checkSerializeAndDeserialize(message);

    // Windows-1252 test
    message.setData("€ŒœŠš™©‰ƒ");
    checkSerializeAndDeserialize(message);

    // Turkish test
    message.setData("ışık bir İyi Günler");
    checkSerializeAndDeserialize(message);

    // Dakuten and handakuten marks test
    message.setData("がざばだぱか゛さ゛た゛は");
    checkSerializeAndDeserialize(message);

    // Combining Grapheme Joiner character
    message.setData("אִ͏ַ");
    checkSerializeAndDeserialize(message);

    // Bidi with Latin test
    message.setData("abcאבגדabc ");
    checkSerializeAndDeserialize(message);

    message.setData("אבגדabcאבגד");
    checkSerializeAndDeserialize(message);

    message.setData("אבגד012אבגד");
    checkSerializeAndDeserialize(message);

    message.setData("אבגד 012 012");
    checkSerializeAndDeserialize(message);

    // Complex Scripts test
    message.setData("สวัสดี");
    checkSerializeAndDeserialize(message);

    message.setData("டாஹ்கோ");
    checkSerializeAndDeserialize(message);

    message.setData("بِسْمِ اللّهِ الرَّحْمـَنِ الرَّحِيمِ");
    checkSerializeAndDeserialize(message);

    // Numeric Shaping test
    message.setData("عدد مارس ١٩٩٨");
    checkSerializeAndDeserialize(message);

    // Common Scripts and Encodings test
    message.setData("Слава Жанна Ювеналий Ярополк");
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testTime() {
    std_msgs.Time message = defaultMessageFactory.newFromType(std_msgs.Time._TYPE);
    message.setData(new Time());
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testDuration() {
    std_msgs.Duration message = defaultMessageFactory.newFromType(std_msgs.Duration._TYPE);
    message.setData(new Duration());
    checkSerializeAndDeserialize(message);
  }

  @Test
  public void testNestedMessage() {
    Nested nestedMessage = defaultMessageFactory.newFromType(Nested._TYPE);
    std_msgs.String stringMessage = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
    stringMessage.setData("Hello, ROS!");
    nestedMessage.setData(stringMessage);
    checkSerializeAndDeserialize(nestedMessage);
  }

  @Test
  public void testNestedMessageList() {
    messageDefinitionReflectionProvider.add(NestedList._TYPE, NestedList._DEFINITION);
    NestedList nestedListMessage = defaultMessageFactory.newFromType(NestedList._TYPE);
    std_msgs.String stringMessageA = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
    stringMessageA.setData("Hello, ROS!");
    std_msgs.String stringMessageB = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
    stringMessageB.setData("Hello, ROS!");
    nestedListMessage.setData(Lists.<std_msgs.String>newArrayList(stringMessageA, stringMessageB));
    checkSerializeAndDeserialize(nestedListMessage);
  }

  /**
   * Regression test for issue 125.
   */
  @Test
  public void testOdometry() {
    nav_msgs.Odometry message = defaultMessageFactory.newFromType(nav_msgs.Odometry._TYPE);
    checkSerializeAndDeserialize(message);
    ChannelBuffer buffer = MessageBuffers.dynamicBuffer();
    serializer.serialize(message, buffer);
    dumpBuffer(buffer);
    // Throw away sequence number.
    buffer.readInt();
    while (buffer.readable()) {
      byte b = buffer.readByte();
      assertEquals("All serialized bytes should be 0. Check stdout.", 0, b);
    }
  }

  private void dumpBuffer(ChannelBuffer buffer) {
    buffer = buffer.duplicate();
    System.out.printf("Dumping %d readable bytes:\n", buffer.readableBytes());
    int i = 0;
    while (buffer.readable()) {
      byte b = buffer.readByte();
      System.out.printf("0x%02x ", b);
      if (++i % 8 == 0) {
        System.out.print("   ");
      }
      if (i % 16 == 0) {
        System.out.print("\n");
      }
    }
    System.out.print("\n\n");
  }
}
