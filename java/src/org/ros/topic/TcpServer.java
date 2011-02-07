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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public abstract class TcpServer {
  
  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(TcpServer.class);

  private final InetSocketAddress address;
  private final ServerThread thread;
  private final ServerSocket server;
  
  public TcpServer(String hostname, int port) throws IOException {
    server = new ServerSocket(port);
    address = InetSocketAddress.createUnresolved(hostname, server.getLocalPort());
    thread = new ServerThread();
  }

  protected abstract void onNewConnection(Socket socket);

  private final class ServerThread extends Thread {
    
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          onNewConnection(server.accept());
        } catch (IOException e) {
          log.error("Connection failed.", e);
        }
      }
    }

    public void cancel() {
      interrupt();
      try {
        server.close();
      } catch (IOException e) {
        log.error("Server shutdown failed.", e);
      }
    }
  }

  public void start() {
    thread.start();
    if (DEBUG) {
      log.info("Bound to: " + getAddress());
    }
  }

  public void shutdown() {
    thread.cancel();
  }

  public InetSocketAddress getAddress() {
    return address;
  }

}
