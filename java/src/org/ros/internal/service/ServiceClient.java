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

package org.ros.internal.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceClient<ResponseMessageType extends Message> {

  private static final boolean DEBUG = true;
  private static final Log log = LogFactory.getLog(ServiceClient.class);
  
  private final ServiceClientOutgoingMessageQueue out;
  private final ServiceClientIncomingMessageQueue<ResponseMessageType> in;

  public static <S extends Message> ServiceClient<S> create(
      Class<S> incomingMessageClass, String name, ServiceIdentifier serviceIdentifier) {
    return new ServiceClient<S>(incomingMessageClass, name, serviceIdentifier);
  }

  private Map<String, String> header;
  
  private ServiceClient(Class<ResponseMessageType> responseMessageClass, String name,
      ServiceIdentifier serviceIdentifier) {
    header = ImmutableMap.<String, String>builder()
        .put(ConnectionHeaderFields.CALLER_ID, name)
        // TODO(damonkohler): Support non-persistent connections.
        .put(ConnectionHeaderFields.PERSISTENT, "1")
        .putAll(serviceIdentifier.toHeader())
        .build();
    in = new ServiceClientIncomingMessageQueue<ResponseMessageType>(responseMessageClass);
    out = new ServiceClientOutgoingMessageQueue();
  }

  public void start(InetSocketAddress server) throws UnknownHostException, IOException {
    Socket socket = new Socket(server.getHostName(), server.getPort());
    handshake(socket);
    in.setSocket(socket);
    in.start();
    out.addSocket(socket);
    out.start();
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    ConnectionHeader.writeHeader(header, socket.getOutputStream());
    Map<String, String> incomingHeader = ConnectionHeader.readHeader(socket.getInputStream());
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }

  /**
   * @param message
   * @throws InterruptedException 
   */
  public ResponseMessageType call(Message message) throws InterruptedException {
    out.add(message);
    return in.take();
  }

}
