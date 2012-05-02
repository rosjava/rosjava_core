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

import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class ValueField<T> extends Field {

  private T value;

  static <T> ValueField<T> newConstant(FieldType type, String name, T value) {
    return new ValueField<T>(type, name, value, true);
  }

  @SuppressWarnings("unchecked")
  static <T> ValueField<T> newVariable(FieldType type, String name) {
    return new ValueField<T>(type, name, (T) type.getDefaultValue(), false);
  }

  private ValueField(FieldType type, String name, T value, boolean isConstant) {
    super(type, name, isConstant);
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getValue() {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Object value) {
    Preconditions.checkState(!isConstant);
    this.value = (T) value;
  }

  @Override
  public void serialize(ByteBuffer buffer) {
    type.serialize(value, buffer);
  }

  @Override
  public void deserialize(ByteBuffer buffer) {
    value = type.<T>deserialize(buffer);
  }

  @Override
  public String getMd5String() {
    return String.format("%s %s\n", type, name);
  }

  @Override
  public int getSerializedSize() {
    Preconditions.checkNotNull(value, "Cannot serialize null field: " + this);
    if (type instanceof MessageFieldType) {
      return ((Message) value).toRawMessage().getSerializedSize();
    } else if (type == PrimitiveFieldType.STRING) {
      // We only support ASCII strings and reserve 4 bytes for the length.
      return ((String) value).length() + 4;
    } else {
      return type.getSerializedSize();
    }
  }

  @Override
  public String getJavaTypeName() {
    return type.getJavaTypeName();
  }

  @Override
  public String toString() {
    return "ValueField<" + type + ", " + name + ">";
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
    ValueField other = (ValueField) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
