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
public class MessageImpl implements RawMessage, GetInstance {

  private final MessageContext context;

  public MessageImpl(MessageContext context) {
    this.context = context;
  }

  private Object getFieldValue(FieldType type, String name) {
    if (context.hasField(type, name)) {
      return context.getField(name).getValue();
    }
    throw new RosRuntimeException(String.format("Uknown field: %s %s", type, name));
  }

  private void setFieldValue(FieldType type, String name, Object value) {
    if (context.hasField(type, name)) {
      context.getField(name).setValue(value);
    } else {
      throw new RosRuntimeException(String.format("Uknown field: %s %s", type, name));
    }
  }

  @Override
  public RawMessage toRawMessage() {
    return (RawMessage) this;
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
    return (Boolean) getFieldValue(PrimitiveFieldType.BOOL, name);
  }

  @Override
  public boolean[] getBoolArray(String name) {
    return (boolean[]) getFieldValue(PrimitiveFieldType.BOOL, name);
  }

  @Override
  public Duration getDuration(String name) {
    return (Duration) getFieldValue(PrimitiveFieldType.DURATION, name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Duration> getDurationList(String name) {
    return (List<Duration>) getFieldValue(PrimitiveFieldType.DURATION, name);
  }

  @Override
  public float getFloat32(String name) {
    return (Float) getFieldValue(PrimitiveFieldType.FLOAT32, name);
  }

  @Override
  public float[] getFloat32Array(String name) {
    return (float[]) getFieldValue(PrimitiveFieldType.FLOAT32, name);
  }

  @Override
  public double getFloat64(String name) {
    return (Double) getFieldValue(PrimitiveFieldType.FLOAT64, name);
  }

  @Override
  public double[] getFloat64Array(String name) {
    return (double[]) getFieldValue(PrimitiveFieldType.FLOAT64, name);
  }

  @Override
  public short getInt16(String name) {
    return (Short) getFieldValue(PrimitiveFieldType.INT16, name);
  }

  @Override
  public short[] getInt16Array(String name) {
    return (short[]) getFieldValue(PrimitiveFieldType.INT16, name);
  }

  @Override
  public int getInt32(String name) {
    return (Integer) getFieldValue(PrimitiveFieldType.INT32, name);
  }

  @Override
  public int[] getInt32Array(String name) {
    return (int[]) getFieldValue(PrimitiveFieldType.INT32, name);
  }

  @Override
  public long getInt64(String name) {
    return (Long) getFieldValue(PrimitiveFieldType.INT64, name);
  }

  @Override
  public long[] getInt64Array(String name) {
    return (long[]) getFieldValue(PrimitiveFieldType.INT64, name);
  }

  @Override
  public byte getInt8(String name) {
    return (Byte) getFieldValue(PrimitiveFieldType.INT8, name);
  }

  @Override
  public byte[] getInt8Array(String name) {
    return (byte[]) getFieldValue(PrimitiveFieldType.INT8, name);
  }

  @Override
  public <T extends RawMessage> T getMessage(String name) {
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
    return (String) getFieldValue(PrimitiveFieldType.STRING, name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getStringList(String name) {
    return (List<String>) getFieldValue(PrimitiveFieldType.STRING, name);
  }

  @Override
  public Time getTime(String name) {
    return (Time) getFieldValue(PrimitiveFieldType.TIME, name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Time> getTimeList(String name) {
    return (List<Time>) getFieldValue(PrimitiveFieldType.TIME, name);
  }

  @Override
  public short getUInt16(String name) {
    return (Short) getFieldValue(PrimitiveFieldType.UINT16, name);
  }

  @Override
  public short[] getUInt16List(String name) {
    return (short[]) getFieldValue(PrimitiveFieldType.UINT16, name);
  }

  @Override
  public int getUInt32(String name) {
    return (Integer) getFieldValue(PrimitiveFieldType.UINT32, name);
  }

  @Override
  public int[] getUInt32List(String name) {
    return (int[]) getFieldValue(PrimitiveFieldType.UINT32, name);
  }

  @Override
  public long getUInt64(String name) {
    return (Long) getFieldValue(PrimitiveFieldType.UINT64, name);
  }

  @Override
  public long[] getUInt64Array(String name) {
    return (long[]) getFieldValue(PrimitiveFieldType.UINT64, name);
  }

  @Override
  public short getUInt8(String name) {
    return (Short) getFieldValue(PrimitiveFieldType.UINT8, name);
  }

  @Override
  public short[] getUInt8Array(String name) {
    return (short[]) getFieldValue(PrimitiveFieldType.UINT8, name);
  }

  @Override
  public void setBool(String name, boolean value) {
    setFieldValue(PrimitiveFieldType.BOOL, name, value);
  }

  @Override
  public void setBoolArray(String name, boolean[] value) {
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
  public void setFloat32Array(String name, float[] value) {
    setFieldValue(PrimitiveFieldType.FLOAT32, name, value);
  }

  @Override
  public void setFloat64(String name, double value) {
    setFieldValue(PrimitiveFieldType.FLOAT64, name, value);
  }

  @Override
  public void setFloat64Array(String name, double[] value) {
    setFieldValue(PrimitiveFieldType.FLOAT64, name, value);
  }

  @Override
  public void setInt16(String name, short value) {
    setFieldValue(PrimitiveFieldType.INT16, name, value);
  }

  @Override
  public void setInt16Array(String name, short[] value) {
    setFieldValue(PrimitiveFieldType.INT16, name, value);
  }

  @Override
  public void setInt32(String name, int value) {
    setFieldValue(PrimitiveFieldType.INT32, name, value);
  }

  @Override
  public void setInt32Array(String name, int[] value) {
    setFieldValue(PrimitiveFieldType.INT32, name, value);
  }

  @Override
  public void setInt64(String name, long value) {
    setFieldValue(PrimitiveFieldType.INT64, name, value);
  }

  @Override
  public void setInt64Array(String name, long[] value) {
    setFieldValue(PrimitiveFieldType.INT64, name, value);
  }

  @Override
  public void setInt8(String name, byte value) {
    setFieldValue(PrimitiveFieldType.INT8, name, value);
  }

  @Override
  public void setInt8Array(String name, byte[] value) {
    setFieldValue(PrimitiveFieldType.INT8, name, value);
  }

  @Override
  public void setMessage(String name, RawMessage value) {
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
  public void setUInt16(String name, short value) {
    setFieldValue(PrimitiveFieldType.UINT16, name, value);
  }

  @Override
  public void setUInt16Array(String name, short[] value) {
    setFieldValue(PrimitiveFieldType.UINT16, name, value);
  }

  @Override
  public void setUInt32(String name, int value) {
    setFieldValue(PrimitiveFieldType.UINT32, name, value);
  }

  @Override
  public void setUInt32Array(String name, int[] value) {
    setFieldValue(PrimitiveFieldType.UINT32, name, value);
  }

  @Override
  public void setUInt64(String name, long value) {
    setFieldValue(PrimitiveFieldType.UINT64, name, value);
  }

  @Override
  public void setUInt64Array(String name, long[] value) {
    setFieldValue(PrimitiveFieldType.UINT64, name, value);
  }

  @Override
  public void setUInt8(String name, byte value) {
    setFieldValue(PrimitiveFieldType.UINT8, name, value);
  }

  @Override
  public void setUInt8Array(String name, byte[] value) {
    setFieldValue(PrimitiveFieldType.UINT8, name, value);
  }

  @SuppressWarnings("deprecation")
  @Override
  public byte getByte(String name) {
    return (Byte) getFieldValue(PrimitiveFieldType.BYTE, name);
  }

  @SuppressWarnings("deprecation")
  @Override
  public short getChar(String name) {
    return (Short) getFieldValue(PrimitiveFieldType.CHAR, name);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setByte(String name, byte value) {
    setFieldValue(PrimitiveFieldType.BYTE, name, value);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setChar(String name, short value) {
    setFieldValue(PrimitiveFieldType.CHAR, name, value);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setByteArray(String name, byte[] value) {
    setFieldValue(PrimitiveFieldType.BYTE, name, value);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setCharArray(String name, short[] value) {
    setFieldValue(PrimitiveFieldType.CHAR, name, value);
  }

  @SuppressWarnings("deprecation")
  @Override
  public byte[] getByteArray(String name) {
    return (byte[]) getFieldValue(PrimitiveFieldType.BYTE, name);
  }

  @SuppressWarnings("deprecation")
  @Override
  public short[] getCharArray(String name) {
    return (short[]) getFieldValue(PrimitiveFieldType.CHAR, name);
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
