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

package org.ros.internal.transport;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;

import java.io.IOException;
import java.net.Socket;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 */
public abstract class IncomingMessageQueue<MessageType extends Message> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(IncomingMessageQueue.class);

  private static final int MESSAGE_BUFFER_CAPACITY = 8192;
  
  private final Class<MessageType> messageClass;
  private final CircularBlockingQueue<MessageType> messages;
  private final MessageReceivingThread thread;

  private LittleEndianDataInputStream stream;

  private final class MessageReceivingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          messages.put(receiveMessage(messageClass, stream));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public void cancel() {
      interrupt();
      try {
        stream.close();
      } catch (IOException e) {
        log.error("Failed to close stream.", e);
      }
    }
  }

  public IncomingMessageQueue(Class<MessageType> messageClass) {
    this.messageClass = messageClass;
    messages = new CircularBlockingQueue<MessageType>(MESSAGE_BUFFER_CAPACITY);
    thread = new MessageReceivingThread();
  }

  public void setSocket(Socket socket) throws IOException {
    stream = new LittleEndianDataInputStream(socket.getInputStream());
  }

  public MessageType take() throws InterruptedException {
    return messages.take();
  }

  public void shutdown() {
    thread.cancel();
  }

  public void start() {
    Preconditions.checkState(stream != null);
    thread.start();
  }

  protected abstract MessageType receiveMessage(Class<MessageType> messageClass,
      LittleEndianDataInputStream stream) throws IOException, InstantiationException,
      IllegalAccessException;
}
