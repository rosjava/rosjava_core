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

import org.ros.exception.RosRuntimeException;
import org.ros.message.Message;
import org.ros.message.MessageDefinition;

/**
 * Factory for message definitions and obtaining message classes.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinitionFactory implements org.ros.message.MessageDefinitionFactory {
  
  private final MessageFactory messageFactory;
  
  public MessageDefinitionFactory() {
    messageFactory = new MessageFactory();
  }

  /**
   * Get the message definition for a given message type.
   * 
   * @param messageType
   *          The string giving "ros package"/"message name", e.g. std_msgs/Time
   * @return The class.
   * 
   * @throws RosRuntimeException
   *           No class representing that name or the class is not accessible.
   */
  @Override
  public MessageDefinition newFromMessageType(String messageType) {
    try {
      Message message = messageFactory.newMessage(messageType);
      return MessageDefinition.newFromStrings(message.getDataType(), message.getMessageDefinition(),
          message.getMD5Sum());
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }
}
