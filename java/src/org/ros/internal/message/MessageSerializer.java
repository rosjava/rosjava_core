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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializer {

  private MessageSerializer() {
    // Utility class
  }

  public static int getSerializedLength(Message message) {
    int size = 0;
    for (Field field : message.getFields()) {
      String fieldName = field.getName();
      if (field.isList()) {
        size += 4; // Reserve 4 bytes for the array length.
        if (field.getType() instanceof MessageFieldType) {
          List<Message> nestedMessages = message.getMessageList(fieldName);
          for (Message nestedMessage : nestedMessages) {
            size += getSerializedLength(nestedMessage);
          }
        } else {
          size += field.getSerializedSize();
        }
      } else {
        if (field.getType() instanceof MessageFieldType) {
          size += getSerializedLength(message.getMessage(fieldName));
        } else if (field.getType() == PrimitiveFieldType.STRING) {
          // Add in an extra 4 bytes for the length of the string. Also, we only
          // use ASCII strings, so we calculate 1 byte per character.
          size += message.getString(fieldName).length() + 4;
        } else {
          size += ((PrimitiveFieldType) field.getType()).getSerializedSize();
        }
      }
    }
    return size;
  }

  // TODO(damonkohler): Add sanity checks.
  public static ByteBuffer serialize(Message message) {
    int length = getSerializedLength(message);
    ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
    for (Field field : message.getFields()) {
      if (field.isConstant()) {
        continue;
      }
      Preconditions.checkState(!field.isList());
      if (field.getType() instanceof PrimitiveFieldType) {
        writePrimitiveFieldTypeValue(message, buffer, field);
      } else {
        buffer.put(serialize(message.getMessage(field.getName())));
      }
    }
    buffer.flip();
    return buffer;
  }

  private static void writePrimitiveFieldTypeValue(Message message, ByteBuffer buffer, Field field) {
    String name = field.getName();
    switch ((PrimitiveFieldType) field.getType()) {
      case BOOL:
        writeBool(message.getBool(name), buffer);
        break;
      case CHAR:
        writeChar(message.getChar(name), buffer);
        break;
      case BYTE:
        writeByte(message.getByte(name), buffer);
        break;
      case INT8:
        writeInt8(message.getInt8(name), buffer);
        break;
      case UINT8:
        writeUint8(message.getUint8(name), buffer);
        break;
      case INT16:
        writeInt16(message.getInt16(name), buffer);
        break;
      case UINT16:
        writeUint16(message.getUint16(name), buffer);
        break;
      case INT32:
        writeInt32(message.getInt32(name), buffer);
        break;
      case UINT32:
        writeUint32(message.getUint32(name), buffer);
        break;
      case INT64:
        writeInt64(message.getInt64(name), buffer);
        break;
      case UINT64:
        writeUint64(message.getUint64(name), buffer);
        break;
      case FLOAT32:
        writeFloat32(message.getFloat32(name), buffer);
        break;
      case FLOAT64:
        writeFloat64(message.getFloat64(name), buffer);
        break;
      case STRING:
        writeString(message.getString(name), buffer);
        break;
      case TIME:
        writeTime(message.getTime(name), buffer);
        break;
      case DURATION:
        writeDuration(message.getDuration(name), buffer);
        break;
      default:
        throw new RuntimeException();
    }
  }

  private static void writeBool(boolean value, ByteBuffer buffer) {
    buffer.put((byte) (value ? 1 : 0));
  }

  private static void writeChar(char value, ByteBuffer buffer) {
    buffer.put((byte) value);
  }

  private static void writeByte(byte value, ByteBuffer buffer) {
    buffer.put(value);
  }

  private static void writeUint8(short value, ByteBuffer buffer) {
    buffer.put((byte) value);
  }

  private static void writeInt8(byte value, ByteBuffer buffer) {
    buffer.put(value);
  }

  private static void writeUint16(int value, ByteBuffer buffer) {
    buffer.putShort((short) value);
  }

  private static void writeInt16(short value, ByteBuffer buffer) {
    buffer.putShort(value);
  }

  private static void writeUint32(long value, ByteBuffer buffer) {
    buffer.putInt((int) value);
  }

  private static void writeInt32(int value, ByteBuffer buffer) {
    buffer.putInt(value);
  }

  private static void writeUint64(long value, ByteBuffer buffer) {
    buffer.putLong(value);
  }

  private static void writeInt64(long value, ByteBuffer buffer) {
    buffer.putLong(value);
  }

  private static void writeFloat32(float value, ByteBuffer buffer) {
    buffer.putFloat(value);
  }

  private static void writeFloat64(double value, ByteBuffer buffer) {
    buffer.putDouble(value);
  }

  private static void writeString(String value, ByteBuffer buffer) {
    byte[] bytes = value.getBytes();
    buffer.putInt(bytes.length);
    buffer.put(bytes);
  }

  private static void writeTime(Time value, ByteBuffer buffer) {
    buffer.putInt(value.secs);
    buffer.putInt(value.nsecs);
  }

  private static void writeDuration(Duration value, ByteBuffer buffer) {
    buffer.putInt(value.secs);
    buffer.putInt(value.nsecs);
  }

}
