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
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class Field<ValueType> {

  private final String name;
  private final FieldType type;
  private final boolean isList;
  private final boolean isConstant;

  private ValueType value;

  static <T> Field<T> createConstant(String name, FieldType type, T value) {
    return new Field<T>(name, type, value, false, true);
  }

  static <T> Field<T> createConstantArray(String name, FieldType type, T value) {
    return new Field<T>(name, type, value, true, true);
  }

  static <T> Field<T> createValue(String name, FieldType type) {
    return new Field<T>(name, type, null, false, false);
  }

  static <T> Field<T> createValueArray(String name, FieldType type) {
    return new Field<T>(name, type, null, true, false);
  }

  private Field(String name, FieldType type, ValueType value, boolean isList, boolean isConstant) {
    this.name = name;
    this.type = type;
    this.isList = isList;
    this.isConstant = isConstant;
    this.value = value;
  }

  public ValueType getValue() {
    return value;
  }

  public void setValue(ValueType value) {
    Preconditions.checkState(!isConstant);
    this.value = value;
  }

  /**
   * @return <code>true</code> if this {@link Field} represents an array
   */
  public boolean isList() {
    return isList;
  }

  /**
   * @return <code>true</code> if this {@link Field} represents a constant
   */
  public boolean isConstant() {
    return isConstant;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the type
   */
  public FieldType getType() {
    return type;
  }

  public void serialize(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  public int getSerializedSize() {
    Preconditions.checkNotNull(value);
    int length;
    if (type instanceof MessageFieldType) {
      throw new UnsupportedOperationException();
    } else {
      length = type.getSerializedSize();
    }
    if (isList) {
      return length * ((List<?>) value).size();
    }
    return length;
  }

  @Override
  public String toString() {
    return "Field<" + name + ", " + type + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isConstant ? 1231 : 1237);
    result = prime * result + (isList ? 1231 : 1237);
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
    Field<?> other = (Field<?>) obj;
    if (isConstant != other.isConstant) return false;
    if (isList != other.isList) return false;
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
