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
package org.ros.namespace;

import junit.framework.TestCase;

import org.junit.Test;
import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;

import java.util.HashMap;

public class NameResolverTest extends TestCase {

  public NameResolver createGlobalResolver() {
    return createGlobalResolver(new HashMap<GraphName, GraphName>());
  }

  public NameResolver createGlobalResolver(HashMap<GraphName, GraphName> remappings) {
    return new NameResolver(Namespace.GLOBAL_NS, remappings);
  }

  @Test
  public void testResolveNameOneArg() {
    NameResolver r = createGlobalResolver();

    assertEquals("/foo", r.resolveName("foo"));
    assertEquals("/foo", r.resolveName("/foo"));
    assertEquals("/foo/bar", r.resolveName("foo/bar"));

    try {
      assertEquals("/node/foo", r.resolveName("~foo"));
      fail("should have thrown RosNameException");
    } catch (RosNameException e) {
    }

    r = new NameResolver("/ns1", new HashMap<GraphName, GraphName>());

    assertEquals("/ns1/foo", r.resolveName("foo"));
    assertEquals("/foo", r.resolveName("/foo"));
    assertEquals("/ns1/foo/bar", r.resolveName("foo/bar"));
  }

  @Test
  public void testResolveNameTwoArg() {
    // these tests are based on test_roslib_names.py

    NameResolver r = createGlobalResolver();
    try {
      r.resolveName("foo", "bar");
      fail("should have raised");
    } catch (IllegalArgumentException e) {
    }

    assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, ""));
    assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, Namespace.GLOBAL_NS));
    assertEquals(Namespace.GLOBAL_NS, r.resolveName("/anything/bar", Namespace.GLOBAL_NS));

    assertEquals("/ns1/node", r.resolveName("/ns1/node", ""));
    assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, ""));

    // relative namespaces get resolved to default namespace
    assertEquals("/foo", r.resolveName("/", "foo"));
    assertEquals("/foo", r.resolveName("/", "foo/"));
    assertEquals("/foo", r.resolveName("/", "/foo"));
    assertEquals("/foo", r.resolveName("/", "/foo/"));

    assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2", "foo"));
    assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2", "foo/"));
    assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2/", "foo"));
    assertEquals("/foo", r.resolveName("/ns1/ns2", "/foo/"));

    assertEquals("/ns1/ns2/ns3/foo", r.resolveName("/ns1/ns2/ns3", "foo"));
    assertEquals("/ns1/ns2/ns3/foo", r.resolveName("/ns1/ns2/ns3/", "foo"));
    assertEquals("/foo", r.resolveName("/", "/foo/"));

    assertEquals("/ns1/ns2/foo/bar", r.resolveName("/ns1/ns2", "foo/bar"));
    assertEquals("/ns1/ns2/ns3/foo/bar", r.resolveName("/ns1/ns2/ns3", "foo/bar"));

    try {
      assertEquals("/foo", r.resolveName("/", "~foo"));
      fail("resolveName() with two args should never allow private names");
    } catch (RosNameException e) {
    }
  }

  @Test
  public void testJoin() {
    assertEquals("/bar", NameResolver.join("/", "bar"));
    assertEquals("bar", NameResolver.join("", "bar"));
    assertEquals("foo/bar", NameResolver.join("foo", "bar"));
    assertEquals("/foo/bar", NameResolver.join("/foo", "bar"));
    assertEquals("/bar", NameResolver.join("/foo", "/bar"));

    assertEquals("/bar", NameResolver.join(new GraphName("/"), new GraphName("bar")));
    assertEquals("bar", NameResolver.join(new GraphName(""), new GraphName("bar")));
    assertEquals("foo/bar", NameResolver.join(new GraphName("foo"), new GraphName("bar")));
    assertEquals("/foo/bar", NameResolver.join(new GraphName("/foo"), new GraphName("bar")));
    assertEquals("/bar", NameResolver.join(new GraphName("/foo"), new GraphName("/bar")));
  }

  /**
   * Test resolveName with name remapping active.
   */
  @Test
  public void testResolveNameRemapping() {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    remappings.put(new GraphName("name"), new GraphName("/my/name"));
    remappings.put(new GraphName("foo"), new GraphName("/my/foo"));

    NameResolver r = createGlobalResolver(remappings);

    String n = r.resolveName("name");
    assertTrue(n.equals("/my/name"));
    assertTrue(r.resolveName("/name").equals("/name"));
    assertTrue(r.resolveName("foo").equals("/my/foo"));
    assertTrue(r.resolveName("/my/name").equals("/my/name"));
  }

}
