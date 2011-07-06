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
package org.ros.internal.namespace;

import junit.framework.TestCase;
import org.junit.Test;
import org.ros.exception.RosNameException;
import org.ros.namespace.GraphName;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class GraphNameTest extends TestCase {

  @Test
  public void testToString() {
    try {
      String[] canonical = {"abc", "ab7", "/abc", "/abc/bar", "/", "~garage", "~foo/bar"};
      for (String c : canonical) {
        assertEquals(c, new DefaultGraphName(c).toString());
      }
      // test canonicalization
      assertEquals("", new DefaultGraphName("").toString());
      assertEquals("/", new DefaultGraphName("/").toString());
      assertEquals("/foo", new DefaultGraphName("/foo/").toString());
      assertEquals("foo", new DefaultGraphName("foo/").toString());
      assertEquals("foo/bar", new DefaultGraphName("foo/bar/").toString());
    } catch (IllegalArgumentException e) {
      fail("These names should be valid" + e.toString());
    }
  }

  @Test
  public void testValidNames() {
    String[] valid =
        {"", "abc", "ab7", "ab7_kdfJKSDJFGkd", "/abc", "/", "~private", "~private/something",
            "/global", "/global/", "/global/local"};
    for (String v : valid) {
      new DefaultGraphName(v);
    }
  }

  @Test
  public void testInvalidNames() {
    final String[] illegalChars = {"=", "-", "(", ")", "*", "%", "^"};
    for (String i : illegalChars) {
      try {
        new DefaultGraphName("good" + i);
        fail("bad name not caught: " + i);
      } catch (RosNameException e) {
      }
    }
    final String[] illegalNames = {"/~private", "5foo"};
    for (String i : illegalNames) {
      try {
        new DefaultGraphName(i);
        fail("bad name not caught" + i);
      } catch (RosNameException e) {
      }
    }
  }

  @Test
  public void testIsGlobal() {
    final String[] tests = {"/", "/global", "/global2"};
    for (String t : tests) {
      assertTrue(new DefaultGraphName(t).isGlobal());
    }
    final String[] fails = {"", "not_global", "not/global"};
    for (String t : fails) {
      assertFalse(new DefaultGraphName(t).isGlobal());
    }
  }

  @Test
  public void testIsPrivate() {
    String[] tests = {"~name", "~name/sub"};
    for (String t : tests) {
      assertTrue(new DefaultGraphName(t).isPrivate());
    }
    String[] fails = {"", "not_private", "not/private", "/"};
    for (String f : fails) {
      assertFalse(new DefaultGraphName(f).isPrivate());
    }
  }

  @Test
  public void testIsRelative() {
    GraphName n = new DefaultGraphName("name");
    assertTrue(n.isRelative());
    n = new DefaultGraphName("/name");
    assertFalse(n.isRelative());
  }

  @Test
  public void testGetParent() {
    GraphName global = new DefaultGraphName("/");
    GraphName empty = new DefaultGraphName("");
    // parent of empty is empty, just like dirname
    assertEquals(empty, new DefaultGraphName("").getParent());
    // parent of global is global, just like dirname
    assertEquals(global, new DefaultGraphName("/").getParent().toString());

    // test with global names
    assertEquals(new DefaultGraphName("/wg"), new DefaultGraphName("/wg/name").getParent());
    assertEquals(new DefaultGraphName("/wg"), new DefaultGraphName("/wg/name/").getParent());
    assertEquals(global, new DefaultGraphName("/wg/").getParent());
    assertEquals(global, new DefaultGraphName("/wg").getParent());

    // test with relative names
    assertEquals(new DefaultGraphName("wg"), new DefaultGraphName("wg/name").getParent());
    assertEquals(empty, new DefaultGraphName("wg/").getParent());
  }

  @Test
  public void testCanonicalizeName() {
    assertEquals("", DefaultGraphName.canonicalize(""));
    assertEquals("/", DefaultGraphName.canonicalize("/"));
    assertEquals("/", DefaultGraphName.canonicalize("//"));
    assertEquals("/", DefaultGraphName.canonicalize("///"));

    assertEquals("foo", DefaultGraphName.canonicalize("foo"));
    assertEquals("foo", DefaultGraphName.canonicalize("foo/"));
    assertEquals("foo", DefaultGraphName.canonicalize("foo//"));

    assertEquals("/foo", DefaultGraphName.canonicalize("/foo"));
    assertEquals("/foo", DefaultGraphName.canonicalize("/foo/"));
    assertEquals("/foo", DefaultGraphName.canonicalize("/foo//"));

    assertEquals("/foo/bar", DefaultGraphName.canonicalize("/foo/bar"));
    assertEquals("/foo/bar", DefaultGraphName.canonicalize("/foo/bar/"));
    assertEquals("/foo/bar", DefaultGraphName.canonicalize("/foo/bar//"));

    assertEquals("~foo", DefaultGraphName.canonicalize("~foo"));
    assertEquals("~foo", DefaultGraphName.canonicalize("~foo/"));
    assertEquals("~foo", DefaultGraphName.canonicalize("~foo//"));
    assertEquals("~foo", DefaultGraphName.canonicalize("~/foo"));
  }

  @Test
  public void testGetName() {
    GraphName name = new DefaultGraphName("");
    assertEquals("", name.getBasename().toString());
    name = new DefaultGraphName("/");
    assertEquals("", name.getBasename().toString());
    name = new DefaultGraphName("/foo");
    assertEquals("foo", name.getBasename().toString());
    name = new DefaultGraphName("foo");
    assertEquals("foo", name.getBasename().toString());
    name = new DefaultGraphName("foo/");
    // The trailing slash is removed when creating a GraphName.
    assertEquals("foo", name.getBasename().toString());
    name = new DefaultGraphName("/foo/bar");
    assertEquals("bar", name.getBasename().toString());
    name = new DefaultGraphName("foo/bar");
    assertEquals("bar", name.getBasename().toString());
  }

  @Test
  public void testJoin() {
    assertEquals(new DefaultGraphName("/bar"), new DefaultGraphName("/").join(new DefaultGraphName("bar")));
    assertEquals(new DefaultGraphName("bar"), new DefaultGraphName("").join(new DefaultGraphName("bar")));
    assertEquals(new DefaultGraphName("foo/bar"), new DefaultGraphName("foo").join(new DefaultGraphName("bar")));
    assertEquals(new DefaultGraphName("/foo/bar"), new DefaultGraphName("/foo").join(new DefaultGraphName("bar")));
    assertEquals(new DefaultGraphName("/bar"), new DefaultGraphName("/foo").join(new DefaultGraphName("/bar")));
  }

}
