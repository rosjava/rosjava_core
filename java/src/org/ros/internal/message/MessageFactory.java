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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

/**
 * Generates a {@link MessageImpl} instance from a specification file at
 * runtime.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory {

  private static final ImmutableMap<String, FieldType> FIELD_TYPE_NAMES;

  static {
    FIELD_TYPE_NAMES =
        ImmutableMap.<String, FieldType>builder().put("bool", FieldType.BOOL)
            .put("bool[]", FieldType.BOOL_ARRAY).put("byte", FieldType.INT8)
            .put("byte[]", FieldType.INT8_ARRAY).put("int8", FieldType.INT8)
            .put("int8[]", FieldType.INT8_ARRAY).put("char", FieldType.UINT8)
            .put("char[]", FieldType.UINT8_ARRAY).put("uint8", FieldType.UINT8)
            .put("uint8[]", FieldType.UINT8_ARRAY).put("int16", FieldType.INT16)
            .put("int16[]", FieldType.INT16_ARRAY).put("uint16", FieldType.UINT16)
            .put("uint16[]", FieldType.UINT16_ARRAY).put("int32", FieldType.INT32)
            .put("int32[]", FieldType.INT32_ARRAY).put("uint32", FieldType.UINT32)
            .put("uint32[]", FieldType.UINT32_ARRAY).put("int64", FieldType.INT64)
            .put("int64[]", FieldType.INT64_ARRAY).put("uint64", FieldType.UINT64)
            .put("uint64[]", FieldType.UINT64_ARRAY).put("float32", FieldType.FLOAT32)
            .put("float32[]", FieldType.FLOAT32_ARRAY).put("float64", FieldType.FLOAT64)
            .put("float64[]", FieldType.FLOAT64_ARRAY).put("string", FieldType.STRING)
            .put("string[]", FieldType.STRING_ARRAY).put("time", FieldType.TIME)
            .put("time[]", FieldType.TIME_ARRAY).put("duration", FieldType.DURATION)
            .put("duration[]", FieldType.DURATION_ARRAY).build();
  }

  private static final class MessageContext<MessageType extends Message> {

    final Class<MessageType> messageClass;
    final Map<String, FieldType> valueFieldTypes;
    final Map<String, FieldType> constantFieldTypes;
    final Map<String, Object> constantFieldValues;

    public MessageContext(Class<MessageType> messageClass,
        Map<String, FieldType> constantFieldTypes, Map<String, Object> constantFieldValues,
        Map<String, FieldType> valueFieldTypes) {
      this.messageClass = messageClass;
      this.constantFieldTypes = Collections.unmodifiableMap(constantFieldTypes);
      this.constantFieldValues = Collections.unmodifiableMap(constantFieldValues);
      this.valueFieldTypes = Collections.unmodifiableMap(valueFieldTypes);
    }

  }

  private final Map<String, MessageContext<? extends Message>> messageContext;
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
    messageContext = Maps.newConcurrentMap();
  }

  private <MessageType extends Message> void addMessageContext(String messageName,
      Class<MessageType> messageClass) throws IOException {
    Map<String, FieldType> valueFieldTypes = Maps.newHashMap();
    Map<String, FieldType> constantFieldTypes = Maps.newHashMap();
    Map<String, Object> constantFieldValues = Maps.newHashMap();
    String messageDefinition = getMessageDefinition(messageName);
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        String[] typeAndName = line.split("\\s+", 2);
        String type = typeAndName[0];
        String name = typeAndName[1];
        if (name.contains("=")) {
          addConstantToContext(type, name, constantFieldTypes, constantFieldValues);
        } else {
          if (type.equals("Header")) {
            Preconditions.checkState(name.equals("header"));
            valueFieldTypes.put(name, FieldType.MESSAGE);
          } else if (type.endsWith("]")) {
            addArrayFieldToContext(name, type, messageName, valueFieldTypes);
          } else if (FIELD_TYPE_NAMES.containsKey(type)) {
            valueFieldTypes.put(name, FIELD_TYPE_NAMES.get(type));
          } else {
            if (!type.contains("/")) {
              type = messageName.substring(0, messageName.lastIndexOf('/') + 1) + type;
            }
            if (messageLoader.hasMessageDefinition(messageName)) {
              valueFieldTypes.put(name, FieldType.MESSAGE);
            } else {
              throw new RuntimeException();
            }
          }
        }
      }
      line = reader.readLine();
    }
    MessageContext<MessageType> contexts =
        new MessageContext<MessageType>(messageClass, constantFieldTypes, constantFieldValues,
            valueFieldTypes);
    messageContext.put(messageName, contexts);
  }

  private void addArrayFieldToContext(String name, String type, String messageName,
      Map<String, FieldType> valueFieldTypes) {
    // TODO(damonkohler): Treat fixed sized arrays differently?
    type = type.substring(0, type.lastIndexOf('['));
    if (FIELD_TYPE_NAMES.containsKey(type + "[]")) {
      valueFieldTypes.put(name, FIELD_TYPE_NAMES.get(type + "[]"));
    } else {
      if (!type.contains("/")) {
        type = messageName.substring(0, messageName.lastIndexOf('/') + 1) + type;
      }
      if (messageLoader.hasMessageDefinition(messageName)) {
        valueFieldTypes.put(name, FieldType.MESSAGE_ARRAY);
      } else {
        throw new RuntimeException();
      }
    }
  }

  private void addConstantToContext(String type, String name,
      Map<String, FieldType> constantFieldTypes, Map<String, Object> constantFieldValues) {
    String[] nameAndValue = name.split("=", 2);
    name = nameAndValue[0];
    String value = nameAndValue[1];
    constantFieldTypes.put(name, FIELD_TYPE_NAMES.get(type));
    constantFieldValues.put(name, parseConstant(value, FIELD_TYPE_NAMES.get(type)));
  }

  private String getMessageDefinition(String messageName) {
    String messageDefinition = messageLoader.getMessageDefinition(messageName);
    if (messageLoader.getMessageDefinition(messageName) == null) {
      throw new RuntimeException("Unknown message type: " + messageName);
    }
    return messageDefinition;
  }

  private Object parseConstant(String value, FieldType type) {
    switch (type) {
      case INT8:
      case UINT8:
      case INT16:
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

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> MessageType createMessage(String messageName,
      Class<MessageType> messageClass) {
    if (!messageContext.containsKey(messageName)) {
      try {
        addMessageContext(messageName, messageClass);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    MessageContext<MessageType> constants =
        (MessageContext<MessageType>) messageContext.get(messageName);
    Preconditions.checkNotNull(constants);
    Preconditions.checkState(messageClass == constants.messageClass);
    return MessageProxyFactory.getProxy(messageClass, new MessageImpl(constants.constantFieldTypes,
        constants.constantFieldValues, constants.valueFieldTypes));
  }

}
