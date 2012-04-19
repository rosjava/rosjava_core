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

import org.ros.internal.message.MessageDefinitionParser.MessageDefinitionVisitor;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageContextFactory {

  private final MessageFactory messageFactory;

  public MessageContextFactory(MessageFactory messageFactory) {
    Preconditions.checkNotNull(messageFactory);
    this.messageFactory = messageFactory;
  }

  public MessageContext newFromMessageDeclaration(final MessageDeclaration messageDeclaration) {
    final MessageContext context = new MessageContext(messageDeclaration);
    MessageDefinitionVisitor visitor = new MessageDefinitionVisitor() {
      private FieldType getFieldType(String type) {
        Preconditions.checkArgument(!type.equals(messageDeclaration.getType()),
            "Message definitions may not be self-referential: " + messageDeclaration);
        FieldType fieldType;
        if (PrimitiveFieldType.existsFor(type)) {
          fieldType = PrimitiveFieldType.valueOf(type.toUpperCase());
        } else {
          fieldType = new MessageFieldType(MessageIdentifier.newFromType(type), messageFactory);
        }
        return fieldType;
      }

      @Override
      public void scalar(String type, String name) {
        context.addValueField(getFieldType(type), name);
      }

      @Override
      public void list(String type, int size, String name) {
        context.addValueListField(getFieldType(type), name);
      }

      @Override
      public void constant(String type, String name, String value) {
        Preconditions.checkArgument(PrimitiveFieldType.existsFor(type),
            "Only primitive field types may be constant: " + messageDeclaration);
        PrimitiveFieldType primitiveFieldType = PrimitiveFieldType.valueOf(type.toUpperCase());
        context.addConstantField(primitiveFieldType, name,
            primitiveFieldType.parseFromString(value));
      }
    };
    MessageDefinitionParser messageDefinitionParser = new MessageDefinitionParser(visitor);
    messageDefinitionParser.parse(messageDeclaration.getType(), messageDeclaration.getDefinition());
    return context;
  }

  public MessageContext newFromStrings(String messageType, String messageDefinition) {
    MessageDeclaration messageDeclaration =
        MessageDeclaration.newFromStrings(messageType, messageDefinition);
    return newFromMessageDeclaration(messageDeclaration);
  }
}
