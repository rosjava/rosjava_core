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

import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.message.std_msgs.Char;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
enum PrimitiveFieldType implements FieldType {

  BOOL {
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
  },
  BYTE {
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
  },
  CHAR {
    @Override
    public int getSerializedSize() {
      return 1;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Char);
      buffer.put((Byte) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Character deserialize(ByteBuffer buffer) {
      return Character.valueOf((char) (buffer.get() & 0xff));
    }
  },
  INT8 {
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
  },
  UINT8 {
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
  },
  INT16 {
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
  },
  UINT16 {
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
  },
  INT32 {
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
  },
  UINT32 {
    @Override
    public int getSerializedSize() {
      return 4;
    }

    @Override
    public <T> void serialize(T value, ByteBuffer buffer) {
      Preconditions.checkArgument(value instanceof Long);
      buffer.putInt((Integer) value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer deserialize(ByteBuffer buffer) {
      return buffer.getInt() & 0xffffffff;
    }
  },
  INT64 {
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
  },
  UINT64 {
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
  },
  FLOAT32 {
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
  },
  FLOAT64 {
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
  },
  STRING {
    @Override
    public int getSerializedSize() {
      throw new RuntimeException();
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
  },
  TIME {
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
  },
  DURATION {
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
  };

  @Override
  public String getName() {
    return toString().toLowerCase();
  }

}
