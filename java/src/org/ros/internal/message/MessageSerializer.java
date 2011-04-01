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
    field.getType().serialize(field.getValue(), buffer);
  }

}
