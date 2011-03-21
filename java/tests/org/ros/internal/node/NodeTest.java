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
import static org.junit.Assert.assertTrue;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.ros.internal.node.server.MasterServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeTest {

  @Test
  public void testCreatePublic() throws XmlRpcException, IOException, URISyntaxException {
    MasterServer masterServer = new MasterServer(NodeBindAddress.createDefault(0));
    masterServer.start();

    Node node = Node.createPublic("/node_name", masterServer.getUri(), "publicname", 0, 0);
    node.start();

    InetSocketAddress address = node.getTcpRosServer().getPublicAddress();
    assertTrue(address.getPort() > 0);
    assertEquals("publicname", address.getHostName());

    URI uri = node.getSlaveServer().getUri();
    assertTrue(uri.getPort() > 0);
    assertEquals("publicname", uri.getHost());

    node.stop();
  }
}
