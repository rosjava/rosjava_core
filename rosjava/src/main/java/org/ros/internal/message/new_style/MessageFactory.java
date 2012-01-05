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
import java.nio.ByteOrder;

// TODO(damonkohler): This should implement org.ros.message.MessageFactory
/**
 * Creates {@link MessageImpl} instances.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory {

  private final MessageDefinitionProvider messageDefinitionProvider;
  private final DefaultedClassMap<Message> messageClasses;
  private final MessageContextFactory messageContextFactory;

  public MessageFactory(MessageDefinitionProvider messageDefinitionProvider,
      DefaultedClassMap<Message> messageClasses) {
    this.messageDefinitionProvider = messageDefinitionProvider;
    this.messageClasses = messageClasses;
    messageContextFactory = new MessageContextFactory(this);
  }

  <T> T newProxy(String messageName, String messageDefinition, Class<T> messageClass) {
    MessageContext context = messageContextFactory.newFromStrings(messageName, messageDefinition);
    return ProxyFactory.newProxy(messageClass, new MessageImpl(context));
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> T newMessage(String messageName) {
    MessageContext context =
        messageContextFactory.newFromStrings(messageName,
            messageDefinitionProvider.get(messageName));
    return ProxyFactory.newProxy((Class<T>) messageClasses.get(messageName), new MessageImpl(
        context));
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> MessageType deserializeMessage(String messageName,
      ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    MessageType message =
        (MessageType) newProxy(messageName, messageDefinitionProvider.get(messageName),
            messageClasses.get(messageName));
    for (Field field : message.getFields()) {
      if (!field.isConstant()) {
        field.deserialize(buffer);
      }
    }
    return message;
  }
}
