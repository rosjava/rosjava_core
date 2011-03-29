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

import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.message.std_msgs.Char;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Message {

  boolean getBool(String name);

  byte getByte(String name);

  char getChar(String name);

  Duration getDuration(String name);

  float getFloat32(String name);

  double getFloat64(String name);

  short getInt16(String name);

  int getInt32(String name);

  long getInt64(String name);

  byte getInt8(String name);

  <MessageType extends Message> MessageType getMessage(String name);

  String getString(String name);

  Time getTime(String name);

  int getUint16(String name);

  long getUint32(String name);

  long getUint64(String name);

  short getUint8(String name);

  void setBool(String name, boolean value);

  void setByte(String name, byte value);

  void setChar(String name, char value);

  void setDuration(String name, Duration value);

  void setFloat32(String name, float value);

  void setFloat64(String name, double value);

  void setInt16(String name, short value);

  void setInt32(String name, int value);

  void setInt64(String name, long value);

  void setInt8(String name, byte value);

  void setMessage(String name, Message value);

  void setString(String name, String value);

  void setTime(String name, Time value);

  void setUint16(String name, int value);

  void setUint32(String name, long value);

  void setUint64(String name, long value);

  void setUint8(String name, short value);

  void setStringList(String name, List<String> value);

  void setInt8List(String name, List<Byte> value);

  void setUint8List(String name, List<Short> value);

  void setDuration(String name, List<Duration> value);

  void setTimeList(String name, List<Time> value);

  void setBoolList(String name, List<Boolean> value);

  void setByteList(String name, List<Byte> value);

  void setCharList(String name, List<Char> value);

  void setFloat64List(String name, List<Double> value);

  void setFloat32List(String name, List<Float> value);

  void setUint64List(String name, List<Long> value);

  void setInt64List(String name, List<Long> value);

  void setUint32List(String name, List<Long> value);

  void setInt32List(String name, List<Integer> value);

  void setUint16List(String name, List<Integer> value);

  void setInt16List(String name, List<Short> value);

  <MessageType extends Message> List<MessageType> getMessageList(String name,
      Class<MessageType> messageClass);

  void setMessageList(String name, List<Message> value);

  List<Duration> getDurationList(String name);

  List<Time> getTimeList(String name);

  List<Boolean> getBoolList(String name);

  List<Byte> getByteList(String name);

  List<Char> getCharList(String name);

  List<Double> getFloat64List(String name);

  List<Float> getFloat32List(String name);

  List<Long> getUint64List(String name);

  List<Long> getInt64List(String name);

  List<Long> getUint32List(String name);

  List<Integer> getInt32List(String name);

  List<Integer> getUint16List(String name);

  List<Short> getInt16List(String name);

  List<Short> getUint8List(String name);

  List<Byte> getInt8List(String name);

  List<String> getStringList(String name);

  List<Field> getFields();

}
