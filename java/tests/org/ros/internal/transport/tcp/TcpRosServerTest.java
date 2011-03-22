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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.topic.TopicManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TcpRosServerTest {

  /**
   * Basic test to exercise getAddress() semantics. Higher-level integration
   * tests are dependent on consistent behavior here.
   */
  @Test
  public void testGetAddress() throws Exception {
    TopicManager topicManagerMock = mock(TopicManager.class);
    ServiceManager serviceManagerMock = mock(ServiceManager.class);
    TcpRosServer tcpRosServer =
        new TcpRosServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic(),
            topicManagerMock, serviceManagerMock);

    // Not really sure of the right behavior here, but getAddress() raises
    // before start and should also raise after shutdown.
    try {
      tcpRosServer.getAddress();
      fail();
    } catch (RuntimeException e) {
    }

    tcpRosServer.start();

    InetSocketAddress address = tcpRosServer.getAddress();
    assertTrue(address.getPort() > 0);
    assertEquals(InetAddress.getLocalHost().getCanonicalHostName(), address.getHostName());

    tcpRosServer.shutdown();
    // Not really sure of the right behavior here, but getAddress() raises
    // before start and should also raise after shutdown.
    try {
      tcpRosServer.getAddress();
      fail();
    } catch (RuntimeException e) {
    }
  }

}
