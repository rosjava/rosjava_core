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
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.communication.Message;
import org.ros.transport.Header;
import org.ros.transport.HeaderFields;
import org.ros.transport.OutgoingMessageQueue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class Publisher extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final OutgoingMessageQueue out;
  private final ImmutableMap<String, String> header;
  private final List<SubscriberDescription> subscribers;

  public Publisher(TopicDescription topicDescription, String hostname) {
    super(topicDescription, hostname);
    out = new OutgoingMessageQueue();
    header = new ImmutableMap.Builder<String, String>()
        .put(HeaderFields.TYPE, topicDescription.getMessageType())
        .put(HeaderFields.MD5_CHECKSUM, topicDescription.getMd5Checksum())
        .build();
    subscribers = Lists.newArrayList();
  }

  @Override
  public void start(int port) throws IOException {
    super.start(port);
    out.start();
  }

  @Override
  public void shutdown() {
    super.shutdown();
    out.shutdown();
  }

  public void publish(Message message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    out.add(message);
  }

  @Override
  protected void onNewConnection(Socket socket) {
    try {
      handshake(socket);
      Preconditions.checkState(socket.isConnected());
      out.addSocket(socket);
    } catch (IOException e) {
      log.error("Failed to accept connection.", e);
    }
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    Map<String, String> incomingHeader = Header.readHeader(socket.getInputStream());
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header); 
    }
    Preconditions.checkState(incomingHeader.get(HeaderFields.TYPE).equals(
        header.get(HeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(HeaderFields.MD5_CHECKSUM).equals(
        header.get(HeaderFields.MD5_CHECKSUM)));
    SubscriberDescription subscriber = SubscriberDescription.CreateFromHeader(incomingHeader);
    subscribers.add(subscriber);
    Header.writeHeader(header, socket.getOutputStream());
  }
}
