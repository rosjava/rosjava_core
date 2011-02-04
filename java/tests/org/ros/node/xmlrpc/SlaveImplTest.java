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

package org.ros.node.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Matchers;
import org.ros.node.StatusCode;
import org.ros.transport.ProtocolNames;
import org.ros.transport.TcpRosDescription;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveImplTest {

  @Test
  public void testRequestTopic() {
    org.ros.node.server.Slave mockSlave = mock(org.ros.node.server.Slave.class);
    InetSocketAddress localhost = InetSocketAddress.createUnresolved("localhost", 1234);
    TcpRosDescription protocol = new TcpRosDescription(localhost);
    when(
        mockSlave.requestTopic(Matchers.<String>any(),
            Matchers.eq(Sets.newHashSet(ProtocolNames.TCPROS)))).thenReturn(protocol);
    SlaveImpl slave = new SlaveImpl(mockSlave);
    Object[][] protocols = new Object[][] {{ProtocolNames.TCPROS}};
    List<Object> response = slave.requestTopic("/foo", "/bar", protocols);
    assertEquals(response.get(0), StatusCode.SUCCESS.toInt());
    assertEquals(response.get(2), protocol.toList());
  }
}
