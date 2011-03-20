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

import org.ros.internal.namespace.GraphName;

import java.util.HashMap;

import org.ros.exceptions.RosNameException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NameResolverTest extends TestCase {

  private HashMap<GraphName, GraphName> emptyRemappings;

  @Override
  @Before
  public void setUp() throws Exception {
    emptyRemappings = new HashMap<GraphName, GraphName>();
  }

  @Override
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testResolveName() throws RosNameException {
    // these tests are based on test_roslib_names.py
    NameResolver r = new NameResolver(Namespace.GLOBAL_NS, emptyRemappings);
    try {
      r.resolveName("foo", "bar");
      fail("should have raised");
    } catch (IllegalArgumentException e) {
    } catch (RosNameException e) {
      fail("should have not raised");
    }
    try {
      assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, ""));
      assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, Namespace.GLOBAL_NS));
      assertEquals(Namespace.GLOBAL_NS, r.resolveName("/anything/bar", Namespace.GLOBAL_NS));
      
      assertEquals(Namespace.GLOBAL_NS, r.resolveName("/node", ""));
      System.out.println(r.resolveName("/ns1/node", ""));
      assertEquals("/ns1", r.resolveName("/ns1/node", ""));
      assertEquals(Namespace.GLOBAL_NS, r.resolveName(Namespace.GLOBAL_NS, ""));

      // relative namespaces get resolved to default namespace
      assertEquals("/foo", r.resolveName("/", "foo"));
      assertEquals("/foo", r.resolveName("/", "foo/"));
      assertEquals("/foo", r.resolveName("/", "/foo"));
      assertEquals("/foo", r.resolveName("/", "/foo/"));

      assertEquals("/ns1/foo", r.resolveName("/ns1/ns2", "foo"));
      assertEquals("/ns1/foo", r.resolveName("/ns1/ns2", "foo/"));
      assertEquals("/ns1/foo", r.resolveName("/ns1/ns2/", "foo"));
      assertEquals("/foo", r.resolveName("/ns1/ns2", "/foo/"));

      assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2/ns3", "foo"));
      assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2/ns3/", "foo"));
      assertEquals("/foo", r.resolveName("/", "/foo/"));

      assertEquals("/ns1/foo/bar", r.resolveName("/ns1/ns2", "foo/bar"));
      assertEquals("/ns1/ns2/foo/bar", r.resolveName("/ns1/ns2/ns3", "foo/bar"));

      assertEquals("/foo", r.resolveName("/", "~foo"));
      assertEquals("/node/foo", r.resolveName("/node", "~foo"));
      assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2", "~foo"));
      assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2", "~foo/"));
      assertEquals("/ns1/ns2/foo/bar", r.resolveName("/ns1/ns2", "~foo/bar"));

      // https://code.ros.org/trac/ros/ticket/3044
      assertEquals("/foo", r.resolveName("/", "~/foo"));
      assertEquals("/node/foo", r.resolveName("/node", "~/foo"));
      assertEquals("/ns1/ns2/foo", r.resolveName("/ns1/ns2", "~/foo"));
      assertEquals("/ns1/ns2/foo/bar", r.resolveName("/ns1/ns2", "~/foo/bar"));
    } catch (RosNameException e) {
      fail("should not be any invalid names in this test");
    }
    /*
     * //ns join tests # private and global names cannot be joined
     * self.assertEquals('~name', ns_join('/foo', '~name'))
     * self.assertEquals('/name', ns_join('/foo', '/name'))
     * self.assertEquals('~name', ns_join('~', '~name'))
     * self.assertEquals('/name', ns_join('/', '/name'))
     * 
     * # ns can be '~' or '/' self.assertEquals('~name', ns_join('~', 'name'))
     * self.assertEquals('/name', ns_join('/', 'name'))
     * 
     * self.assertEquals('/ns/name', ns_join('/ns', 'name'))
     * self.assertEquals('/ns/name', ns_join('/ns/', 'name'))
     * self.assertEquals('/ns/ns2/name', ns_join('/ns', 'ns2/name'))
     * self.assertEquals('/ns/ns2/name', ns_join('/ns/', 'ns2/name'))
     * 
     * # allow ns to be empty self.assertEquals('name', ns_join('', 'name'))
     */
  }

  /**
   * Test resolveName with name remapping active.
   * 
   * @throws RosNameException
   */
  @Test
  public void testResolveNameRemapping() throws RosNameException {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    remappings.put(new GraphName("name"), new GraphName("/my/name"));
    remappings.put(new GraphName("foo"), new GraphName("/my/foo"));

    NameResolver r = new NameResolver(Namespace.GLOBAL_NS, remappings);

    String n = r.resolveName("name");
    System.out.print(n);
    assertTrue(n.equals("/my/name"));
    assertTrue(r.resolveName("/name").equals("/name"));
    assertTrue(r.resolveName("foo").equals("/my/foo"));
    assertTrue(r.resolveName("/my/name").equals("/my/name"));

    // TODO: not enough tests here.
  }

}
