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

package org.ros.internal.topic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.ros.internal.transport.tcp.TcpServer;

import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import org.ros.internal.node.server.SlaveIdentifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Publisher extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final PublisherMessageQueue out;
  private final List<SubscriberIdentifier> subscribers;
  private final TcpServer server;

  private class Server extends TcpServer {
    public Server(String hostname, int port) throws IOException {
      super(hostname, port);
    }

    @Override
    protected void onNewConnection(Socket socket) {
      try {
        handshake(socket);
        out.addSocket(socket);
      } catch (IOException e) {
        log.error("Failed to accept connection.", e);
      }
    }
  }

  public Publisher(TopicDefinition description, String hostname, int port) throws IOException {
    super(description);
    out = new PublisherMessageQueue();
    subscribers = Lists.newArrayList();

    // TODO(kwc) we only need one TCPROS server for the entire node.
    server = new Server(hostname, port);
  }

  public void start() {
    server.start();
    out.start();
  }

  public void shutdown() {
    server.shutdown();
    out.shutdown();
  }

  public PublisherIdentifier toPublisherIdentifier(SlaveIdentifier description) {
    return new PublisherIdentifier(description, getTopicDefinition());
  }

  public InetSocketAddress getAddress() {
    return server.getAddress();
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
  public void publish(Message message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    out.put(message);
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    Map<String, String> incomingHeader = ConnectionHeader.readHeader(socket.getInputStream());
    Map<String, String> header = getTopicDefinitionHeader();
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    SubscriberIdentifier subscriber = SubscriberIdentifier.createFromHeader(incomingHeader);
    subscribers.add(subscriber);
    ConnectionHeader.writeHeader(header, socket.getOutputStream());
  }

}
