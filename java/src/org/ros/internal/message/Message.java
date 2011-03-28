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
import java.util.Collection;

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

  void setStringArray(String key, Collection<String> value);

  void setInt8Array(String key, Collection<Integer> value);

  void setUint8Array(String key, Collection<Integer> value);

  void setDuration(String key, Collection<Duration> value);

  void setTimeArray(String key, Collection<Time> value);

  void setBoolArray(String key, Collection<Boolean> value);

  void setFloat64Array(String key, Collection<Double> value);

  void setFloat32Array(String key, Collection<Float> value);

  void setUint64Array(String key, Collection<BigInteger> value);

  void setInt64Array(String key, Collection<Long> value);

  void setUint32Array(String key, Collection<Long> value);

  void setInt32Array(String key, Collection<Integer> value);

  void setUint16Array(String key, Collection<Integer> value);

  void setInt16Array(String key, Collection<Integer> value);

  <MessageType extends Message> Collection<MessageType> getMessageArray(String key,
      Class<MessageType> messageClass);

  void setMessageArray(String key, Collection<Message> value);

  Collection<Duration> getDurationArray(String key);

  Collection<Time> getTimeArray(String key);

  Collection<Boolean> getBoolArray(String key);

  Collection<Double> getFloat64Array(String key);

  Collection<Float> getFloat32Array(String key);

  Collection<Long> getUint64Array(String key);

  Collection<Long> getInt64Array(String key);

  Collection<Long> getUint32Array(String key);

  Collection<Integer> getInt32Array(String key);

  Collection<Integer> getUint16Array(String key);

  Collection<Integer> getInt16Array(String key);

  Collection<Integer> getUint8Array(String key);

  Collection<Integer> getInt8Array(String key);

  Collection<String> getStringArray(String key);

}
