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
  },
  BYTE {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return Byte.valueOf((byte) 0);
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
      return (byte) (buffer.get() & 0xff);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Byte parseFromString(String value) {
      return Byte.parseByte(value);
    }
  },
  CHAR {
    @SuppressWarnings("unchecked")
    @Override
    public Character getDefaultValue() {
      return Character.valueOf((char) 0);
    }

    @Override
    public int getSerializedSize() {
      return 1;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Character);
      buffer.put((Byte) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Character deserialize(ByteBuffer buffer) {
      return Character.valueOf((char) (buffer.get() & 0xff));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Character parseFromString(String value) {
      Preconditions.checkArgument(value.length() == 1);
      return Character.valueOf(value.charAt(0));
    }
  },
  INT8 {
    @SuppressWarnings("unchecked")
    @Override
    public Byte getDefaultValue() {
      return Byte.valueOf((byte) 0);
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
  },
  UINT8 {
    @SuppressWarnings("unchecked")
    @Override
    public Short getDefaultValue() {
      return Short.valueOf((short) 0);
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
    public Short deserialize(ByteBuffer buffer) {
      return (short) (buffer.get() & 0xff);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Short parseFromString(String value) {
      return Short.parseShort(value);
    }
  },
  INT16 {
    @SuppressWarnings("unchecked")
    @Override
    public Short getDefaultValue() {
      return Short.valueOf((short) 0);
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
  },
  UINT16 {
    @SuppressWarnings("unchecked")
    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(0);
    }

    @Override
    public int getSerializedSize() {
      return 2;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Integer);
      buffer.putShort((Short) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer deserialize(ByteBuffer buffer) {
      return buffer.getShort() & 0xffff;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer parseFromString(String value) {
      return Integer.parseInt(value);
    }
  },
  INT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(0);
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
  },
  UINT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Long getDefaultValue() {
      return Long.valueOf(0);
    }

    @Override
    public int getSerializedSize() {
      return 4;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Long);
      buffer.putInt(((Long) value).intValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long deserialize(ByteBuffer buffer) {
      return (long) buffer.getInt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long parseFromString(String value) {
      return Long.parseLong(value);
    }
  },
  INT64 {
    @SuppressWarnings("unchecked")
    @Override
    public Long getDefaultValue() {
      return Long.valueOf(0);
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
  },
  UINT64 {
    @SuppressWarnings("unchecked")
    @Override
    public Long getDefaultValue() {
      return Long.valueOf(0);
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
  },
  FLOAT32 {
    @SuppressWarnings("unchecked")
    @Override
    public Float getDefaultValue() {
      return Float.valueOf(0);
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
  },
  STRING {
    @SuppressWarnings("unchecked")
    @Override
    public String getDefaultValue() {
      return "";
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
  },
  TIME {
    @SuppressWarnings("unchecked")
    @Override
    public Time getDefaultValue() {
      return new Time();
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
  },
  DURATION {
    @SuppressWarnings("unchecked")
    @Override
    public Duration getDefaultValue() {
      return new Duration();
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
  public String getName() {
    return toString().toLowerCase();
  }

  @Override
  public String getMd5String() {
    return getName();
  }
}
