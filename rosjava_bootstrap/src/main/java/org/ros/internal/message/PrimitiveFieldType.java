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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.ros.message.Duration;
import org.ros.message.Time;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public enum PrimitiveFieldType implements FieldType {

  BOOL {
    @SuppressWarnings("unchecked")
    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public BooleanArrayField newVariableList(String name, int size) {
      return BooleanArrayField.newVariable(size, name);
    }

    @Override
    public int getSerializedSize() {
      return 1;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Boolean);
      buffer.put((byte) ((Boolean) value ? 1 : 0));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean deserialize(ByteBuffer buffer) {
      return buffer.get() == 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean parseFromString(String value) {
      return value.equals("1");
    }

    @Override
    public String getJavaTypeName() {
      return "boolean";
    }
  },
  INT8 {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return Byte.valueOf((byte) 0);
    }

    @Override
    public Field newVariableList(String name, int size) {
      return ByteArrayField.newVariable(name, size);
    }

    @Override
    public int getSerializedSize() {
      return 1;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Byte);
      buffer.put((Byte) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte deserialize(ByteBuffer buffer) {
      return buffer.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte parseFromString(String value) {
      return Byte.parseByte(value);
    }

    @Override
    public String getJavaTypeName() {
      return "byte";
    }
  },
  /**
   * @deprecated replaced by {@link PrimitiveFieldType#INT8}
   */
  BYTE {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return INT8.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return INT8.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return INT8.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      INT8.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte deserialize(ByteBuffer buffer) {
      return INT8.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte parseFromString(String value) {
      return INT8.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return INT8.getJavaTypeName();
    }
  },
  UINT8 {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return INT8.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return INT8.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return INT8.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      INT8.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte deserialize(ByteBuffer buffer) {
      return INT8.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte parseFromString(String value) {
      return INT8.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return INT8.getJavaTypeName();
    }
  },
  /**
   * @deprecated replaced by {@link PrimitiveFieldType#UINT8}
   */
  CHAR {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return UINT8.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return UINT8.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return UINT8.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      UINT8.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte deserialize(ByteBuffer buffer) {
      return UINT8.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte parseFromString(String value) {
      return UINT8.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return "byte";
    }
  },
  INT16 {
    @SuppressWarnings("unchecked")
    @Override
    public Short getDefaultValue() {
      return Short.valueOf((short) 0);
    }

    @Override
    public Field newVariableList(String name, int size) {
      return ShortArrayField.newVariable(this, size, name);
    }

    @Override
    public int getSerializedSize() {
      return 2;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Short);
      buffer.putShort((Short) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Short deserialize(ByteBuffer buffer) {
      return buffer.getShort();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Short parseFromString(String value) {
      return Short.parseShort(value);
    }

    @Override
    public String getJavaTypeName() {
      return "short";
    }
  },
  UINT16 {
    @SuppressWarnings("unchecked")
    @Override
    public Short getDefaultValue() {
      return INT16.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return INT16.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return INT16.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      INT16.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Short deserialize(ByteBuffer buffer) {
      return INT16.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer parseFromString(String value) {
      return INT16.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return INT16.getJavaTypeName();
    }
  },
  INT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(0);
    }

    @Override
    public Field newVariableList(String name, int size) {
      return IntegerArrayField.newVariable(this, size, name);
    }

    @Override
    public int getSerializedSize() {
      return 4;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Integer);
      buffer.putInt((Integer) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer deserialize(ByteBuffer buffer) {
      return buffer.getInt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer parseFromString(String value) {
      return Integer.parseInt(value);
    }

    @Override
    public String getJavaTypeName() {
      return "int";
    }
  },
  UINT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Integer getDefaultValue() {
      return INT32.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return INT32.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return INT32.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      INT32.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer deserialize(ByteBuffer buffer) {
      return INT32.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long parseFromString(String value) {
      return INT32.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return INT32.getJavaTypeName();
    }
  },
  INT64 {
    @SuppressWarnings("unchecked")
    @Override
    public Long getDefaultValue() {
      return Long.valueOf(0);
    }

    @Override
    public Field newVariableList(String name, int size) {
      return LongArrayField.newVariable(this, size, name);
    }

    @Override
    public int getSerializedSize() {
      return 8;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Long);
      buffer.putLong((Long) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long deserialize(ByteBuffer buffer) {
      return buffer.getLong();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long parseFromString(String value) {
      return Long.parseLong(value);
    }

    @Override
    public String getJavaTypeName() {
      return "long";
    }
  },
  UINT64 {
    @SuppressWarnings("unchecked")
    @Override
    public Long getDefaultValue() {
      return INT64.getDefaultValue();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return INT64.newVariableList(name, size);
    }

    @Override
    public int getSerializedSize() {
      return INT64.getSerializedSize();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      INT64.serialize(value, buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long deserialize(ByteBuffer buffer) {
      return INT64.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long parseFromString(String value) {
      return INT64.parseFromString(value);
    }

    @Override
    public String getJavaTypeName() {
      return INT64.getJavaTypeName();
    }
  },
  FLOAT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Float getDefaultValue() {
      return Float.valueOf(0);
    }

    @Override
    public Field newVariableList(String name, int size) {
      return FloatArrayField.newVariable(size, name);
    }

    @Override
    public int getSerializedSize() {
      return 4;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Float);
      buffer.putFloat((Float) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Float deserialize(ByteBuffer buffer) {
      return buffer.getFloat();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Float parseFromString(String value) {
      return Float.parseFloat(value);
    }

    @Override
    public String getJavaTypeName() {
      return "float";
    }
  },
  FLOAT64 {
    @SuppressWarnings("unchecked")
    @Override
    public Double getDefaultValue() {
      return Double.valueOf(0);
    }

    @Override
    public int getSerializedSize() {
      return 8;
    }

    @Override
    public Field newVariableList(String name, int size) {
      return DoubleArrayField.newVariable(size, name);
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Double);
      buffer.putDouble((Double) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double deserialize(ByteBuffer buffer) {
      return buffer.getDouble();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double parseFromString(String value) {
      return Double.parseDouble(value);
    }

    @Override
    public String getJavaTypeName() {
      return "double";
    }
  },
  STRING {
    @SuppressWarnings("unchecked")
    @Override
    public String getDefaultValue() {
      return "";
    }

    @Override
    public Field newVariableList(String name, int size) {
      return ListField.newVariable(this, name);
    }

    @Override
    public int getSerializedSize() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof String);
      byte[] bytes = ((String) value).getBytes();
      buffer.putInt(bytes.length);
      buffer.put(bytes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String deserialize(ByteBuffer buffer) {
      int length = buffer.getInt();
      ByteBuffer stringBuffer = buffer.slice();
      stringBuffer.limit(length);
      buffer.position(buffer.position() + length);
      return Charset.forName("US-ASCII").decode(stringBuffer).toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String parseFromString(String value) {
      return value;
    }

    @Override
    public String getJavaTypeName() {
      return "java.lang.String";
    }
  },
  TIME {
    @SuppressWarnings("unchecked")
    @Override
    public Time getDefaultValue() {
      return new Time();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return ListField.newVariable(this, name);
    }

    @Override
    public int getSerializedSize() {
      return 8;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Time);
      buffer.putInt(((Time) value).secs);
      buffer.putInt(((Time) value).nsecs);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Time deserialize(ByteBuffer buffer) {
      return new Time(buffer.getInt(), buffer.getInt());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void parseFromString(String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getJavaTypeName() {
      return Time.class.getName();
    }
  },
  DURATION {
    @SuppressWarnings("unchecked")
    @Override
    public Duration getDefaultValue() {
      return new Duration();
    }

    @Override
    public Field newVariableList(String name, int size) {
      return ListField.newVariable(this, name);
    }

    @Override
    public int getSerializedSize() {
      return 8;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Duration);
      buffer.putInt(((Duration) value).secs);
      buffer.putInt(((Duration) value).nsecs);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Duration deserialize(ByteBuffer buffer) {
      return new Duration(buffer.getInt(), buffer.getInt());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void parseFromString(String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getJavaTypeName() {
      return Duration.class.getName();
    }
  };

  private static final ImmutableSet<String> TYPE_NAMES;

  static {
    ImmutableSet.Builder<String> builder = ImmutableSet.<String>builder();
    for (PrimitiveFieldType type : values()) {
      builder.add(type.getName());
    }
    TYPE_NAMES = builder.build();
  }

  public static boolean existsFor(String name) {
    return TYPE_NAMES.contains(name);
  }

  @Override
  public Field newVariableValue(String name) {
    return ValueField.newVariable(this, name);
  }

  @Override
  public <T> Field newConstantValue(String name, T value) {
    return ValueField.newConstant(this, name, value);
  }

  @Override
  public String getName() {
    return toString().toLowerCase();
  }

  @Override
  public String getMd5String() {
    return getName();
  }
}
