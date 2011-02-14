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

package org.ros.service.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.ros.node.server.SlaveDescription;

import org.ros.service.ServiceCallback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Message;
import org.ros.service.ServiceDescription;
import org.ros.transport.ConnectionHeader;
import org.ros.transport.ConnectionHeaderFields;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Service<T extends Message> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Service.class);

  private final ServiceDescription description;

  public static <S extends Message> Service<S> create(Class<S> outgoingMessageClass,
      SlaveDescription slaveDescription) {
    return new Service<S>(slaveDescription);
  }

  private Service(SlaveDescription slaveDescription) {
    description = new ServiceDescription(slaveDescription);
  }

  public void start(InetSocketAddress server) throws UnknownHostException, IOException {
    Socket socket = new Socket(server.getHostName(), server.getPort());
    handshake(socket);
  }

  @VisibleForTesting
  void handshake(Socket socket) throws IOException {
    Map<String, String> header = description.toHeader();
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
   * @param serviceCallback
   */
  public void call(Message message, ServiceCallback<T> serviceCallback) {
  }

}
