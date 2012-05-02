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
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the value type
 */
public class ListField<T> extends Field {

  private List<T> value;

  public static <T> ListField<T> newVariable(FieldType type, String name) {
    return new ListField<T>(type, name, new ArrayList<T>());
  }

  private ListField(FieldType type, String name, List<T> value) {
    super(type, name, false);
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getValue() {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Object value) {
    Preconditions.checkState(!isConstant);
    this.value = (List<T>) value;
  }

  @Override
  public void serialize(ByteBuffer buffer) {
    buffer.putInt(value.size());
    for (T v : value) {
      type.serialize(v, buffer);
    }
  }

  @Override
  public void deserialize(ByteBuffer buffer) {
    int size = buffer.getInt();
    value = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      value.add(type.<T>deserialize(buffer));
    }
  }

  @Override
  public String getMd5String() {
    return String.format("%s %s\n", type, name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public int getSerializedSize() {
    Preconditions.checkNotNull(value);
    // Reserve 4 bytes for the length.
    int size = 4;
    if (type instanceof MessageFieldType) {
      for (Message message : (List<Message>) value) {
        size += message.toRawMessage().getSerializedSize();
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
  public String getJavaTypeName() {
    return String.format("java.util.List<%s>", type.getJavaTypeName());
  }

  @Override
  public String toString() {
    return "ListField<" + type + ", " + name + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ListField other = (ListField) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
