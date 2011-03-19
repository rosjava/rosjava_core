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

package org.ros.internal.node.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.ros.internal.node.NodeSocketAddress;
import org.ros.internal.node.xmlrpc.Node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeServerTest {

  class FakeNode implements Node {

  }

  @Test
  public void testGetUri() throws URISyntaxException, XmlRpcException, IOException {
    NodeSocketAddress bindAddress = new NodeSocketAddress(new InetSocketAddress(0), "override");
    NodeServer nodeServer = new NodeServer(bindAddress);
    try {
      nodeServer.getUri();
      fail("should not have succeeded before startup");
    } catch (RuntimeException e) {
    }

    nodeServer.start(FakeNode.class, new FakeNode());
    URI uri = nodeServer.getUri();
    assertEquals(uri.getHost(), "override");
    assertFalse(uri.getPort() == 0);

    nodeServer.shutdown();

    // Test with loopback binding.
    bindAddress = new NodeSocketAddress(new InetSocketAddress("127.0.0.1", 0), "override2");
    nodeServer = new NodeServer(bindAddress);
    try {
      nodeServer.getUri();
      fail("should not have succeeded before startup");
    } catch (RuntimeException e) {
    }

    nodeServer.start(FakeNode.class, new FakeNode());
    uri = nodeServer.getUri();
    assertEquals(uri.getHost(), "override2");
    assertFalse(uri.getPort() == 0);

    nodeServer.shutdown();

  }
}
