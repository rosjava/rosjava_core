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

import java.math.BigInteger;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Message {

  boolean getBool(String key);

  Duration getDuration(String key);

  float getFloat32(String key);

  double getFloat64(String key);

  int getInt16(String key);

  int getInt32(String key);

  long getInt64(String key);

  int getInt8(String key);

  <MessageType extends Message> MessageType getMessage(String key, Class<MessageType> messageClass);

  String getString(String key);

  Time getTime(String key);

  int getUint16(String key);

  long getUint32(String key);

  long getUint64(String key);

  int getUint8(String key);

  void setBool(String key, boolean value);

  void setDuration(String key, Duration value);

  void setFloat32(String key, float value);

  void setFloat64(String key, double value);

  void setInt16(String key, int value);

  void setInt32(String key, int value);

  void setInt64(String key, long value);

  void setInt8(String key, int value);

  void setMessage(String key, Message value);

  void setString(String key, String value);

  void setTime(String key, Time value);

  void setUint16(String key, int value);

  void setUint32(String key, long value);

  void setUint64(String key, BigInteger value);

  void setUint8(String key, int value);

  void setStringList(String key, List<String> value);

  void setInt8List(String key, List<Integer> value);

  void setUint8List(String key, List<Integer> value);

  void setDuration(String key, List<Duration> value);

  void setTimeList(String key, List<Time> value);

  void setBoolList(String key, List<Boolean> value);

  void setFloat64List(String key, List<Double> value);

  void setFloat32List(String key, List<Float> value);

  void setUint64List(String key, List<BigInteger> value);

  void setInt64List(String key, List<Long> value);

  void setUint32List(String key, List<Long> value);

  void setInt32List(String key, List<Integer> value);

  void setUint16List(String key, List<Integer> value);

  void setInt16List(String key, List<Integer> value);

  <MessageType extends Message> List<MessageType> getMessageList(String key,
      Class<MessageType> messageClass);

  void setMessageList(String key, List<Message> value);

  List<Duration> getDurationList(String key);

  List<Time> getTimeList(String key);

  List<Boolean> getBoolList(String key);

  List<Double> getFloat64List(String key);

  List<Float> getFloat32List(String key);

  List<Long> getUint64List(String key);

  List<Long> getInt64List(String key);

  List<Long> getUint32List(String key);

  List<Integer> getInt32List(String key);

  List<Integer> getUint16List(String key);

  List<Integer> getInt16List(String key);

  List<Integer> getUint8List(String key);

  List<Integer> getInt8List(String key);

  List<String> getStringList(String key);

}
