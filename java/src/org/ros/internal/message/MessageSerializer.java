// Copyright 2011 Google Inc. All Rights Reserved.

package org.ros.internal.message;


import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Your Name Here)
 *
 */
public class MessageSerializer implements org.ros.MessageSerializer<Message> {

  @Override
  public ByteBuffer serialize(Message message) {
    return message.serialize();
  }

}
