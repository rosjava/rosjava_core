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
import java.util.Arrays;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ShortArrayField extends Field {

  private final int size;

  private short[] value;

  public static ShortArrayField newVariable(FieldType type, int size, String name) {
    Preconditions.checkArgument(type.equals(PrimitiveFieldType.UINT8)
        || type.equals(PrimitiveFieldType.INT16));
    return new ShortArrayField(type, size, name, new short[Math.max(0, size)]);
  }

  private ShortArrayField(FieldType type, int size, String name, short[] value) {
    super(type, name, false);
    this.size = size;
    setValue(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public short[] getValue() {
    return value;
  }

  @Override
  public void setValue(Object value) {
    Preconditions.checkArgument(size < 0 || ((short[]) value).length == size);
    this.value = (short[]) value;
  }

  @Override
  public void serialize(ByteBuffer buffer) {
    buffer.putInt(value.length);
    for (short v : value) {
      type.serialize(v, buffer);
    }
  }

  @Override
  public void deserialize(ByteBuffer buffer) {
    int size = buffer.getInt();
    value = new short[size];
    for (int i = 0; i < size; i++) {
      value[i] = (Short) type.deserialize(buffer);
    }
  }

  @Override
  public String getMd5String() {
    return String.format("%s %s\n", type, name);
  }

  @Override
  public int getSerializedSize() {
    Preconditions.checkNotNull(value);
    // Reserve 4 bytes for the length.
    int size = 4;
    size += type.getSerializedSize() * value.length;
    return size;
  }

  @Override
  public String getJavaTypeName() {
    return type.getJavaTypeName() + "[]";
  }

  @Override
  public String toString() {
    return "ShortArrayField<" + type + ", " + name + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ShortArrayField other = (ShortArrayField) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!Arrays.equals(value, other.value))
      return false;
    return true;
  }
}
