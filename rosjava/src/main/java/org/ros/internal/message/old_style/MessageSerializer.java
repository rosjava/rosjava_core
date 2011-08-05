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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

import org.ros.message.Message;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializer<MessageType> implements
    org.ros.message.MessageSerializer<MessageType> {

  /**
   * Header messages receive a monotonically increasing sequence number
   * automatically.
   */
  private static AtomicInteger seq = new AtomicInteger(0);

  @Override
  public ByteBuffer serialize(MessageType message) {
    ByteBuffer buffer = ByteBuffer.wrap(((Message) message).serialize(seq.incrementAndGet()));
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    return buffer;
  }

}
