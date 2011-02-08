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

package org.ros.service.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.service.ServiceDescription;
import org.ros.topic.Publisher;
import org.ros.topic.SubscriberDescription;
import org.ros.transport.ConnectionHeader;
import org.ros.transport.ConnectionHeaderFields;
import org.ros.transport.tcp.OutgoingMessageQueue;
import org.ros.transport.tcp.TcpServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Service {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final OutgoingMessageQueue out;
  private final TcpServer server;
  private final ServiceDescription description;
  
  public Service() throws IOException {
    out = new OutgoingMessageQueue();
    server = new Server(null, 0);
    description = null;
  }
  
  private class Server extends TcpServer {
    public Server(String hostname, int port) throws IOException {
      super(hostname, port);
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
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    Map<String, String> incomingHeader = ConnectionHeader.readHeader(socket.getInputStream());
    Map<String, String> header = topicDescription.toHeader();
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    SubscriberDescription subscriber = SubscriberDescription.createFromHeader(incomingHeader);
    subscribers.add(subscriber);
    ConnectionHeader.writeHeader(header, socket.getOutputStream());
  }
}
