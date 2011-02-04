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

package org.ros.topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.communication.Message;
import org.ros.transport.IncomingMessageQueue;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Subscriber<T extends Message> extends Topic {

  private static final boolean DEBUG = true;
  private static final Log log = LogFactory.getLog(Subscriber.class);

  private final CopyOnWriteArrayList<SubscriberListener<T>> listeners;
  private final IncomingMessageQueue<T> in;
  private final MessageReadingThread thread;

  public interface SubscriberListener<T extends Message> {
    public void onNewMessage(T message);
  }

  private final class MessageReadingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          T message = in.take();
          if (DEBUG) {
            log.info("Received message: " + message);
          }
          for (SubscriberListener<T> listener : listeners) {
            if (isInterrupted()) {
              break;
            }
            listener.onNewMessage(message);
          }
        }
      } catch (InterruptedException e) {
        // Cancelable
        if (DEBUG) {
          log.info("Canceled.");
        }
      }
    }

    public void cancel() {
      interrupt();
    }
  }

  public Subscriber(Class<T> messageClass, TopicDescription description, Socket publisherSocket)
      throws IOException {
    super(description);
    this.listeners = new CopyOnWriteArrayList<Subscriber.SubscriberListener<T>>();
    this.in = new IncomingMessageQueue<T>(messageClass, publisherSocket);
    thread = new MessageReadingThread();
  }

  public void addListener(SubscriberListener<T> listener) {
    listeners.add(listener);
  }

  public void start() {
    thread.start();
    in.start();
  }

  public void shutdown() {
    thread.cancel();
    in.shutdown();
  }
}
