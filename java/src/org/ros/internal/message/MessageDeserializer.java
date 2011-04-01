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

import com.google.common.collect.Lists;

import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.message.std_msgs.Char;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDeserializer {

  private MessageDeserializer() {
    // Utility class
  }

  // TODO(damonkohler): Add sanity checks.
  public static <MessageType extends Message> MessageType deserialize(MessageFactory factory,
      String messageName, ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    MessageType message = factory.createMessage(messageName);
    for (Field field : message.getFields()) {
      if (field.isConstant()) {
        continue;
      }
      String name = field.getName();
      if (field.isList()) {
        if (field.getType() instanceof PrimitiveFieldType) {
          setPrimitiveFieldTypeList(buffer, message, field, name);
        } else {
          int length = buffer.getInt();
          for (int i = 0; i < length; i++) {
            message.setMessage(name, deserialize(factory, field.getType().getName(), buffer));
          }
        }
      }
      if (field.getType() instanceof PrimitiveFieldType) {
        setPrimitiveFieldTypeValue(buffer, message, field, name);
      } else {
        message.setMessage(name, deserialize(factory, field.getType().getName(), buffer));
      }
    }
    return message;
  }

  private static <MessageType extends Message> void setPrimitiveFieldTypeValue(ByteBuffer buffer,
      MessageType message, Field field, String name) {
    switch ((PrimitiveFieldType) field.getType()) {
      case BOOL:
        message.setBool(name, readBool(buffer));
        break;
      case CHAR:
        message.setChar(name, readChar(buffer));
        break;
      case BYTE:
        message.setByte(name, readByte(buffer));
        break;
      case INT8:
        message.setInt8(name, readInt8(buffer));
        break;
      case UINT8:
        message.setUint8(name, readUint8(buffer));
        break;
      case INT16:
        message.setInt16(name, readInt16(buffer));
        break;
      case UINT16:
        message.setUint16(name, readUint16(buffer));
        break;
      case INT32:
        message.setInt32(name, readInt32(buffer));
        break;
      case UINT32:
        message.setUint32(name, readUint32(buffer));
        break;
      case INT64:
        message.setInt64(name, readInt64(buffer));
        break;
      case UINT64:
        message.setUint64(name, readUint64(buffer));
        break;
      case FLOAT32:
        message.setFloat32(name, readFloat32(buffer));
        break;
      case FLOAT64:
        message.setFloat64(name, readFloat64(buffer));
        break;
      case STRING:
        message.setString(name, readString(buffer));
        break;
      case TIME:
        message.setTime(name, readTime(buffer));
        break;
      case DURATION:
        message.setDuration(name, readDuration(buffer));
        break;
      default:
        throw new RuntimeException();
    }
  }

  private static <MessageType extends Message> void setPrimitiveFieldTypeList(ByteBuffer buffer,
      MessageType message, Field field, String name) {
    switch ((PrimitiveFieldType) field.getType()) {
      case BOOL:
        message.setBoolList(name,
            MessageDeserializer.<Boolean>readFieldFromBufferToList(buffer, field));
        break;
      case CHAR:
        message.setCharList(name,
            MessageDeserializer.<Char>readFieldFromBufferToList(buffer, field));
        break;
      case BYTE:
        message.setByteList(name,
            MessageDeserializer.<Byte>readFieldFromBufferToList(buffer, field));
        break;
      case INT8:
        message.setInt8List(name,
            MessageDeserializer.<Byte>readFieldFromBufferToList(buffer, field));
        break;
      case UINT8:
        message.setUint8List(name,
            MessageDeserializer.<Short>readFieldFromBufferToList(buffer, field));
        break;
      case INT16:
        message.setInt16List(name,
            MessageDeserializer.<Short>readFieldFromBufferToList(buffer, field));
        break;
      case UINT16:
        message.setUint16List(name,
            MessageDeserializer.<Integer>readFieldFromBufferToList(buffer, field));
        break;
      case INT32:
        message.setInt32List(name,
            MessageDeserializer.<Integer>readFieldFromBufferToList(buffer, field));
        break;
      case UINT32:
        message.setUint32List(name,
            MessageDeserializer.<Long>readFieldFromBufferToList(buffer, field));
        break;
      case INT64:
        message.setInt64List(name,
            MessageDeserializer.<Long>readFieldFromBufferToList(buffer, field));
        break;
      case UINT64:
        message.setUint64List(name,
            MessageDeserializer.<Long>readFieldFromBufferToList(buffer, field));
        break;
      case FLOAT32:
        message.setFloat32List(name,
            MessageDeserializer.<Float>readFieldFromBufferToList(buffer, field));
        break;
      case FLOAT64:
        message.setFloat64List(name,
            MessageDeserializer.<Double>readFieldFromBufferToList(buffer, field));
        break;
      case STRING:
        message.setStringList(name,
            MessageDeserializer.<String>readFieldFromBufferToList(buffer, field));
        break;
      case TIME:
        message.setTimeList(name,
            MessageDeserializer.<Time>readFieldFromBufferToList(buffer, field));
        break;
      case DURATION:
        message.setDurationList(name,
            MessageDeserializer.<Duration>readFieldFromBufferToList(buffer, field));
        break;
      default:
        throw new RuntimeException();
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> List<T> readFieldFromBufferToList(ByteBuffer buffer, Field field) {
    int length = buffer.getInt();
    List<T> values = Lists.newArrayList();
    for (int i = 0; i < length; i++) {
      values.add((T) readFieldFromBuffer(field, buffer));
    }
    return values;
  }

  private static Object readFieldFromBuffer(Field field, ByteBuffer buffer) {
    switch ((PrimitiveFieldType) field.getType()) {
      case BOOL:
        return readBool(buffer);
      case CHAR:
        return readChar(buffer);
      case BYTE:
        return readByte(buffer);
      case INT8:
        return readInt8(buffer);
      case UINT8:
        return readUint8(buffer);
      case INT16:
        return readInt16(buffer);
      case UINT16:
        return readUint16(buffer);
      case INT32:
        return readInt32(buffer);
      case UINT32:
        return readUint32(buffer);
      case INT64:
        return readInt64(buffer);
      case UINT64:
        return readUint64(buffer);
      case FLOAT32:
        return readFloat32(buffer);
      case FLOAT64:
        return readFloat64(buffer);
      case STRING:
        return readString(buffer);
      case TIME:
        return readTime(buffer);
      case DURATION:
        return readDuration(buffer);
      default:
        throw new RuntimeException();
    }
  }

  private static boolean readBool(ByteBuffer buffer) {
    return buffer.get() == 1;
  }

  private static char readChar(ByteBuffer buffer) {
    return (char) (buffer.get() & 0xff);
  }

  private static byte readByte(ByteBuffer buffer) {
    return (byte) (buffer.get() & 0xff);
  }

  private static short readUint8(ByteBuffer buffer) {
    return (short) (buffer.get() & 0xff);
  }

  private static byte readInt8(ByteBuffer buffer) {
    return buffer.get();
  }

  private static int readUint16(ByteBuffer buffer) {
    return buffer.getShort() & 0xffff;
  }

  private static short readInt16(ByteBuffer buffer) {
    return buffer.getShort();
  }

  private static long readUint32(ByteBuffer buffer) {
    return buffer.getInt() & 0xffffffff;
  }

  private static int readInt32(ByteBuffer buffer) {
    return buffer.getInt();
  }

  private static long readUint64(ByteBuffer buffer) {
    return buffer.getLong();
  }

  private static long readInt64(ByteBuffer buffer) {
    return buffer.getLong();
  }

  private static float readFloat32(ByteBuffer buffer) {
    return buffer.getFloat();
  }

  private static double readFloat64(ByteBuffer buffer) {
    return buffer.getDouble();
  }

  private static String readString(ByteBuffer buffer) {
    int length = buffer.getInt();
    ByteBuffer stringBuffer = buffer.slice();
    stringBuffer.limit(length);
    buffer.position(buffer.position() + length);
    return Charset.forName("US-ASCII").decode(stringBuffer).toString();
  }

  private static Time readTime(ByteBuffer buffer) {
    Time time = new Time(buffer.getInt(), buffer.getInt());
    return time;
  }

  private static Duration readDuration(ByteBuffer buffer) {
    Duration duration = new Duration(buffer.getInt(), buffer.getInt());
    return duration;
  }

}
