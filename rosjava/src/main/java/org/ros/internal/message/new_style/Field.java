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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public abstract class Field {

  protected final String name;
  protected final FieldType type;
  protected final boolean isConstant;

  protected Field(String name, FieldType type, boolean isConstant) {
    this.name = name;
    this.type = type;
    this.isConstant = isConstant;
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
  FieldType getType() {
    return type;
  }

  /**
   * @return <code>true</code> if this {@link ListField} represents a constant
   */
  boolean isConstant() {
    return isConstant;
  }

  abstract int getSerializedSize();

  abstract void serialize(ByteBuffer buffer);

  abstract void deserialize(ByteBuffer buffer);

  abstract <T> T getValue();

  abstract void setValue(Object value);

}
