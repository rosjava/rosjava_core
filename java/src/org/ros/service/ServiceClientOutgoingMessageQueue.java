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

import org.ros.message.Message;
import org.ros.transport.LittleEndianDataOutputStream;
import org.ros.transport.OutgoingMessageQueue;

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceClientOutgoingMessageQueue extends OutgoingMessageQueue {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ros.transport.OutgoingMessageQueue#sendMessage(org.ros.message.Message,
   * org.ros.transport.LittleEndianDataOutputStream)
   */
  @Override
  protected void sendMessage(Message message, LittleEndianDataOutputStream stream)
      throws IOException {
    byte[] data = message.serialize(0 /* unused seq */);
    stream.writeField(data);
  }
}
