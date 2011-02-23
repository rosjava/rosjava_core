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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;
import org.ros.transport.ConnectionHeader;
import org.ros.transport.ConnectionHeaderFields;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Subscriber<T extends Message> extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Subscriber.class);

  private final CopyOnWriteArrayList<SubscriberListener<T>> listeners;
  private final SubscriberMessageQueue<T> in;
  private final MessageReadingThread thread;
  private final ImmutableMap<String, String> header;

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

  public static <S extends Message> Subscriber<S> create(String name, TopicDefinition description,
      Class<S> messageClass) {
    return new Subscriber<S>(name, description, messageClass);
  }

  private Subscriber(String name, TopicDefinition description, Class<T> messageClass) {
    super(description);
    this.listeners = new CopyOnWriteArrayList<Subscriber.SubscriberListener<T>>();
    this.in = SubscriberMessageQueue.create(messageClass);
    thread = new MessageReadingThread();
    header = ImmutableMap.<String, String>builder()
        .put(ConnectionHeaderFields.CALLER_ID, name)
        .putAll(description.toHeader())
        .build();
  }

  public void addListener(SubscriberListener<T> listener) {
    listeners.add(listener);
  }

  public void start(InetSocketAddress publisher) throws IOException {
    Socket socket = new Socket(publisher.getHostName(), publisher.getPort());
    handshake(socket);
    in.setSocket(socket);
    in.start();
    thread.start();
  }

  public void shutdown() {
    thread.cancel();
    in.shutdown();
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    ConnectionHeader.writeHeader(header, socket.getOutputStream());
    Map<String, String> incomingHeader = ConnectionHeader.readHeader(socket.getInputStream());
    if (DEBUG) {
      log.info("Sent handshake header: " + header);
      log.info("Received handshake header: " + incomingHeader);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }

}
