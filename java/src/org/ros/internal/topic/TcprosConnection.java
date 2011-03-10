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
import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * Store connection for a TCPROS socket-based connection.
 * 
 * @author kwc
 * 
 */
public class TcprosConnection implements Connection {

  private static final boolean DEBUG = false;
  private final Socket socket;
  private static final Log log = LogFactory.getLog(TcprosConnection.class);
  
  public Socket getSocket() {
    return socket;
  }

  private TcprosConnection(Socket socket) {
    this.socket = socket;
  }

  public static TcprosConnection createOutgoing(InetSocketAddress tcprosServerAddress, ImmutableMap<String, String> header) throws IOException {

    Socket socket = new Socket(tcprosServerAddress.getHostName(), tcprosServerAddress.getPort());
    handshake(socket, header);
    return new TcprosConnection(socket);
  }

  @VisibleForTesting
  static
  void handshake(Socket socket, ImmutableMap<String, String> header) throws IOException {
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
