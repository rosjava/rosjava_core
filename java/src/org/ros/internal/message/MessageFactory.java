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
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Generates a {@link MessageImpl} instance from a specification file at
 * runtime.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory {

  private final Map<String, MessageContext> messageContexts;
  private final Map<String, Class<? extends Message>> messageClasses;
  private final MessageLoader messageLoader;

  private static final class MessageProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T extends Message> T getProxy(Class<T> interfaceClass,
        final Message implementation) {
      return (T) Proxy.newProxyInstance(implementation.getClass().getClassLoader(),
          new Class[] {interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              return method.invoke(implementation, args);
            }
          });
    }
  }

  public MessageFactory(MessageLoader loader) {
    this.messageLoader = loader;
    messageContexts = Maps.newConcurrentMap();
    messageClasses = Maps.newConcurrentMap();
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
    if (name.endsWith("]")) {
      // TODO(damonkohler): Treat fixed sized arrays differently?
      type = type.substring(0, type.lastIndexOf('['));
      array = true;
    }
    FieldType fieldType = getFieldType(context.getName(), type);
    if (value != null) {
      Preconditions.checkState(fieldType instanceof PrimitiveFieldType);
      Object parsedValue = parseConstantValueFromString(value, (PrimitiveFieldType) fieldType);
      if (array) {
        context.addConstantArrayField(name, fieldType, parsedValue);
      } else {
        context.addConstantField(name, fieldType, parsedValue);
      }
    } else if (array) {
      context.addValueArrayField(name, fieldType);
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
    return new MessageFieldType(type);
  }

  private void addMessageContext(String messageName) throws IOException {
    MessageContext context = new MessageContext(messageName);
    String messageDefinition = getMessageDefinition(messageName);
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        createFieldFromString(line, context);
      }
      line = reader.readLine();
    }
    messageContexts.put(messageName, context);
  }

  private String getMessageDefinition(String messageName) {
    String messageDefinition = messageLoader.getMessageDefinition(messageName);
    if (messageLoader.getMessageDefinition(messageName) == null) {
      throw new RuntimeException("Unknown message type: " + messageName);
    }
    return messageDefinition;
  }

  private Object parseConstantValueFromString(String value, PrimitiveFieldType type) {
    switch (type) {
      case INT8:
        return Byte.parseByte(value);
      case UINT8:
      case INT16:
        return Short.parseShort(value);
      case UINT16:
      case INT32:
        return Integer.parseInt(value);
      case UINT32:
      case INT64:
      case UINT64:
        return Long.parseLong(value);
      case FLOAT32:
        return Float.parseFloat(value);
      case FLOAT64:
        return Double.parseDouble(value);
      case STRING:
        return value;
      case BOOL:
        return value.equals("1");
      default:
        throw new RuntimeException("Invalid field type for constant: " + type + " " + value);
    }
  }

  public <MessageType extends Message> void setMessageClass(String messageName,
      Class<MessageType> messageClass) {
    messageClasses.put(messageName, messageClass);
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> MessageType createMessage(String messageName) {
    Class<MessageType> messageClass = (Class<MessageType>) messageClasses.get(messageName);
    if (messageClass == null) {
      // If we don't know a specific message class to use with the proxy, fall
      // back to the generic Message interface.
      messageClass = (Class<MessageType>) Message.class;
    }
    if (!messageContexts.containsKey(messageName)) {
      try {
        addMessageContext(messageName);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    MessageContext context = messageContexts.get(messageName);
    Preconditions.checkNotNull(context);
    return MessageProxyFactory.getProxy(messageClass, new MessageImpl(context));
  }

}
