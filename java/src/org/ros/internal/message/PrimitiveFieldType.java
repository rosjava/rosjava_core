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
  };

  @Override
  public String getName() {
    return toString().toLowerCase();
  }

  @Override
  public abstract <T> void serialize(T value, ByteBuffer buffer);

  @Override
  public abstract int getSerializedSize();

}
