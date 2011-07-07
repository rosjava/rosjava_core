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

import com.google.common.base.Preconditions;

import org.ros.internal.message.new_style.MessageDefinition;
import org.ros.message.Message;


/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinitionFactory {

  @SuppressWarnings("unchecked")
  public static MessageDefinition createFromString(String messageType) {
    Preconditions.checkArgument(messageType.split("/").length == 2);
    Class<Message> messageClass;
    Message message;
    try {
      messageClass =
          (Class<Message>) MessageDefinitionFactory.class.getClassLoader().loadClass(
              "org.ros.message." + messageType.replace('/', '.'));
      message = messageClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return MessageDefinition.create(message.getDataType(), message.getMessageDefinition(),
        message.getMD5Sum());
  }

}
