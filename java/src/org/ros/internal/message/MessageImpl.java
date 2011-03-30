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
import org.ros.message.std_msgs.Char;

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
  public List<Field> getFields() {
    return context.getFields();
  }

  @Override
  public boolean getBool(String name) {
    return this.<Boolean>getField(name, PrimitiveFieldType.BOOL);
  }

  @Override
  public List<Boolean> getBoolList(String name) {
    return this.<List<Boolean>>getField(name, PrimitiveFieldType.BOOL);
  }

  @Override
  public Duration getDuration(String name) {
    return this.<Duration>getField(name, PrimitiveFieldType.DURATION);
  }

  @Override
  public List<Duration> getDurationList(String name) {
    return this.<List<Duration>>getField(name, PrimitiveFieldType.DURATION);
  }

  @Override
  public float getFloat32(String name) {
    return this.<Float>getField(name, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public List<Float> getFloat32List(String name) {
    return this.<List<Float>>getField(name, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public double getFloat64(String name) {
    return this.<Double>getField(name, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public List<Double> getFloat64List(String name) {
    return this.<List<Double>>getField(name, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public short getInt16(String name) {
    return this.<Short>getField(name, PrimitiveFieldType.INT16);
  }

  @Override
  public List<Short> getInt16List(String name) {
    return this.<List<Short>>getField(name, PrimitiveFieldType.INT16);
  }

  @Override
  public int getInt32(String name) {
    return this.<Integer>getField(name, PrimitiveFieldType.INT32);
  }

  @Override
  public List<Integer> getInt32List(String name) {
    return this.<List<Integer>>getField(name, PrimitiveFieldType.INT32);
  }

  @Override
  public long getInt64(String name) {
    return this.<Long>getField(name, PrimitiveFieldType.INT64);
  }

  @Override
  public List<Long> getInt64List(String name) {
    return this.<List<Long>>getField(name, PrimitiveFieldType.INT64);
  }

  @Override
  public byte getInt8(String name) {
    return this.<Byte>getField(name, PrimitiveFieldType.INT8);
  }

  @Override
  public List<Byte> getInt8List(String name) {
    return this.<List<Byte>>getField(name, PrimitiveFieldType.INT8);
  }

  @Override
  public <MessageType extends Message> MessageType getMessage(String name) {
    return this.<MessageType>getMessageValueField(name);
  }

  @Override
  public <MessageType extends Message> List<MessageType> getMessageList(String name,
      Class<MessageType> messageClass) {
    List<MessageType> message = getMessageValueField(name);
    // TODO(damonkohler): Check that all members are of the right type?
    return message;
  }

  @Override
  public String getString(String name) {
    return getField(name, PrimitiveFieldType.STRING);
  }

  @Override
  public List<String> getStringList(String name) {
    return getField(name, PrimitiveFieldType.STRING);
  }

  @Override
  public Time getTime(String name) {
    return getField(name, PrimitiveFieldType.TIME);
  }

  @Override
  public List<Time> getTimeList(String name) {
    return getField(name, PrimitiveFieldType.TIME);
  }

  @Override
  public int getUint16(String name) {
    return this.<Integer>getField(name, PrimitiveFieldType.UINT16);
  }

  @Override
  public List<Integer> getUint16List(String name) {
    return this.<List<Integer>>getField(name, PrimitiveFieldType.UINT16);
  }

  @Override
  public long getUint32(String name) {
    return this.<Long>getField(name, PrimitiveFieldType.UINT32);
  }

  @Override
  public List<Long> getUint32List(String name) {
    return this.<List<Long>>getField(name, PrimitiveFieldType.UINT32);
  }

  @Override
  public long getUint64(String name) {
    return this.<Long>getField(name, PrimitiveFieldType.UINT64);
  }

  @Override
  public List<Long> getUint64List(String name) {
    return this.<List<Long>>getField(name, PrimitiveFieldType.UINT64);
  }

  @Override
  public short getUint8(String name) {
    return this.<Short>getField(name, PrimitiveFieldType.UINT8);
  }

  @Override
  public List<Short> getUint8List(String name) {
    return this.<List<Short>>getField(name, PrimitiveFieldType.UINT8);
  }

  @Override
  public void setBool(String name, boolean value) {
    setValueField(name, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setBoolList(String name, List<Boolean> value) {
    setValueField(name, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setDuration(String name, List<Duration> value) {
    setValueField(name, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setDuration(String name, Duration value) {
    setValueField(name, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setFloat32(String name, float value) {
    setValueField(name, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat32List(String name, List<Float> value) {
    setValueField(name, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat64(String name, double value) {
    setValueField(name, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setFloat64List(String name, List<Double> value) {
    setValueField(name, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setInt16(String name, short value) {
    setValueField(name, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt16List(String name, List<Short> value) {
    setValueField(name, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt32(String name, int value) {
    setValueField(name, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt32List(String name, List<Integer> value) {
    setValueField(name, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt64(String name, long value) {
    setValueField(name, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt64List(String name, List<Long> value) {
    setValueField(name, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt8(String name, byte value) {
    setValueField(name, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setInt8List(String name, List<Byte> value) {
    setValueField(name, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setMessage(String name, Message value) {
    setMessageValueField(name, value);
  }

  @Override
  public void setMessageList(String name, List<Message> value) {
    setMessageValueField(name, value);
  }

  @Override
  public void setString(String name, String value) {
    setValueField(name, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setStringList(String name, List<String> value) {
    setValueField(name, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setTime(String name, Time value) {
    setValueField(name, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setTimeList(String name, List<Time> value) {
    setValueField(name, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setUint16(String name, int value) {
    setValueField(name, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint16List(String name, List<Integer> value) {
    setValueField(name, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint32(String name, long value) {
    setValueField(name, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint32List(String name, List<Long> value) {
    setValueField(name, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint64(String name, long value) {
    setValueField(name, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint64List(String name, List<Long> value) {
    setValueField(name, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint8(String name, short value) {
    setValueField(name, PrimitiveFieldType.UINT8, value);
  }

  @Override
  public void setUint8List(String name, List<Short> value) {
    setValueField(name, PrimitiveFieldType.UINT8, value);
  }

  @Override
  public byte getByte(String name) {
    return this.<Byte>getField(name, PrimitiveFieldType.BYTE);
  }

  @Override
  public char getChar(String name) {
    return this.<Character>getField(name, PrimitiveFieldType.CHAR);
  }

  @Override
  public void setByte(String name, byte value) {
    setValueField(name, PrimitiveFieldType.BYTE, value);
  }

  @Override
  public void setChar(String name, char value) {
    setValueField(name, PrimitiveFieldType.CHAR, value);
  }

  @Override
  public void setByteList(String name, List<Byte> value) {
    setValueField(name, PrimitiveFieldType.BYTE, value);
  }

  @Override
  public void setCharList(String name, List<Char> value) {
    setValueField(name, PrimitiveFieldType.CHAR, value);
  }

  @Override
  public List<Byte> getByteList(String name) {
    return getField(name, PrimitiveFieldType.BYTE);
  }

  @Override
  public List<Char> getCharList(String name) {
    return getField(name, PrimitiveFieldType.CHAR);
  }

}
