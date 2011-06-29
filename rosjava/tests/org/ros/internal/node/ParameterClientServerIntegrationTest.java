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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.ros.ParameterTree;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;
import org.ros.namespace.NodeNameResolver;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterClientServerIntegrationTest {

  private MasterServer masterServer;
  
  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    masterServer.start();
  }

  @Test
  public void testSetAndGetStrings() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", "baz");
    assertEquals("baz", parameters.getString("/foo/bar"));
    parameters.set("/foo/bar", "baz");
    assertEquals("baz", parameters.getString("/foo/bar"));
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("bar", "baz");
    assertEquals(expected, parameters.getMap("/foo"));
    node.shutdown();
  }

  @Test
  public void testSetAndGetInts() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", 42);
    assertEquals(42, parameters.getInteger("/foo/bar"));
    node.shutdown();
  }

  @Test
  public void testSetAndGetFloats() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", 0.42f);
    assertEquals(0.42f, parameters.getFloat("/foo/bar"), 0.01);
    node.shutdown();
  }

  @Test
  public void testSetAndGetDoubles() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", 0.42d);
    assertEquals(0.42d, parameters.getDouble("/foo/bar"), 0.01);
    node.shutdown();
  }

  @Test
  public void testSetAndGetLongs() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", 42l);
    assertEquals(42l, parameters.getLong("/foo/bar"));
    node.shutdown();
  }

  @Test
  public void testDeleteAndHas() throws RemoteException {
    Node node = Node.createPrivate(new GraphName("/node_name"), masterServer.getUri(), 0, 0);
    ParameterTree parameters = node.createParameterTree(NodeNameResolver.createDefault());
    parameters.set("/foo/bar", "baz");
    assertTrue(parameters.has("/foo/bar"));
    parameters.delete("/foo/bar");
    assertFalse(parameters.has("/foo/bar"));
    node.shutdown();
  }

}
