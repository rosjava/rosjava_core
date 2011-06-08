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

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class GraphNameTest extends TestCase {

  @Test
  public void testToString() {
    try {
      String[] canonical = {"abc", "ab7", "/abc", "/abc/bar", "/", "~garage", "~foo/bar"};
      for (String c : canonical) {
        assertEquals(c, new GraphName(c).toString());
      }
      // test canonicalization
      assertEquals("", new GraphName("").toString());
      assertEquals("/", new GraphName("/").toString());
      assertEquals("/foo", new GraphName("/foo/").toString());
      assertEquals("foo", new GraphName("foo/").toString());
      assertEquals("foo/bar", new GraphName("foo/bar/").toString());
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
      new GraphName(v);
    }
  }

  @Test
  public void testInvalidNames() {
    final String[] illegalChars = {"=", "-", "(", ")", "*", "%", "^"};
    for (String i : illegalChars) {
      try {
        new GraphName("good" + i);
        fail("bad name not caught: " + i);
      } catch (RosNameException e) {
      }
    }
    final String[] illegalNames = {"/~private", "5foo"};
    for (String i : illegalNames) {
      try {
        new GraphName(i);
        fail("bad name not caught" + i);
      } catch (RosNameException e) {
      }
    }
  }

  @Test
  public void testIsGlobal() {
    final String[] tests = {"/", "/global", "/global2"};
    for (String t : tests) {
      assertTrue(new GraphName(t).isGlobal());
    }
    final String[] fails = {"", "not_global", "not/global"};
    for (String t : fails) {
      assertFalse(new GraphName(t).isGlobal());
    }
  }

  @Test
  public void testIsPrivate() {
    String[] tests = {"~name", "~name/sub"};
    for (String t : tests) {
      assertTrue(new GraphName(t).isPrivate());
    }
    String[] fails = {"", "not_private", "not/private", "/"};
    for (String f : fails) {
      assertFalse(new GraphName(f).isPrivate());
    }
  }

  @Test
  public void testIsRelative() {
    GraphName n = new GraphName("name");
    assertTrue(n.isRelative());
    n = new GraphName("/name");
    assertFalse(n.isRelative());
  }

  @Test
  public void testGetParent() {
    GraphName global = new GraphName("/");
    GraphName empty = new GraphName("");
    // parent of empty is empty, just like dirname
    assertEquals(empty, new GraphName("").getParent());
    // parent of global is global, just like dirname
    assertEquals(global, new GraphName("/").getParent().toString());

    // test with global names
    assertEquals(new GraphName("/wg"), new GraphName("/wg/name").getParent());
    assertEquals(new GraphName("/wg"), new GraphName("/wg/name/").getParent());
    assertEquals(global, new GraphName("/wg/").getParent());
    assertEquals(global, new GraphName("/wg").getParent());

    // test with relative names
    assertEquals(new GraphName("wg"), new GraphName("wg/name").getParent());
    assertEquals(empty, new GraphName("wg/").getParent());
  }

  @Test
  public void testCanonicalizeName() {
    assertEquals("", GraphName.canonicalizeName(""));
    assertEquals("/", GraphName.canonicalizeName("/"));
    assertEquals("/", GraphName.canonicalizeName("//"));
    assertEquals("/", GraphName.canonicalizeName("///"));

    assertEquals("foo", GraphName.canonicalizeName("foo"));
    assertEquals("foo", GraphName.canonicalizeName("foo/"));
    assertEquals("foo", GraphName.canonicalizeName("foo//"));

    assertEquals("/foo", GraphName.canonicalizeName("/foo"));
    assertEquals("/foo", GraphName.canonicalizeName("/foo/"));
    assertEquals("/foo", GraphName.canonicalizeName("/foo//"));

    assertEquals("/foo/bar", GraphName.canonicalizeName("/foo/bar"));
    assertEquals("/foo/bar", GraphName.canonicalizeName("/foo/bar/"));
    assertEquals("/foo/bar", GraphName.canonicalizeName("/foo/bar//"));

    assertEquals("~foo", GraphName.canonicalizeName("~foo"));
    assertEquals("~foo", GraphName.canonicalizeName("~foo/"));
    assertEquals("~foo", GraphName.canonicalizeName("~foo//"));
    assertEquals("~foo", GraphName.canonicalizeName("~/foo"));
  }

}
