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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageImpl implements Message {

  private MessageContext context;
  private Map<String, Object> valueFieldValues;

  public MessageImpl(MessageContext context) {
    this.context = context;
    valueFieldValues = Maps.newConcurrentMap();
  }

  @SuppressWarnings("unchecked")
  private <T> T getField(String name, FieldType type) {
    if (context.hasConstantField(name, type)) {
      return (T) context.getConstant(name);
    }
    if (context.hasValueField(name, type)) {
      return (T) valueFieldValues.get(name);
    }
    throw new RuntimeException("Unknown field: " + type + " " + name);
  }

  @SuppressWarnings("unchecked")
  private <T> T getMessageValueField(String name) {
    if (context.hasMessageValueField(name)) {
      return (T) valueFieldValues.get(name);
    }
    throw new RuntimeException();
  }

  private void setValueField(String name, PrimitiveFieldType type, Object value) {
    if (context.hasValueField(name, type)) {
      valueFieldValues.put(name, value);
    } else {
      throw new RuntimeException("Unable to set value field: " + type + " " + name + " = " + value);
    }
  }

  private void setMessageValueField(String name, Object value) {
    if (context.hasMessageValueField(name)) {
      valueFieldValues.put(name, value);
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public Collection<Field> getFields() {
    return context.getFields();
  }

  @Override
  public boolean getBool(String key) {
    return getField(key, PrimitiveFieldType.BOOL);
  }

  @Override
  public List<Boolean> getBoolList(String key) {
    return getField(key, PrimitiveFieldType.BOOL);
  }

  @Override
  public Duration getDuration(String key) {
    return getField(key, PrimitiveFieldType.DURATION);
  }

  @Override
  public List<Duration> getDurationList(String key) {
    return getField(key, PrimitiveFieldType.DURATION);
  }

  @Override
  public float getFloat32(String key) {
    return getField(key, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public List<Float> getFloat32List(String key) {
    return getField(key, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public double getFloat64(String key) {
    return getField(key, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public List<Double> getFloat64List(String key) {
    return getField(key, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public short getInt16(String key) {
    return getField(key, PrimitiveFieldType.INT16);
  }

  @Override
  public List<Short> getInt16List(String key) {
    return getField(key, PrimitiveFieldType.INT16);
  }

  @Override
  public int getInt32(String key) {
    return getField(key, PrimitiveFieldType.INT32);
  }

  @Override
  public List<Integer> getInt32List(String key) {
    return getField(key, PrimitiveFieldType.INT32);
  }

  @Override
  public long getInt64(String key) {
    return getField(key, PrimitiveFieldType.INT64);
  }

  @Override
  public List<Long> getInt64List(String key) {
    return getField(key, PrimitiveFieldType.INT64);
  }

  @Override
  public byte getInt8(String key) {
    return getField(key, PrimitiveFieldType.INT8);
  }

  @Override
  public List<Byte> getInt8List(String key) {
    return getField(key, PrimitiveFieldType.INT8);
  }

  @Override
  public <MessageType extends Message> MessageType getMessage(String key) {
    return getMessageValueField(key);
  }

  @Override
  public <MessageType extends Message> List<MessageType> getMessageList(String key,
      Class<MessageType> messageClass) {
    List<MessageType> message = getMessageValueField(key);
    // TODO(damonkohler): Check that all members are of the right type?
    return message;
  }

  @Override
  public String getString(String key) {
    return getField(key, PrimitiveFieldType.STRING);
  }

  @Override
  public List<String> getStringList(String key) {
    return getField(key, PrimitiveFieldType.STRING);
  }

  @Override
  public Time getTime(String key) {
    return getField(key, PrimitiveFieldType.TIME);
  }

  @Override
  public List<Time> getTimeList(String key) {
    return getField(key, PrimitiveFieldType.TIME);
  }

  @Override
  public int getUint16(String key) {
    return getField(key, PrimitiveFieldType.UINT16);
  }

  @Override
  public List<Integer> getUint16List(String key) {
    return getField(key, PrimitiveFieldType.UINT16);
  }

  @Override
  public long getUint32(String key) {
    return getField(key, PrimitiveFieldType.UINT32);
  }

  @Override
  public List<Long> getUint32List(String key) {
    return getField(key, PrimitiveFieldType.UINT32);
  }

  @Override
  public long getUint64(String key) {
    return getField(key, PrimitiveFieldType.UINT64);
  }

  @Override
  public List<Long> getUint64List(String key) {
    return getField(key, PrimitiveFieldType.UINT64);
  }

  @Override
  public short getUint8(String key) {
    return getField(key, PrimitiveFieldType.UINT8);
  }

  @Override
  public List<Short> getUint8List(String key) {
    return getField(key, PrimitiveFieldType.UINT8);
  }

  @Override
  public void setBool(String key, boolean value) {
    setValueField(key, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setBoolList(String key, List<Boolean> value) {
    setValueField(key, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setDuration(String key, List<Duration> value) {
    setValueField(key, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setDuration(String key, Duration value) {
    setValueField(key, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setFloat32(String key, float value) {
    setValueField(key, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat32List(String key, List<Float> value) {
    setValueField(key, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat64(String key, double value) {
    setValueField(key, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setFloat64List(String key, List<Double> value) {
    setValueField(key, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setInt16(String key, short value) {
    setValueField(key, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt16List(String key, List<Short> value) {
    setValueField(key, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt32(String key, int value) {
    setValueField(key, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt32List(String key, List<Integer> value) {
    setValueField(key, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt64(String key, long value) {
    setValueField(key, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt64List(String key, List<Long> value) {
    setValueField(key, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt8(String key, byte value) {
    setValueField(key, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setInt8List(String key, List<Byte> value) {
    setValueField(key, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setMessage(String key, Message value) {
    setMessageValueField(key, value);
  }

  @Override
  public void setMessageList(String key, List<Message> value) {
    setMessageValueField(key, value);
  }

  @Override
  public void setString(String key, String value) {
    setValueField(key, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setStringList(String key, List<String> value) {
    setValueField(key, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setTime(String key, Time value) {
    setValueField(key, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setTimeList(String key, List<Time> value) {
    setValueField(key, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setUint16(String key, int value) {
    setValueField(key, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint16List(String key, List<Integer> value) {
    setValueField(key, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint32(String key, long value) {
    setValueField(key, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint32List(String key, List<Long> value) {
    setValueField(key, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint64(String key, long value) {
    setValueField(key, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint64List(String key, List<Long> value) {
    setValueField(key, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint8(String key, short value) {
    setValueField(key, PrimitiveFieldType.UINT8, value);
  }

  @Override
  public void setUint8List(String key, List<Short> value) {
    setValueField(key, PrimitiveFieldType.UINT8, value);
  }

}
