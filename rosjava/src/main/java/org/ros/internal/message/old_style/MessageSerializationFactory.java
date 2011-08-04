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

package org.ros.internal.message.old_style;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationFactory implements org.ros.message.MessageSerializationFactory {
  
  private final MessageFactory messageFactory;
  
  public MessageSerializationFactory() {
    messageFactory = new MessageFactory();
  }

  @Override
  public <MessageType> MessageSerializer<MessageType> newMessageSerializer(String messageType) {
    return new MessageSerializer<MessageType>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> MessageDeserializer<MessageType> newMessageDeserializer(String messageType) {
    MessageType message = messageFactory.newMessage(messageType);
    return new MessageDeserializer<MessageType>((Class<MessageType>) message.getClass());
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceRequestSerializer(
      String serviceType) {
    return new MessageSerializer<MessageType>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType>
      newServiceRequestDeserializer(String serviceType) {
    MessageType message = messageFactory.newServiceRequest(serviceType);
    return new MessageDeserializer<MessageType>((Class<MessageType>) message.getClass());
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceResponseSerializer(
      String serviceType) {
    return new MessageSerializer<MessageType>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType>
      newServiceResponseDeserializer(String serviceType) {
    MessageType message = messageFactory.newServiceResponse(serviceType);
    return new MessageDeserializer<MessageType>((Class<MessageType>) message.getClass());
  }

}