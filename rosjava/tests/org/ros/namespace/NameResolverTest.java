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
import org.ros.exception.RosNameException;
import org.ros.internal.namespace.GraphName;

import java.util.HashMap;

public class NameResolverTest extends TestCase {

  @Test
  public void testResolveNameOneArg() {
    NameResolver r = NameResolver.createDefault();

    assertEquals("/foo", r.resolve("foo"));
    assertEquals("/foo", r.resolve("/foo"));
    assertEquals("/foo/bar", r.resolve("foo/bar"));

    try {
      assertEquals("/node/foo", r.resolve("~foo"));
      fail("should have thrown RosNameException");
    } catch (RosNameException e) {
    }

    r = NameResolver.createFromString("/ns1");

    assertEquals("/ns1/foo", r.resolve("foo"));
    assertEquals("/foo", r.resolve("/foo"));
    assertEquals("/ns1/foo/bar", r.resolve("foo/bar"));
  }

  @Test
  public void testResolveNameTwoArg() {
    // These tests are based on test_roslib_names.py.
    NameResolver r = NameResolver.createDefault();
    try {
      r.resolve("foo", "bar");
      fail("should have raised");
    } catch (IllegalArgumentException e) {
    }

    assertEquals(Namespace.GLOBAL, r.resolve(Namespace.GLOBAL, ""));
    assertEquals(Namespace.GLOBAL, r.resolve(Namespace.GLOBAL, Namespace.GLOBAL));
    assertEquals(Namespace.GLOBAL, r.resolve("/anything/bar", Namespace.GLOBAL));

    assertEquals("/ns1/node", r.resolve("/ns1/node", ""));
    assertEquals(Namespace.GLOBAL, r.resolve(Namespace.GLOBAL, ""));

    // relative namespaces get resolved to default namespace
    assertEquals("/foo", r.resolve("/", "foo"));
    assertEquals("/foo", r.resolve("/", "foo/"));
    assertEquals("/foo", r.resolve("/", "/foo"));
    assertEquals("/foo", r.resolve("/", "/foo/"));

    assertEquals("/ns1/ns2/foo", r.resolve("/ns1/ns2", "foo"));
    assertEquals("/ns1/ns2/foo", r.resolve("/ns1/ns2", "foo/"));
    assertEquals("/ns1/ns2/foo", r.resolve("/ns1/ns2/", "foo"));
    assertEquals("/foo", r.resolve("/ns1/ns2", "/foo/"));

    assertEquals("/ns1/ns2/ns3/foo", r.resolve("/ns1/ns2/ns3", "foo"));
    assertEquals("/ns1/ns2/ns3/foo", r.resolve("/ns1/ns2/ns3/", "foo"));
    assertEquals("/foo", r.resolve("/", "/foo/"));

    assertEquals("/ns1/ns2/foo/bar", r.resolve("/ns1/ns2", "foo/bar"));
    assertEquals("/ns1/ns2/ns3/foo/bar", r.resolve("/ns1/ns2/ns3", "foo/bar"));

    try {
      assertEquals("/foo", r.resolve("/", "~foo"));
      fail("resolveName() with two args should never allow private names");
    } catch (RosNameException e) {
    }
  }

  /**
   * Test resolveName with name remapping active.
   */
  @Test
  public void testResolveNameRemapping() {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    remappings.put(new GraphName("name"), new GraphName("/my/name"));
    remappings.put(new GraphName("foo"), new GraphName("/my/foo"));

    NameResolver r = NameResolver.createDefault(remappings);

    String n = r.resolve("name");
    assertTrue(n.equals("/my/name"));
    assertTrue(r.resolve("/name").equals("/name"));
    assertTrue(r.resolve("foo").equals("/my/foo"));
    assertTrue(r.resolve("/my/name").equals("/my/name"));
  }

}
