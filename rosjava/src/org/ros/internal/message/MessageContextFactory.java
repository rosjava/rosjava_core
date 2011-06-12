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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageContextFactory {

  private final MessageFactory messageFactory;

  public MessageContextFactory(MessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }

  public MessageContext create(String messageName, String messageDefinition) {
    MessageContext context = new MessageContext(messageName);
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
    if (fieldType.getName().equals("Header")) {
      Preconditions.checkState(name.equals("header"));
    }
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
  }

  private FieldType getFieldType(String messageName, String type) {
    if (PrimitiveFieldType.existsFor(type)) {
      return PrimitiveFieldType.valueOf(type.toUpperCase());
    }
    if (!type.equals("Header") && !type.contains("/")) {
      type = messageName.substring(0, messageName.lastIndexOf('/') + 1) + type;
    }
    return new MessageFieldType(type, messageFactory);
  }

}
