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

import org.ros.exception.RosRuntimeException;
import org.ros.message.Duration;
import org.ros.message.MessageIdentifier;
import org.ros.message.Time;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageImpl implements Message, GetInstance {

  private final MessageContext context;

  public MessageImpl(MessageContext context) {
    this.context = context;
  }

  private <T> T getFieldValue(FieldType type, String name) {
    if (context.hasField(type, name)) {
      return context.getField(name).<T>getValue();
    }
    throw new RosRuntimeException("Unknown field: " + type + " " + name);
  }

  private <T> void setFieldValue(FieldType type, String name, T value) {
    if (context.hasField(type, name)) {
      context.getField(name).setValue(value);
    } else {
      throw new RosRuntimeException("Unable to set field value: " + type + " " + name + " = "
          + value);
    }
  }

  @Override
  public MessageIdentifier getIdentifier() {
    return context.getMessageIdentifer();
  }

  @Override
  public String getType() {
    return context.getType();
  }

  @Override
  public String getPackage() {
    return context.getPackage();
  }

  @Override
  public String getName() {
    return context.getName();
  }

  @Override
  public String getDefinition() {
    return context.getDefinition();
  }

  @Override
  public List<Field> getFields() {
    return context.getFields();
  }

  @Override
  public boolean getBool(String name) {
    return this.<Boolean>getFieldValue(PrimitiveFieldType.BOOL, name);
  }

  @Override
  public List<Boolean> getBoolList(String name) {
    return this.<List<Boolean>>getFieldValue(PrimitiveFieldType.BOOL, name);
  }

  @Override
  public Duration getDuration(String name) {
    return this.<Duration>getFieldValue(PrimitiveFieldType.DURATION, name);
  }

  @Override
  public List<Duration> getDurationList(String name) {
    return this.<List<Duration>>getFieldValue(PrimitiveFieldType.DURATION, name);
  }

  @Override
  public float getFloat32(String name) {
    return this.<Float>getFieldValue(PrimitiveFieldType.FLOAT32, name);
  }

  @Override
  public List<Float> getFloat32List(String name) {
    return this.<List<Float>>getFieldValue(PrimitiveFieldType.FLOAT32, name);
  }

  @Override
  public double getFloat64(String name) {
    return this.<Double>getFieldValue(PrimitiveFieldType.FLOAT64, name);
  }

  @Override
  public List<Double> getFloat64List(String name) {
    return this.<List<Double>>getFieldValue(PrimitiveFieldType.FLOAT64, name);
  }

  @Override
  public short getInt16(String name) {
    return this.<Short>getFieldValue(PrimitiveFieldType.INT16, name);
  }

  @Override
  public List<Short> getInt16List(String name) {
    return this.<List<Short>>getFieldValue(PrimitiveFieldType.INT16, name);
  }

  @Override
  public int getInt32(String name) {
    return this.<Integer>getFieldValue(PrimitiveFieldType.INT32, name);
  }

  @Override
  public List<Integer> getInt32List(String name) {
    return this.<List<Integer>>getFieldValue(PrimitiveFieldType.INT32, name);
  }

  @Override
  public long getInt64(String name) {
    return this.<Long>getFieldValue(PrimitiveFieldType.INT64, name);
  }

  @Override
  public List<Long> getInt64List(String name) {
    return this.<List<Long>>getFieldValue(PrimitiveFieldType.INT64, name);
  }

  @Override
  public byte getInt8(String name) {
    return this.<Byte>getFieldValue(PrimitiveFieldType.INT8, name);
  }

  @Override
  public List<Byte> getInt8List(String name) {
    return this.<List<Byte>>getFieldValue(PrimitiveFieldType.INT8, name);
  }

  @Override
  public <T extends Message> T getMessage(String name) {
    if (context.getField(name).getType() instanceof MessageFieldType) {
      return context.getField(name).<T>getValue();
    }
    throw new RosRuntimeException("Failed to access message field: " + name);
  }

  @Override
  public <T extends Message> List<T> getMessageList(String name) {
    if (context.getField(name).getType() instanceof MessageFieldType) {
      return context.getField(name).<List<T>>getValue();
    }
    throw new RosRuntimeException("Failed to access list field: " + name);
  }

  @Override
  public String getString(String name) {
    return getFieldValue(PrimitiveFieldType.STRING, name);
  }

  @Override
  public List<String> getStringList(String name) {
    return getFieldValue(PrimitiveFieldType.STRING, name);
  }

  @Override
  public Time getTime(String name) {
    return getFieldValue(PrimitiveFieldType.TIME, name);
  }

  @Override
  public List<Time> getTimeList(String name) {
    return getFieldValue(PrimitiveFieldType.TIME, name);
  }

  @Override
  public int getUint16(String name) {
    return this.<Integer>getFieldValue(PrimitiveFieldType.UINT16, name);
  }

  @Override
  public List<Integer> getUint16List(String name) {
    return this.<List<Integer>>getFieldValue(PrimitiveFieldType.UINT16, name);
  }

  @Override
  public long getUint32(String name) {
    return this.<Long>getFieldValue(PrimitiveFieldType.UINT32, name);
  }

  @Override
  public List<Long> getUint32List(String name) {
    return this.<List<Long>>getFieldValue(PrimitiveFieldType.UINT32, name);
  }

  @Override
  public long getUint64(String name) {
    return this.<Long>getFieldValue(PrimitiveFieldType.UINT64, name);
  }

  @Override
  public List<Long> getUint64List(String name) {
    return this.<List<Long>>getFieldValue(PrimitiveFieldType.UINT64, name);
  }

  @Override
  public short getUint8(String name) {
    return this.<Short>getFieldValue(PrimitiveFieldType.UINT8, name);
  }

  @Override
  public List<Short> getUint8List(String name) {
    return this.<List<Short>>getFieldValue(PrimitiveFieldType.UINT8, name);
  }

  @Override
  public void setBool(String name, boolean value) {
    setFieldValue(PrimitiveFieldType.BOOL, name, value);
  }

  @Override
  public void setBoolList(String name, List<Boolean> value) {
    setFieldValue(PrimitiveFieldType.BOOL, name, value);
  }

  @Override
  public void setDurationList(String name, List<Duration> value) {
    setFieldValue(PrimitiveFieldType.DURATION, name, value);
  }

  @Override
  public void setDuration(String name, Duration value) {
    setFieldValue(PrimitiveFieldType.DURATION, name, value);
  }

  @Override
  public void setFloat32(String name, float value) {
    setFieldValue(PrimitiveFieldType.FLOAT32, name, value);
  }

  @Override
  public void setFloat32List(String name, List<Float> value) {
    setFieldValue(PrimitiveFieldType.FLOAT32, name, value);
  }

  @Override
  public void setFloat64(String name, double value) {
    setFieldValue(PrimitiveFieldType.FLOAT64, name, value);
  }

  @Override
  public void setFloat64List(String name, List<Double> value) {
    setFieldValue(PrimitiveFieldType.FLOAT64, name, value);
  }

  @Override
  public void setInt16(String name, short value) {
    setFieldValue(PrimitiveFieldType.INT16, name, value);
  }

  @Override
  public void setInt16List(String name, List<Short> value) {
    setFieldValue(PrimitiveFieldType.INT16, name, value);
  }

  @Override
  public void setInt32(String name, int value) {
    setFieldValue(PrimitiveFieldType.INT32, name, value);
  }

  @Override
  public void setInt32List(String name, List<Integer> value) {
    setFieldValue(PrimitiveFieldType.INT32, name, value);
  }

  @Override
  public void setInt64(String name, long value) {
    setFieldValue(PrimitiveFieldType.INT64, name, value);
  }

  @Override
  public void setInt64List(String name, List<Long> value) {
    setFieldValue(PrimitiveFieldType.INT64, name, value);
  }

  @Override
  public void setInt8(String name, byte value) {
    setFieldValue(PrimitiveFieldType.INT8, name, value);
  }

  @Override
  public void setInt8List(String name, List<Byte> value) {
    setFieldValue(PrimitiveFieldType.INT8, name, value);
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
    setFieldValue(PrimitiveFieldType.STRING, name, value);
  }

  @Override
  public void setStringList(String name, List<String> value) {
    setFieldValue(PrimitiveFieldType.STRING, name, value);
  }

  @Override
  public void setTime(String name, Time value) {
    setFieldValue(PrimitiveFieldType.TIME, name, value);
  }

  @Override
  public void setTimeList(String name, List<Time> value) {
    setFieldValue(PrimitiveFieldType.TIME, name, value);
  }

  @Override
  public void setUint16(String name, int value) {
    setFieldValue(PrimitiveFieldType.UINT16, name, value);
  }

  @Override
  public void setUint16List(String name, List<Integer> value) {
    setFieldValue(PrimitiveFieldType.UINT16, name, value);
  }

  @Override
  public void setUint32(String name, long value) {
    setFieldValue(PrimitiveFieldType.UINT32, name, value);
  }

  @Override
  public void setUint32List(String name, List<Long> value) {
    setFieldValue(PrimitiveFieldType.UINT32, name, value);
  }

  @Override
  public void setUint64(String name, long value) {
    setFieldValue(PrimitiveFieldType.UINT64, name, value);
  }

  @Override
  public void setUint64List(String name, List<Long> value) {
    setFieldValue(PrimitiveFieldType.UINT64, name, value);
  }

  @Override
  public void setUint8(String name, short value) {
    setFieldValue(PrimitiveFieldType.UINT8, name, value);
  }

  @Override
  public void setUint8List(String name, List<Short> value) {
    setFieldValue(PrimitiveFieldType.UINT8, name, value);
  }

  @Override
  public byte getByte(String name) {
    return this.<Byte>getFieldValue(PrimitiveFieldType.BYTE, name);
  }

  @Override
  public char getChar(String name) {
    return this.<Character>getFieldValue(PrimitiveFieldType.CHAR, name);
  }

  @Override
  public void setByte(String name, byte value) {
    setFieldValue(PrimitiveFieldType.BYTE, name, value);
  }

  @Override
  public void setChar(String name, char value) {
    setFieldValue(PrimitiveFieldType.CHAR, name, value);
  }

  @Override
  public void setByteList(String name, List<Byte> value) {
    setFieldValue(PrimitiveFieldType.BYTE, name, value);
  }

  @Override
  public void setCharList(String name, List<Character> value) {
    setFieldValue(PrimitiveFieldType.CHAR, name, value);
  }

  @Override
  public List<Byte> getByteList(String name) {
    return getFieldValue(PrimitiveFieldType.BYTE, name);
  }

  @Override
  public List<Character> getCharList(String name) {
    return getFieldValue(PrimitiveFieldType.CHAR, name);
  }

  @Override
  public int getSerializedSize() {
    int size = 0;
    for (Field field : getFields()) {
      size += field.getSerializedSize();
    }
    return size;
  }

  @Override
  public ByteBuffer serialize() {
    int length = getSerializedSize();
    ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
    for (Field field : getFields()) {
      if (!field.isConstant()) {
        field.serialize(buffer);
      }
    }
    buffer.flip();
    return buffer;
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Message))
      return false;
    if (!(obj instanceof GetInstance))
      return false;
    obj = ((GetInstance) obj).getInstance();
    if (getClass() != obj.getClass())
      return false;
    MessageImpl other = (MessageImpl) obj;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    return true;
  }
}
