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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.ros.internal.namespace.GraphName;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterServerTest {

  @Test
  public void testGetNonExistent() {
    ParameterServer server = new ParameterServer();
    assertEquals(null, server.get(new GraphName("/foo")));
    assertEquals(null, server.get(new GraphName("/foo/bar")));
  }

  @Test
  public void testSetAndGetShallow() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo")));
  }

  @Test
  public void testSetAndGetDeep() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo/bar"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo/bar")));
  }

  @Test
  public void testSetAndGet() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo")));
    server.set(new GraphName("/foo/bar"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo/bar")));
    server.set(new GraphName("/foo/bar/baz"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo/bar/baz")));
  }

  @Test
  public void testSetDeepAndGetShallow() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo/bar"), "bloop");
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("bar", "bloop");
    assertEquals(expected, server.get(new GraphName("/foo")));
  }

  @Test
  public void testSetOverwritesMap() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo/bar"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo/bar")));
    server.set(new GraphName("/foo"), "bloop");
    assertEquals("bloop", server.get(new GraphName("/foo")));
  }

  @Test
  public void testSetAndGetFloat() {
    ParameterServer server = new ParameterServer();
    GraphName name = new GraphName("/foo/bar");
    server.set(name, 0.42f);
    assertEquals(0.42f, server.get(name));
  }

  @Test
  public void testDeleteShallow() {
    ParameterServer server = new ParameterServer();
    GraphName name = new GraphName("/foo");
    server.set(name, "bloop");
    server.delete(name);
    assertEquals(null, server.get(name));
  }

  @Test
  public void testDeleteDeep() {
    ParameterServer server = new ParameterServer();
    GraphName name = new GraphName("/foo/bar");
    server.set(name, "bloop");
    server.delete(name);
    assertEquals(null, server.get(name));
  }

  @Test
  public void testHas() {
    ParameterServer server = new ParameterServer();
    server.set(new GraphName("/foo/bar/baz"), "bloop");
    assertTrue(server.has(new GraphName("/foo/bar/baz")));
    assertTrue(server.has(new GraphName("/foo/bar")));
    assertTrue(server.has(new GraphName("/foo")));
    assertTrue(server.has(new GraphName("/")));
  }

}
