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

import org.ros.message.MessageDeclaration;
import org.ros.message.MessageIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageContext {

  private final MessageDeclaration messageDeclaration;
  private final Map<String, Field> fields;
  private final List<Field> orderedFields;

  public static MessageContext newFromStrings(String type, String definition) {
    MessageIdentifier messageIdentifier = MessageIdentifier.newFromType(type);
    MessageDeclaration messageDeclaration = new MessageDeclaration(messageIdentifier, definition);
    return new MessageContext(messageDeclaration);
  }

  public MessageContext(MessageDeclaration messageDeclaration) {
    this.messageDeclaration = messageDeclaration;
    this.fields = Maps.newConcurrentMap();
    this.orderedFields = Lists.newArrayList();
  }

  public MessageIdentifier getMessageIdentifer() {
    return messageDeclaration.getMessageIdentifier();
  }

  public String getType() {
    return messageDeclaration.getType();
  }

  public String getPackage() {
    return messageDeclaration.getPackage();
  }

  public String getName() {
    return messageDeclaration.getName();
  }

  public String getDefinition() {
    return messageDeclaration.getDefinition();
  }

  public void addField(Field field) {
    fields.put(field.getName(), field);
    orderedFields.add(field);
  }

  public boolean hasField(FieldType type, String name) {
    return fields.containsKey(name) && fields.get(name).getType().equals(type);
  }

  public Field getField(String name) {
    return fields.get(name);
  }

  /**
   * @return the {@link List} of {@link Field}s in the order they were added
   */
  public List<Field> getFields() {
    return Collections.unmodifiableList(orderedFields);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
    result = prime * result + ((messageDeclaration == null) ? 0 : messageDeclaration.hashCode());
    result = prime * result + ((orderedFields == null) ? 0 : orderedFields.hashCode());
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
    MessageContext other = (MessageContext) obj;
    if (fields == null) {
      if (other.fields != null)
        return false;
    } else if (!fields.equals(other.fields))
      return false;
    if (messageDeclaration == null) {
      if (other.messageDeclaration != null)
        return false;
    } else if (!messageDeclaration.equals(other.messageDeclaration))
      return false;
    if (orderedFields == null) {
      if (other.orderedFields != null)
        return false;
    } else if (!orderedFields.equals(other.orderedFields))
      return false;
    return true;
  }
}