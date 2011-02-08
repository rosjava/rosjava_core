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

package org.ros.transport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ros.transport.tcp.IncomingMessageQueue;
import org.ros.transport.tcp.OutgoingMessageQueue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageQueueIntegrationTest {

  private final class ServerThread extends Thread {
    public Socket server;
    private final ServerSocket serverSocket;
    private final CountDownLatch latch;

    private ServerThread(ServerSocket serverSocket, CountDownLatch latch) {
      this.serverSocket = serverSocket;
      this.latch = latch;
    }

    @Override
    public void run() {
      try {
        server = serverSocket.accept();
        latch.countDown();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  public void testSendAndReceiveMessage() throws IOException, InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final ServerSocket serverSocket = new ServerSocket(0);
    final Socket server;
    ServerThread serverThread = new ServerThread(serverSocket, latch);
    serverThread.start();
    Socket client = new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
    latch.await(3, TimeUnit.SECONDS);
    OutgoingMessageQueue out = new OutgoingMessageQueue();
    out.addSocket(serverThread.server);
    IncomingMessageQueue<org.ros.message.std.String> in =
        IncomingMessageQueue.create(org.ros.message.std.String.class);
    in.setSocket(client);
    
    out.start();
    in.start();
    org.ros.message.std.String hello = new org.ros.message.std.String();
    hello.data = "Would you like to play a game?";
    out.add(hello);
    assertEquals(in.take(), hello);
  }
}
