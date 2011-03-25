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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Generates a {@link Message} instance from a specification file at runtime.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RuntimeGeneratedMessageFactory implements FieldType {

  private final ImmutableMap<String, Short> valueFieldTypes;
  private final ImmutableMap<String, Object> constantFieldValues;

  public RuntimeGeneratedMessageFactory(String messageDefinition) throws IOException {
    ImmutableMap<String, Short> fieldTypeNames =
        ImmutableMap.<String, Short>builder().put("bool", BOOL).put("int8", INT8)
            .put("uint8", UINT8).put("int16", INT16).put("uint16", UINT16).put("int32", INT32)
            .put("uint32", UINT32).put("int64", INT64).put("uint64", UINT64)
            .put("float32", FLOAT32).put("float64", FLOAT64).put("string", STRING)
            .put("time", TIME).put("duration", DURATION).build();
    ImmutableMap.Builder<String, Short> valueFieldTypesBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<String, Object> constantFieldValuesBuilder = ImmutableMap.builder();
    BufferedReader reader = new BufferedReader(new StringReader(messageDefinition));
    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        Preconditions.checkState(tokenizer.countTokens() == 2);
        String type = tokenizer.nextToken();
        String name = tokenizer.nextToken();
        if (name.contains("=")) {
          throw new UnsupportedOperationException();
        } else {
          valueFieldTypesBuilder.put(name, fieldTypeNames.get(type));
        }
      }
      line = reader.readLine();
    }
    valueFieldTypes = valueFieldTypesBuilder.build();
    constantFieldValues = constantFieldValuesBuilder.build();
  }

  public Message createMessage() {
    return new Message(valueFieldTypes, constantFieldValues);
  }

}
