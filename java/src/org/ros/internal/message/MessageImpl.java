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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageImpl implements Message, GetInstance {

  private MessageContext context;

  public MessageImpl(MessageContext context) {
    this.context = context;
  }

  @SuppressWarnings("unchecked")
  private <T> T getFieldValue(String name, FieldType type) {
    if (context.hasField(name, type)) {
      return (T) context.getField(name).getValue();
    }
    throw new RuntimeException("Unknown field: " + type + " " + name);
  }

  private <T> void setFieldValue(String name, FieldType type, T value) {
    if (context.hasField(name, type)) {
      context.getField(name).setValue(value);
    } else {
      throw new RuntimeException("Unable to set field value: " + type + " " + name + " = " + value);
    }
  }

  @Override
  public String getName() {
    return context.getName();
  }

  @Override
  public List<Field<?>> getFields() {
    return context.getFields();
  }

  @Override
  public boolean getBool(String name) {
    return this.<Boolean>getFieldValue(name, PrimitiveFieldType.BOOL);
  }

  @Override
  public List<Boolean> getBoolList(String name) {
    return this.<List<Boolean>>getFieldValue(name, PrimitiveFieldType.BOOL);
  }

  @Override
  public Duration getDuration(String name) {
    return this.<Duration>getFieldValue(name, PrimitiveFieldType.DURATION);
  }

  @Override
  public List<Duration> getDurationList(String name) {
    return this.<List<Duration>>getFieldValue(name, PrimitiveFieldType.DURATION);
  }

  @Override
  public float getFloat32(String name) {
    return this.<Float>getFieldValue(name, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public List<Float> getFloat32List(String name) {
    return this.<List<Float>>getFieldValue(name, PrimitiveFieldType.FLOAT32);
  }

  @Override
  public double getFloat64(String name) {
    return this.<Double>getFieldValue(name, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public List<Double> getFloat64List(String name) {
    return this.<List<Double>>getFieldValue(name, PrimitiveFieldType.FLOAT64);
  }

  @Override
  public short getInt16(String name) {
    return this.<Short>getFieldValue(name, PrimitiveFieldType.INT16);
  }

  @Override
  public List<Short> getInt16List(String name) {
    return this.<List<Short>>getFieldValue(name, PrimitiveFieldType.INT16);
  }

  @Override
  public int getInt32(String name) {
    return this.<Integer>getFieldValue(name, PrimitiveFieldType.INT32);
  }

  @Override
  public List<Integer> getInt32List(String name) {
    return this.<List<Integer>>getFieldValue(name, PrimitiveFieldType.INT32);
  }

  @Override
  public long getInt64(String name) {
    return this.<Long>getFieldValue(name, PrimitiveFieldType.INT64);
  }

  @Override
  public List<Long> getInt64List(String name) {
    return this.<List<Long>>getFieldValue(name, PrimitiveFieldType.INT64);
  }

  @Override
  public byte getInt8(String name) {
    return this.<Byte>getFieldValue(name, PrimitiveFieldType.INT8);
  }

  @Override
  public List<Byte> getInt8List(String name) {
    return this.<List<Byte>>getFieldValue(name, PrimitiveFieldType.INT8);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType extends Message> MessageType getMessage(String name) {
    if (context.getField(name).getType() instanceof MessageFieldType) {
      return (MessageType) context.getField(name).getValue();
    }
    throw new RuntimeException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType extends Message> List<MessageType> getMessageList(String name) {
    if (context.getField(name).getType() instanceof MessageFieldType) {
      return (List<MessageType>) context.getField(name).getValue();
    }
    throw new RuntimeException();
  }

  @Override
  public String getString(String name) {
    return getFieldValue(name, PrimitiveFieldType.STRING);
  }

  @Override
  public List<String> getStringList(String name) {
    return getFieldValue(name, PrimitiveFieldType.STRING);
  }

  @Override
  public Time getTime(String name) {
    return getFieldValue(name, PrimitiveFieldType.TIME);
  }

  @Override
  public List<Time> getTimeList(String name) {
    return getFieldValue(name, PrimitiveFieldType.TIME);
  }

  @Override
  public int getUint16(String name) {
    return this.<Integer>getFieldValue(name, PrimitiveFieldType.UINT16);
  }

  @Override
  public List<Integer> getUint16List(String name) {
    return this.<List<Integer>>getFieldValue(name, PrimitiveFieldType.UINT16);
  }

  @Override
  public long getUint32(String name) {
    return this.<Long>getFieldValue(name, PrimitiveFieldType.UINT32);
  }

  @Override
  public List<Long> getUint32List(String name) {
    return this.<List<Long>>getFieldValue(name, PrimitiveFieldType.UINT32);
  }

  @Override
  public long getUint64(String name) {
    return this.<Long>getFieldValue(name, PrimitiveFieldType.UINT64);
  }

  @Override
  public List<Long> getUint64List(String name) {
    return this.<List<Long>>getFieldValue(name, PrimitiveFieldType.UINT64);
  }

  @Override
  public short getUint8(String name) {
    return this.<Short>getFieldValue(name, PrimitiveFieldType.UINT8);
  }

  @Override
  public List<Short> getUint8List(String name) {
    return this.<List<Short>>getFieldValue(name, PrimitiveFieldType.UINT8);
  }

  @Override
  public void setBool(String name, boolean value) {
    setFieldValue(name, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setBoolList(String name, List<Boolean> value) {
    setFieldValue(name, PrimitiveFieldType.BOOL, value);
  }

  @Override
  public void setDurationList(String name, List<Duration> value) {
    setFieldValue(name, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setDuration(String name, Duration value) {
    setFieldValue(name, PrimitiveFieldType.DURATION, value);
  }

  @Override
  public void setFloat32(String name, float value) {
    setFieldValue(name, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat32List(String name, List<Float> value) {
    setFieldValue(name, PrimitiveFieldType.FLOAT32, value);
  }

  @Override
  public void setFloat64(String name, double value) {
    setFieldValue(name, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setFloat64List(String name, List<Double> value) {
    setFieldValue(name, PrimitiveFieldType.FLOAT64, value);
  }

  @Override
  public void setInt16(String name, short value) {
    setFieldValue(name, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt16List(String name, List<Short> value) {
    setFieldValue(name, PrimitiveFieldType.INT16, value);
  }

  @Override
  public void setInt32(String name, int value) {
    setFieldValue(name, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt32List(String name, List<Integer> value) {
    setFieldValue(name, PrimitiveFieldType.INT32, value);
  }

  @Override
  public void setInt64(String name, long value) {
    setFieldValue(name, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt64List(String name, List<Long> value) {
    setFieldValue(name, PrimitiveFieldType.INT64, value);
  }

  @Override
  public void setInt8(String name, byte value) {
    setFieldValue(name, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setInt8List(String name, List<Byte> value) {
    setFieldValue(name, PrimitiveFieldType.INT8, value);
  }

  @Override
  public void setMessage(String name, Message value) {
    // TODO(damonkohler): Verify the type of the provided Message?
    context.getField(name).setValue(value);
  }

  @Override
  public void setMessageList(String name, List<Message> value) {
    // TODO(damonkohler): Verify the type of all Messages in the provided list?
    context.getField(name).setValue(value);
  }

  @Override
  public void setString(String name, String value) {
    setFieldValue(name, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setStringList(String name, List<String> value) {
    setFieldValue(name, PrimitiveFieldType.STRING, value);
  }

  @Override
  public void setTime(String name, Time value) {
    setFieldValue(name, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setTimeList(String name, List<Time> value) {
    setFieldValue(name, PrimitiveFieldType.TIME, value);
  }

  @Override
  public void setUint16(String name, int value) {
    setFieldValue(name, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint16List(String name, List<Integer> value) {
    setFieldValue(name, PrimitiveFieldType.UINT16, value);
  }

  @Override
  public void setUint32(String name, long value) {
    setFieldValue(name, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint32List(String name, List<Long> value) {
    setFieldValue(name, PrimitiveFieldType.UINT32, value);
  }

  @Override
  public void setUint64(String name, long value) {
    setFieldValue(name, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint64List(String name, List<Long> value) {
    setFieldValue(name, PrimitiveFieldType.UINT64, value);
  }

  @Override
  public void setUint8(String name, short value) {
    setFieldValue(name, PrimitiveFieldType.UINT8, value);
  }

  @Override
  public void setUint8List(String name, List<Short> value) {
    setFieldValue(name, PrimitiveFieldType.UINT8, value);
  }

  @Override
  public byte getByte(String name) {
    return this.<Byte>getFieldValue(name, PrimitiveFieldType.BYTE);
  }

  @Override
  public char getChar(String name) {
    return this.<Character>getFieldValue(name, PrimitiveFieldType.CHAR);
  }

  @Override
  public void setByte(String name, byte value) {
    setFieldValue(name, PrimitiveFieldType.BYTE, value);
  }

  @Override
  public void setChar(String name, char value) {
    setFieldValue(name, PrimitiveFieldType.CHAR, value);
  }

  @Override
  public void setByteList(String name, List<Byte> value) {
    setFieldValue(name, PrimitiveFieldType.BYTE, value);
  }

  @Override
  public void setCharList(String name, List<Char> value) {
    setFieldValue(name, PrimitiveFieldType.CHAR, value);
  }

  @Override
  public List<Byte> getByteList(String name) {
    return getFieldValue(name, PrimitiveFieldType.BYTE);
  }

  @Override
  public List<Char> getCharList(String name) {
    return getFieldValue(name, PrimitiveFieldType.CHAR);
  }

  @Override
  public Object getInstance() {
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Message)) return false;
    if (!(obj instanceof GetInstance)) return false;
    obj = ((GetInstance) obj).getInstance();
    if (getClass() != obj.getClass()) return false;
    MessageImpl other = (MessageImpl) obj;
    if (context == null) {
      if (other.context != null) return false;
    } else if (!context.equals(other.context)) return false;
    return true;
  }

  @Override
  public int getSerializedSize() {
    int size = 0;
    for (Field<?> field : getFields()) {
      size += field.getSerializedSize();
    }
    return size;
  }

  @Override
  public ByteBuffer serialize() {
    int length = getSerializedSize();
    ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
    for (Field<?> field : getFields()) {
      if (field.isConstant()) {
        continue;
      }
      field.serialize(buffer);
    }
    buffer.flip();
    return buffer;
  }

  @Override
  public void deserialize() {
    throw new UnsupportedOperationException();
  }

}
