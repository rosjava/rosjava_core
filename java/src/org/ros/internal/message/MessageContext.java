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
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class MessageContext {

  private final String name;
  private final Map<String, Field<?>> fields;
  private final List<Field<?>> orderedFields;

  MessageContext(String name) {
    this.name = name;
    this.fields = Maps.newConcurrentMap();
    this.orderedFields = Lists.newArrayList();
  }

  String getName() {
    return name;
  }

  <T> void addConstantField(String name, FieldType type, T value) {
    Field<T> field = Field.createConstant(name, type, value);
    fields.put(name, field);
    orderedFields.add(field);
  }

  <T> void addConstantArrayField(String name, FieldType type, T value) {
    Field<T> field = Field.createConstantArray(name, type, value);
    fields.put(name, field);
    orderedFields.add(field);
  }

  void addValueField(String name, FieldType type) {
    Field<?> field = Field.createValue(name, type);
    fields.put(name, field);
    orderedFields.add(field);
  }

  void addValueArrayField(String name, FieldType type) {
    Field<?> field = Field.createValueArray(name, type);
    fields.put(name, field);
    orderedFields.add(field);
  }

  boolean hasField(String name, FieldType type) {
    return fields.containsKey(name) && fields.get(name).getType().equals(type);
  }

  // This cast should always be checked by calling hasField() before
  // getField().
  @SuppressWarnings("unchecked")
  <T> Field<T> getField(String name) {
    return (Field<T>) fields.get(name);
  }

  /**
   * @return the {@link List} of {@link Field}s in the order they were added
   */
  List<Field<?>> getFields() {
    return Collections.unmodifiableList(orderedFields);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((orderedFields == null) ? 0 : orderedFields.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MessageContext other = (MessageContext) obj;
    if (fields == null) {
      if (other.fields != null) return false;
    } else if (!fields.equals(other.fields)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (orderedFields == null) {
      if (other.orderedFields != null) return false;
    } else if (!orderedFields.equals(other.orderedFields)) return false;
    return true;
  }

}
