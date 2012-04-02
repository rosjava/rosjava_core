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

package org.ros.message;

import com.google.common.base.Preconditions;

/**
 * Uniquely identifies a message.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageIdentifier {

  private final String pkg;
  private final String name;
  
  public static MessageIdentifier newFromType(String type) {
    Preconditions.checkNotNull(type);
    Preconditions.checkArgument(type.contains("/"), "Type must be fully qualified: " + type);
    String[] packageAndName = type.split("/", 2);
    return new MessageIdentifier(packageAndName[0], packageAndName[1]);
  }

  public MessageIdentifier(String pkg, String name) {
    this.pkg = pkg;
    this.name = name;
  }

  public String getType() {
    return String.format("%s/%s", pkg, name);
  }

  public String getPackage() {
    return pkg;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("MessageIdentifier<%s/%s>", pkg, name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((pkg == null) ? 0 : pkg.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MessageIdentifier other = (MessageIdentifier) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (pkg == null) {
      if (other.pkg != null)
        return false;
    } else if (!pkg.equals(other.pkg))
      return false;
    return true;
  }
}
