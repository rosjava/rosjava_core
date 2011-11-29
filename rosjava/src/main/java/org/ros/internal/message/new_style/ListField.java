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

package org.ros.internal.message.new_style;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ListField<ValueType> extends Field {

  private List<ValueType> value;

  static <T> ListField<T> createConstant(String name, FieldType type, List<T> value) {
    return new ListField<T>(name, type, value, true);
  }

  static <T> ListField<T> createValue(String name, FieldType type) {
    // TODO(damonkohler): All values should have a default.
    return new ListField<T>(name, type, null, false);
  }

  private ListField(String name, FieldType type, List<ValueType> value, boolean isConstant) {
    super(name, type, isConstant);
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ValueType> getValue() {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Object value) {
    Preconditions.checkState(!isConstant);
    this.value = (List<ValueType>) value;
  }

  @Override
  public void serialize(ByteBuffer buffer) {
    buffer.putInt(value.size());
    for (ValueType v : value) {
      type.serialize(v, buffer);
    }
  }

  @Override
  public void deserialize(ByteBuffer buffer) {
    int size = buffer.getInt();
    value = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      value.add(type.<ValueType>deserialize(buffer));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public int getSerializedSize() {
    Preconditions.checkNotNull(value);
    // Reserve 4 bytes for the length.
    int size = 4;
    if (type instanceof MessageFieldType) {
      for (Message message : (List<Message>) value) {
        size += message.getSerializedSize();
      }
    } else if (type == PrimitiveFieldType.STRING) {
      for (String string : (List<String>) value) {
        // We only support ASCII strings and reserve 4 bytes for the length.
        size += string.length() + 4;
      }
    } else {
      size += type.getSerializedSize() * ((List<?>) value).size();
    }
    return size;
  }

  @Override
  public String toString() {
    return "ListField<" + name + ", " + type + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isConstant ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListField<?> other = (ListField<?>) obj;
    if (isConstant != other.isConstant) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (value == null) {
      if (other.value != null) return false;
    } else if (!value.equals(other.value)) return false;
    return true;
  }

}
