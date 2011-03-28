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

import com.google.common.collect.Maps;

import org.ros.message.Duration;
import org.ros.message.Time;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageImpl implements Message {

  private Map<String, FieldType> constantFieldTypes;
  private Map<String, Object> constantFieldValues;
  private Map<String, FieldType> valueFieldTypes;
  private Map<String, Object> valueFieldValues;

  public MessageImpl(Map<String, FieldType> constantFieldTypes,
      Map<String, Object> constantFieldValues, Map<String, FieldType> valueFieldTypes) {
    this.constantFieldTypes = constantFieldTypes;
    this.constantFieldValues = constantFieldValues;
    this.valueFieldTypes = valueFieldTypes;
    valueFieldValues = Maps.newConcurrentMap();
  }

  @SuppressWarnings("unchecked")
  private <T> T getField(String key, FieldType type) {
    if (constantFieldValues.containsKey(key) && constantFieldTypes.get(key) == type) {
      return (T) constantFieldValues.get(key);
    }
    if (valueFieldValues.containsKey(key) && valueFieldTypes.get(key) == type) {
      return (T) valueFieldValues.get(key);
    }
    throw new RuntimeException("Unknown field: " + type + " " + key);
  }

  private void setField(String key, FieldType type, Object value) {
    if (valueFieldTypes.get(key) != type) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  @Override
  public boolean getBool(String key) {
    return getField(key, FieldType.BOOL);
  }

  @Override
  public List<Boolean> getBoolList(String key) {
    return getField(key, FieldType.BOOL_ARRAY);
  }

  @Override
  public Duration getDuration(String key) {
    return getField(key, FieldType.DURATION);
  }

  @Override
  public List<Duration> getDurationList(String key) {
    return getField(key, FieldType.DURATION_ARRAY);
  }

  @Override
  public float getFloat32(String key) {
    return getField(key, FieldType.FLOAT32);
  }

  @Override
  public List<Float> getFloat32List(String key) {
    return getField(key, FieldType.FLOAT32_ARRAY);
  }

  @Override
  public double getFloat64(String key) {
    return getField(key, FieldType.FLOAT64);
  }

  @Override
  public List<Double> getFloat64List(String key) {
    return getField(key, FieldType.FLOAT64_ARRAY);
  }

  @Override
  public int getInt16(String key) {
    return getField(key, FieldType.INT16);
  }

  @Override
  public List<Integer> getInt16List(String key) {
    return getField(key, FieldType.INT16_ARRAY);
  }

  @Override
  public int getInt32(String key) {
    return getField(key, FieldType.INT32);
  }

  @Override
  public List<Integer> getInt32List(String key) {
    return getField(key, FieldType.INT32_ARRAY);
  }

  @Override
  public long getInt64(String key) {
    return getField(key, FieldType.INT64);
  }

  @Override
  public List<Long> getInt64List(String key) {
    return getField(key, FieldType.INT64_ARRAY);
  }

  @Override
  public int getInt8(String key) {
    return getField(key, FieldType.INT8);
  }

  @Override
  public List<Integer> getInt8List(String key) {
    return getField(key, FieldType.INT8_ARRAY);
  }

  @Override
  public <MessageType extends Message> MessageType getMessage(String key,
      Class<MessageType> messageClass) {
    MessageType message = getField(key, FieldType.MESSAGE);
    if (messageClass.isInstance(message)) {
      return message;
    }
    throw new RuntimeException();
  }

  @Override
  public <MessageType extends Message> List<MessageType> getMessageList(String key,
      Class<MessageType> messageClass) {
    List<MessageType> message = getField(key, FieldType.MESSAGE_ARRAY);
    // TODO(damonkohler): Check that all members are of the right type?
    return message;
  }

  @Override
  public String getString(String key) {
    return getField(key, FieldType.STRING);
  }

  @Override
  public List<String> getStringList(String key) {
    return getField(key, FieldType.STRING_ARRAY);
  }

  @Override
  public Time getTime(String key) {
    return getField(key, FieldType.TIME);
  }

  @Override
  public List<Time> getTimeList(String key) {
    return getField(key, FieldType.TIME_ARRAY);
  }

  @Override
  public int getUint16(String key) {
    return getField(key, FieldType.UINT16);
  }

  @Override
  public List<Integer> getUint16List(String key) {
    return getField(key, FieldType.UINT16_ARRAY);
  }

  @Override
  public long getUint32(String key) {
    return getField(key, FieldType.UINT32);
  }

  @Override
  public List<Long> getUint32List(String key) {
    return getField(key, FieldType.UINT32_ARRAY);
  }

  @Override
  public long getUint64(String key) {
    return getField(key, FieldType.UINT64);
  }

  @Override
  public List<Long> getUint64List(String key) {
    return getField(key, FieldType.UINT64_ARRAY);
  }

  @Override
  public int getUint8(String key) {
    return getField(key, FieldType.UINT8);
  }

  @Override
  public List<Integer> getUint8List(String key) {
    return getField(key, FieldType.UINT8_ARRAY);
  }

  @Override
  public void setBool(String key, boolean value) {
    setField(key, FieldType.BOOL, value);
  }

  @Override
  public void setBoolList(String key, List<Boolean> value) {
    setField(key, FieldType.BOOL_ARRAY, value);
  }

  @Override
  public void setDuration(String key, List<Duration> value) {
    setField(key, FieldType.DURATION_ARRAY, value);
  }

  @Override
  public void setDuration(String key, Duration value) {
    setField(key, FieldType.DURATION, value);
  }

  @Override
  public void setFloat32(String key, float value) {
    setField(key, FieldType.FLOAT32, value);
  }

  @Override
  public void setFloat32List(String key, List<Float> value) {
    setField(key, FieldType.FLOAT32_ARRAY, value);
  }

  @Override
  public void setFloat64(String key, double value) {
    setField(key, FieldType.FLOAT64, value);
  }

  @Override
  public void setFloat64List(String key, List<Double> value) {
    setField(key, FieldType.FLOAT64_ARRAY, value);
  }

  @Override
  public void setInt16(String key, int value) {
    setField(key, FieldType.INT16, value);
  }

  @Override
  public void setInt16List(String key, List<Integer> value) {
    setField(key, FieldType.INT16_ARRAY, value);
  }

  @Override
  public void setInt32(String key, int value) {
    setField(key, FieldType.INT32, value);
  }

  @Override
  public void setInt32List(String key, List<Integer> value) {
    setField(key, FieldType.INT32_ARRAY, value);
  }

  @Override
  public void setInt64(String key, long value) {
    setField(key, FieldType.INT64, value);
  }

  @Override
  public void setInt64List(String key, List<Long> value) {
    setField(key, FieldType.INT64_ARRAY, value);
  }

  @Override
  public void setInt8(String key, int value) {
    setField(key, FieldType.INT8, value);
  }

  @Override
  public void setInt8List(String key, List<Integer> value) {
    setField(key, FieldType.INT8_ARRAY, value);
  }

  @Override
  public void setMessage(String key, Message value) {
    setField(key, FieldType.MESSAGE, value);
  }

  @Override
  public void setMessageList(String key, List<Message> value) {
    setField(key, FieldType.MESSAGE_ARRAY, value);
  }

  @Override
  public void setString(String key, String value) {
    setField(key, FieldType.STRING, value);
  }

  @Override
  public void setStringList(String key, List<String> value) {
    setField(key, FieldType.STRING_ARRAY, value);
  }

  @Override
  public void setTime(String key, Time value) {
    setField(key, FieldType.TIME, value);
  }

  @Override
  public void setTimeList(String key, List<Time> value) {
    setField(key, FieldType.TIME_ARRAY, value);
  }

  @Override
  public void setUint16(String key, int value) {
    setField(key, FieldType.UINT16, value);
  }

  @Override
  public void setUint16List(String key, List<Integer> value) {
    setField(key, FieldType.UINT16_ARRAY, value);
  }

  @Override
  public void setUint32(String key, long value) {
    setField(key, FieldType.UINT32, value);
  }

  @Override
  public void setUint32List(String key, List<Long> value) {
    setField(key, FieldType.UINT32_ARRAY, value);
  }

  @Override
  public void setUint64(String key, BigInteger value) {
    setField(key, FieldType.UINT64, value);
  }

  @Override
  public void setUint64List(String key, List<BigInteger> value) {
    setField(key, FieldType.UINT64_ARRAY, value);
  }

  @Override
  public void setUint8(String key, int value) {
    setField(key, FieldType.UINT8, value);
  }

  @Override
  public void setUint8List(String key, List<Integer> value) {
    setField(key, FieldType.UINT8_ARRAY, value);
  }

}
