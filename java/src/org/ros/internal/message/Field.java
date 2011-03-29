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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class Field {

  private final String name;
  private final FieldType type;
  private final boolean isArray;
  private final boolean isConstant;

  static Field createConstant(String name, FieldType type) {
    return new Field(name, type, false, true);
  }

  static Field createConstantArray(String name, FieldType type) {
    return new Field(name, type, true, true);
  }

  static Field createValue(String name, FieldType type) {
    return new Field(name, type, false, false);
  }

  static Field createValueArray(String name, FieldType type) {
    return new Field(name, type, true, false);
  }

  private Field(String name, FieldType type, boolean isArray, boolean isConstant) {
    this.name = name;
    this.type = type;
    this.isArray = isArray;
    this.isConstant = isConstant;
  }

  /**
   * @return <code>true</code> if this {@link Field} represents an array
   */
  public boolean isArray() {
    return isArray;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isArray ? 1231 : 1237);
    result = prime * result + (isConstant ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Field other = (Field) obj;
    if (isArray != other.isArray) return false;
    if (isConstant != other.isConstant) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (type != other.type) return false;
    return true;
  }

}
