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

import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Generates a {@link MessageImpl} instance from a specification file at runtime.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory<MessageType extends Message> implements FieldType {

  private static final ImmutableMap<String, Short> FIELD_TYPE_NAMES;

  static {
    FIELD_TYPE_NAMES = ImmutableMap.<String, Short>builder()
        .put("bool", BOOL)
        .put("int8", INT8)
        .put("uint8", UINT8)
        .put("int16", INT16)
        .put("uint16", UINT16)
        .put("int32", INT32)
        .put("uint32", UINT32)
        .put("int64", INT64)
        .put("uint64", UINT64)
        .put("float32", FLOAT32)
        .put("float64", FLOAT64)
        .put("string", STRING)
        .put("time", TIME)
        .put("duration", DURATION)
        .build();
  }

  private final Class<MessageType> messageClass;
  private final ImmutableMap<String, Short> valueFieldTypes;
  private final ImmutableMap<String, Object> constantFieldValues;

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

  public MessageFactory(String messageDefinition, Class<MessageType> messageClass)
      throws IOException {
    this.messageClass = messageClass;
    ImmutableMap.Builder<String, Short> valueFieldTypesBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<String, Object> constantFieldValuesBuilder = ImmutableMap.builder();
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        String[] typeAndName = line.split("\\s+", 2);
        String type = typeAndName[0];
        String name = typeAndName[1];
        if (name.contains("=")) {
          String[] nameAndValue = name.split("=", 2);
          name = nameAndValue[0];
          String value = nameAndValue[1];
          constantFieldValuesBuilder.put(name, parseConstant(value, FIELD_TYPE_NAMES.get(type)));
        } else {
          if (FIELD_TYPE_NAMES.containsKey(type)) {
            valueFieldTypesBuilder.put(name, FIELD_TYPE_NAMES.get(type));
          } else {
            // TODO(damonkohler): Maintain and check against a list of known
            // message types.
            valueFieldTypesBuilder.put(name, MESSAGE);
          }
        }
      }
      line = reader.readLine();
    }
    valueFieldTypes = valueFieldTypesBuilder.build();
    constantFieldValues = constantFieldValuesBuilder.build();
  }

  private Object parseConstant(String value, short type) {
    switch (type) {
      case INT8:
      case UINT8:
      case INT16:
      case UINT16:
      case INT32:
      case UINT32:
        return Integer.parseInt(value);
      case INT64:
      case UINT64:
        return Long.parseLong(value);
      case FLOAT32:
        return Float.parseFloat(value);
      case FLOAT64:
        return Double.parseDouble(value);
      case STRING:
        return value;
      default:
        throw new RuntimeException();
    }
  }

  public MessageType createMessage() {
    return MessageProxyFactory.getProxy(messageClass, new MessageImpl(valueFieldTypes,
        constantFieldValues));
  }
}
