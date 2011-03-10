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

package org.ros.internal.transport.tcp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.transport.Connection;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * Store connection for a TCPROS socket-based connection.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TcpRosConnection implements Connection {

  private static final boolean DEBUG = false;
  private final Socket socket;
  private static final Log log = LogFactory.getLog(TcpRosConnection.class);

  public Socket getSocket() {
    return socket;
  }

  private TcpRosConnection(Socket socket) {
    this.socket = socket;
  }

  /**
   * Create connection to TCPROS TCP/IP server of publisher. This also does the
   * initial handshake exchange for the socket.
   * 
   * @param tcprosServerAddress
   * @param header
   *          TCPROS connection header.
   * @return Connection instance. Contains initialized socket with handshake
   *         already exchanged.
   * @throws IOException
   */
  public static TcpRosConnection createOutgoing(InetSocketAddress tcprosServerAddress,
      ImmutableMap<String, String> header) throws IOException {
    Socket socket = new Socket(tcprosServerAddress.getHostName(), tcprosServerAddress.getPort());
    subscriberHandshake(socket, header);
    return new TcpRosConnection(socket);
  }

  /**
   * Implements the subscriber-side of a TCPROS handshake. The handshake is an
   * initial exchange over the TCP/IP socket created between the publisher and
   * the subscriber. The subscriber initiates the connection.
   */
  @VisibleForTesting
  static void subscriberHandshake(Socket socket, ImmutableMap<String, String> header)
      throws IOException {
    ConnectionHeader.writeHeader(header, socket.getOutputStream());
    Map<String, String> incomingHeader = ConnectionHeader.readHeader(socket.getInputStream());
    if (DEBUG) {
      log.info("Sent sub handshake header: " + header);
      log.info("Received sub handshake header: " + incomingHeader);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }

}
