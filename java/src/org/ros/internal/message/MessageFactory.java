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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Creates {@link MessageImpl} instances.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory {

  private final MessageLoader messageLoader;
  private final MessageClassRegistry messageClassRegistry;

  public MessageFactory(MessageLoader loader, MessageClassRegistry messageClassRegistry) {
    this.messageLoader = loader;
    this.messageClassRegistry = messageClassRegistry;
  }

  private void createFieldFromString(String field, MessageContext context) {
    String[] typeAndName = field.split("\\s+", 2);
    String type = typeAndName[0];
    String name = typeAndName[1];
    String value = null;
    if (name.contains("=")) {
      String[] nameAndValue = name.split("=", 2);
      name = nameAndValue[0].trim();
      value = nameAndValue[1].trim();
    }
    boolean array = false;
    if (type.endsWith("]")) {
      type = type.substring(0, type.lastIndexOf('['));
      array = true;
    }
    FieldType fieldType = getFieldType(context.getName(), type);
    if (value != null) {
      if (array) {
        throw new RuntimeException();
      } else {
        Preconditions.checkState(fieldType instanceof PrimitiveFieldType);
        context.addConstantField(name, fieldType, fieldType.parseFromString(value));
      }
    } else if (array) {
      context.addValueListField(name, fieldType);
    } else {
      context.addValueField(name, fieldType);
    }
    if (fieldType.getName().equals("Header")) {
      Preconditions.checkState(name.equals("header"));
    }
    if (fieldType instanceof MessageFieldType) {
      Preconditions.checkState(messageLoader.hasMessageDefinition(context.getName()));
    }
  }

  private FieldType getFieldType(String messageName, String type) {
    try {
      return PrimitiveFieldType.valueOf(type.toUpperCase());
    } catch (Exception e) {
    }
    if (!type.equals("Header") && !type.contains("/")) {
      type = messageName.substring(0, messageName.lastIndexOf('/') + 1) + type;
    }
    return new MessageFieldType(type, this);
  }

  private MessageContext createMessageContext(String messageName) {
    MessageContext context = new MessageContext(messageName);
    String messageDefinition = getMessageDefinition(messageName);
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line;
    try {
      line = reader.readLine();
      while (line != null) {
        line = line.trim();
        if (line.length() > 0 && !line.startsWith("#")) {
          createFieldFromString(line, context);
        }
        line = reader.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return context;
  }

  private String getMessageDefinition(String messageName) {
    String messageDefinition = messageLoader.getMessageDefinition(messageName);
    if (messageLoader.getMessageDefinition(messageName) == null) {
      throw new RuntimeException("Unknown message type: " + messageName);
    }
    return messageDefinition;
  }

  public <MessageType extends Message> MessageType createMessage(String messageName) {
    Class<MessageType> messageClass = messageClassRegistry.get(messageName);
    MessageContext context = createMessageContext(messageName);
    return MessageProxyFactory.createMessageProxy(messageClass, new MessageImpl(context));
  }

  public <MessageType extends Message> MessageType deserializeMessage(String messageName,
      ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    MessageType message = createMessage(messageName);
    for (Field field : message.getFields()) {
      if (!field.isConstant()) {
        field.deserialize(buffer);
      }
    }
    return message;
  }

}
