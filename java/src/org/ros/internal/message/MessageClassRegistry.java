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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageClassRegistry {

  private final Map<String, Class<? extends Message>> messageClasses;

  public MessageClassRegistry() {
    messageClasses = Maps.newConcurrentMap();
  }

  public <MessageType extends Message> void put(String messageName, Class<MessageType> messageClass) {
    messageClasses.put(messageName, messageClass);
  }

  @SuppressWarnings("unchecked")
  public <MessageType extends Message> Class<MessageType> get(String messageName) {
    if (!messageClasses.containsKey(messageName)) {
      // If we don't know a specific message class to use with the proxy, fall
      // back to the generic Message interface.
      return (Class<MessageType>) Message.class;
    }
    return (Class<MessageType>) messageClasses.get(messageName);
  }

}
