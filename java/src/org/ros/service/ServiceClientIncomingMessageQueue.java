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

package org.ros.service;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;
import org.ros.transport.IncomingMessageQueue;
import org.ros.transport.LittleEndianDataInputStream;

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 */
public class ServiceClientIncomingMessageQueue<MessageType extends Message> extends
    IncomingMessageQueue<MessageType> {

  private static final boolean DEBUG = true;
  private static final Log log = LogFactory.getLog(ServiceClientIncomingMessageQueue.class);
  
  /**
   * @param messageClass
   */
  public ServiceClientIncomingMessageQueue(Class<MessageType> messageClass) {
    super(messageClass);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.transport.IncomingMessageQueue#receiveMessage(java.lang.Class,
   * org.ros.transport.LittleEndianDataInputStream)
   */
  @Override
  protected MessageType receiveMessage(Class<MessageType> messageClass,
      LittleEndianDataInputStream stream) throws IOException, InstantiationException,
      IllegalAccessException {
    byte[] ok = stream.readByteArray(1);
    Preconditions.checkState(ok[0] == 1);
    int size = stream.readInt();
    byte[] buffer = stream.readByteArray(size);
    MessageType message = messageClass.newInstance();
    message.deserialize(buffer);
    if (DEBUG) {
      log.info("Received buffer of size " + size);
      log.info("Received buffer: " + buffer);
    }
    return message;
  }
}
