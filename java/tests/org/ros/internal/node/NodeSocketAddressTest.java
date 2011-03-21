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

package org.ros.internal.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeSocketAddressTest {

  @Test
  public void testGetPublicHostname() throws UnknownHostException {

    NodeBindAddress address = new NodeBindAddress(new InetSocketAddress(1234), "override");
    assertEquals(address, new NodeBindAddress(new InetSocketAddress(1234), "override"));
    assertEquals("override", address.getPublicHostName());
    assertEquals(1234, address.getPort());
    assertEquals(new InetSocketAddress(1234), address.getBindAddress());

    address = new NodeBindAddress(new InetSocketAddress("localhost", 1234), "override");
    assertEquals(address, new NodeBindAddress(new InetSocketAddress("localhost", 1234), "override"));
    assertEquals("override", address.getPublicHostName());
    assertEquals(1234, address.getPort());
    assertEquals(new InetSocketAddress("localhost", 1234), address.getBindAddress());

  }
}
