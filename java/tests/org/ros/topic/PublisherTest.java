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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ros.communication.MessageDescription;
import org.ros.node.server.SlaveDescription;
import org.ros.node.server.SubscriberDescription;
import org.ros.transport.ConnectionHeader;
import org.ros.transport.ConnectionHeaderFields;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherTest {

  @Test
  public void testHandshake() throws IOException {
    Socket socket = mock(Socket.class);
    TopicDescription topicDescription = new TopicDescription("/topic",
        MessageDescription.createFromMessage(new org.ros.communication.std_msgs.String()));
    SlaveDescription slaveDescription = new SlaveDescription("/caller", null);
    SubscriberDescription subscriberDescription = new SubscriberDescription(slaveDescription,
        topicDescription);
    Map<String, String> header = subscriberDescription.toHeader();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ConnectionHeader.writeHeader(header, outputStream);
    byte[] buffer = outputStream.toByteArray();
    when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(buffer));
    outputStream = new ByteArrayOutputStream();
    when(socket.getOutputStream()).thenReturn(outputStream);

    Publisher publisher = new Publisher(topicDescription, "localhost", 1234);
    publisher.handshake(socket);
    buffer = outputStream.toByteArray();
    Map<String, String> result = ConnectionHeader.readHeader(new ByteArrayInputStream(buffer));
    assertEquals(result.get(ConnectionHeaderFields.TYPE), header.get(ConnectionHeaderFields.TYPE));
    assertEquals(result.get(ConnectionHeaderFields.MD5_CHECKSUM), header.get(ConnectionHeaderFields.MD5_CHECKSUM));
  }
}
