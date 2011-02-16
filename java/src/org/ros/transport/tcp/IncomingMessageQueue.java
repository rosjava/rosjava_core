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

package org.ros.transport.tcp;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;
import org.ros.transport.LittleEndianDataInputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author damonkohler@google.com (Damon Kohler)
 *
 * @param <T>
 */
public class IncomingMessageQueue<T extends Message> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(IncomingMessageQueue.class);

  private final Class<T> messageClass;
  private final BlockingQueue<T> messages;
  private final MessageReceivingThread thread;

  private LittleEndianDataInputStream stream;
  
  private final class MessageReceivingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!isInterrupted()) {
          messages.put(receiveMessage());
        }
      } catch (InterruptedException e) {
        // Cancelable
        if (DEBUG) {
          log.info("Canceled.");
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
  
  public static <S extends Message> IncomingMessageQueue<S> create(Class<S> messageClass) {
    return new IncomingMessageQueue<S>(messageClass);
  }

  private IncomingMessageQueue(Class<T> messageClass) {
    this.messageClass = messageClass;
    messages = new LinkedBlockingQueue<T>();
    thread = new MessageReceivingThread();
  }
  
  public void setSocket(Socket socket) throws IOException {
    stream = new LittleEndianDataInputStream(socket.getInputStream());
  }

  public T take() throws InterruptedException {
    return messages.take();
  }

  public void shutdown() {
    thread.cancel();
  }

  public void start() {
    Preconditions.checkState(stream != null);
    thread.start();
  }

  private T receiveMessage() throws IOException, InstantiationException, IllegalAccessException {
    int size = stream.readInt();
    byte[] buffer = stream.readByteArray(size);
    T message = messageClass.newInstance();
    message.deserialize(buffer);
    if (DEBUG) {
      log.info("Received message: " + message.toString());
    }
    return message;
  }
}
