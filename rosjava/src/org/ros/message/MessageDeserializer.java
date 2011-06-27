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

import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDeserializer<MessageType> implements org.ros.MessageDeserializer<MessageType> {

  private final Class<MessageType> messageClass;

  @SuppressWarnings("unchecked")
  public static <T> MessageDeserializer<T> createFromString(String messageType) {
    Preconditions.checkArgument(messageType.split("/").length == 2);
    Class<T> messageClass;
    try {
      messageClass = (Class<T>) Class.forName("org.ros.message." + messageType.replace('/', '.'));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new MessageDeserializer<T>(messageClass);
  }

  public static <T> MessageDeserializer<T> createServiceResponseFromString(String serviceType) {
    Preconditions.checkArgument(serviceType.split("/").length == 2);
    return createFromString("srv." + serviceType + "$Response");
  }

  public static <T> MessageDeserializer<T> createServiceRequestFromString(String serviceType) {
    Preconditions.checkArgument(serviceType.split("/").length == 2);
    return createFromString("srv." + serviceType + "$Request");
  }

  public MessageDeserializer(Class<MessageType> messageClass) {
    Preconditions.checkArgument(Message.class.isAssignableFrom(messageClass));
    this.messageClass = messageClass;
  }

  @Override
  public MessageType deserialize(ByteBuffer buffer) {
    MessageType message;
    try {
      message = messageClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException();
    }
    ((Message) message).deserialize(buffer);
    return message;
  }

}
