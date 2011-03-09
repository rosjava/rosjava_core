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

import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Generates a {@link Message} instance from a specification file at runtime.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RuntimeGeneratedMessageFactory {

  private final String messageDefinition;

  private class Field {
    String name;
    String type;

    public Field(String name, String type) {
      this.name = name;
      this.type = type;
    }
  }
  
  private class ValueField<T> extends Field {
    T value;
    
    public ValueField(String name, String type) {
      super(name, type);
    }
  }
  
  private class ConstantField<T> extends Field {
    final T value;
    
    public ConstantField(String name, String type, T value) {
      super(name, type);
      this.value = value;
    }
  }

  private final Map<String, ValueField> valueFields;
  private final Map<String, ConstantField> constantFields;

  public RuntimeGeneratedMessageFactory(String messageDefinition) {
    this.messageDefinition = messageDefinition;
    valueFields = Maps.newHashMap();
    constantFields = Maps.newHashMap();
  }

  private void parse() throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.startsWith("#")) {
        continue;
      }
      StringTokenizer tokenizer = new StringTokenizer(line);
      String type = tokenizer.nextToken();
      String name = tokenizer.nextToken();
      if (name.contains("=")) {
        // This is a constant.

      } else {
        // This is a field.

      }
      line = reader.readLine();
    }
  }

  public Message createMessage() {
    return null;
  }

}
