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
import java.util.Collection;
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
  public Collection<Boolean> getBoolArray(String key) {
    return getField(key, FieldType.BOOL_ARRAY);
  }

  @Override
  public Duration getDuration(String key) {
    return getField(key, FieldType.DURATION);
  }

  @Override
  public Collection<Duration> getDurationArray(String key) {
    return getField(key, FieldType.DURATION_ARRAY);
  }

  @Override
  public float getFloat32(String key) {
    return getField(key, FieldType.FLOAT32);
  }

  @Override
  public Collection<Float> getFloat32Array(String key) {
    return getField(key, FieldType.FLOAT32_ARRAY);
  }

  @Override
  public double getFloat64(String key) {
    return getField(key, FieldType.FLOAT64);
  }

  @Override
  public Collection<Double> getFloat64Array(String key) {
    return getField(key, FieldType.FLOAT64_ARRAY);
  }

  @Override
  public int getInt16(String key) {
    return getField(key, FieldType.INT16);
  }

  @Override
  public Collection<Integer> getInt16Array(String key) {
    return getField(key, FieldType.INT16_ARRAY);
  }

  @Override
  public int getInt32(String key) {
    return getField(key, FieldType.INT32);
  }

  @Override
  public Collection<Integer> getInt32Array(String key) {
    return getField(key, FieldType.INT32_ARRAY);
  }

  @Override
  public long getInt64(String key) {
    return getField(key, FieldType.INT64);
  }

  @Override
  public Collection<Long> getInt64Array(String key) {
    return getField(key, FieldType.INT64_ARRAY);
  }

  @Override
  public int getInt8(String key) {
    return getField(key, FieldType.INT8);
  }

  @Override
  public Collection<Integer> getInt8Array(String key) {
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
  public <MessageType extends Message> Collection<MessageType> getMessageArray(String key,
      Class<MessageType> messageClass) {
    Collection<MessageType> message = getField(key, FieldType.MESSAGE_ARRAY);
    // TODO(damonkohler): Check that all members are of the right type?
    return message;
  }

  @Override
  public String getString(String key) {
    return getField(key, FieldType.STRING);
  }

  @Override
  public Collection<String> getStringArray(String key) {
    return getField(key, FieldType.STRING_ARRAY);
  }

  @Override
  public Time getTime(String key) {
    return getField(key, FieldType.TIME);
  }

  @Override
  public Collection<Time> getTimeArray(String key) {
    return getField(key, FieldType.TIME_ARRAY);
  }

  @Override
  public int getUint16(String key) {
    return getField(key, FieldType.UINT16);
  }

  @Override
  public Collection<Integer> getUint16Array(String key) {
    return getField(key, FieldType.UINT16_ARRAY);
  }

  @Override
  public long getUint32(String key) {
    return getField(key, FieldType.UINT32);
  }

  @Override
  public Collection<Long> getUint32Array(String key) {
    return getField(key, FieldType.UINT32_ARRAY);
  }

  @Override
  public long getUint64(String key) {
    return getField(key, FieldType.UINT64);
  }

  @Override
  public Collection<Long> getUint64Array(String key) {
    return getField(key, FieldType.UINT64_ARRAY);
  }

  @Override
  public int getUint8(String key) {
    return getField(key, FieldType.UINT8);
  }

  @Override
  public Collection<Integer> getUint8Array(String key) {
    return getField(key, FieldType.UINT8_ARRAY);
  }

  @Override
  public void setBool(String key, boolean value) {
    setField(key, FieldType.BOOL, value);
  }

  @Override
  public void setBoolArray(String key, Collection<Boolean> value) {
    setField(key, FieldType.BOOL_ARRAY, value);
  }

  @Override
  public void setDuration(String key, Collection<Duration> value) {
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
  public void setFloat32Array(String key, Collection<Float> value) {
    setField(key, FieldType.FLOAT32_ARRAY, value);
  }

  @Override
  public void setFloat64(String key, double value) {
    setField(key, FieldType.FLOAT64, value);
  }

  @Override
  public void setFloat64Array(String key, Collection<Double> value) {
    setField(key, FieldType.FLOAT64_ARRAY, value);
  }

  @Override
  public void setInt16(String key, int value) {
    setField(key, FieldType.INT16, value);
  }

  @Override
  public void setInt16Array(String key, Collection<Integer> value) {
    setField(key, FieldType.INT16_ARRAY, value);
  }

  @Override
  public void setInt32(String key, int value) {
    setField(key, FieldType.INT32, value);
  }

  @Override
  public void setInt32Array(String key, Collection<Integer> value) {
    setField(key, FieldType.INT32_ARRAY, value);
  }

  @Override
  public void setInt64(String key, long value) {
    setField(key, FieldType.INT64, value);
  }

  @Override
  public void setInt64Array(String key, Collection<Long> value) {
    setField(key, FieldType.INT64_ARRAY, value);
  }

  @Override
  public void setInt8(String key, int value) {
    setField(key, FieldType.INT8, value);
  }

  @Override
  public void setInt8Array(String key, Collection<Integer> value) {
    setField(key, FieldType.INT8_ARRAY, value);
  }

  @Override
  public void setMessage(String key, Message value) {
    setField(key, FieldType.MESSAGE, value);
  }

  @Override
  public void setMessageArray(String key, Collection<Message> value) {
    setField(key, FieldType.MESSAGE_ARRAY, value);
  }

  @Override
  public void setString(String key, String value) {
    setField(key, FieldType.STRING, value);
  }

  @Override
  public void setStringArray(String key, Collection<String> value) {
    setField(key, FieldType.STRING_ARRAY, value);
  }

  @Override
  public void setTime(String key, Time value) {
    setField(key, FieldType.TIME, value);
  }

  @Override
  public void setTimeArray(String key, Collection<Time> value) {
    setField(key, FieldType.TIME_ARRAY, value);
  }

  @Override
  public void setUint16(String key, int value) {
    setField(key, FieldType.UINT16, value);
  }

  @Override
  public void setUint16Array(String key, Collection<Integer> value) {
    setField(key, FieldType.UINT16_ARRAY, value);
  }

  @Override
  public void setUint32(String key, long value) {
    setField(key, FieldType.UINT32, value);
  }

  @Override
  public void setUint32Array(String key, Collection<Long> value) {
    setField(key, FieldType.UINT32_ARRAY, value);
  }

  @Override
  public void setUint64(String key, BigInteger value) {
    setField(key, FieldType.UINT64, value);
  }

  @Override
  public void setUint64Array(String key, Collection<BigInteger> value) {
    setField(key, FieldType.UINT64_ARRAY, value);
  }

  @Override
  public void setUint8(String key, int value) {
    setField(key, FieldType.UINT8, value);
  }

  @Override
  public void setUint8Array(String key, Collection<Integer> value) {
    setField(key, FieldType.UINT8_ARRAY, value);
  }

}
