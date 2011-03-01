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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.exceptions.RosNameException;

public class NameTest extends TestCase {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testToString() {
    try {
      String[] canonical = { "abc", "ab7", "/abc", "/abc/bar", "/", "~garage", "~foo/bar" };
      for (String c : canonical) {
        assertEquals(c, new RosName(c).toString());
      }
      // test canonicalization
      System.out.println(new RosName("/foo/").toString());
      assertEquals("/foo", new RosName("/foo/").toString());
      assertEquals("foo", new RosName("foo/").toString());
      assertEquals("foo/bar", new RosName("foo/bar/").toString());
    } catch (IllegalArgumentException e) {
      fail("These names should be valid" + e.toString());
    } catch (RosNameException e) {
      fail("These names should be valid" + e.toString());
    }
  }

  @Test
  public void testValidNames() {

    String[] valid = { "", "abc", "ab7", "ab7_kdfJKSDJFGkd", "/abc", "/", "~private",
        "~private/something", "/global", "/global/", "/global/local" };
    try {
      for (String v : valid) {
        new RosName(v);
      }
    } catch (RosNameException e) {
      fail("These names should be valid" + e.toString());
    }
  }

  @Test
  public void testInvalidNames() {
    final String[] illegalChars = { "=", "-", "(", ")", "*", "%", "^" };
    for (String i : illegalChars) {
      try {
        new RosName("good" + i);
        fail("bad name not caught: " + i);
      } catch (RosNameException e) {
      }
    }
    final String[] illegalNames = { "/~private", "5foo" };
    for (String i : illegalNames) {
      try {
        new RosName(i);
        fail("bad name not caught" + i);
      } catch (RosNameException e) {
      }
    }
  }

  @Test
  public void testIsGlobal() throws RosNameException {
    final String[] tests = { "/", "/global", "/global2" };
    for (String t : tests) {
      assertTrue(new RosName(t).isGlobal());
    }
    final String[] fails = { "", "not_global", "not/global" };
    for (String t : fails) {
      assertFalse(new RosName(t).isGlobal());
    }
  }

  @Test
  public void testIsPrivate() throws RosNameException {
    String[] tests = { "~name", "~name/sub" };
    for (String t : tests) {
      assertTrue(new RosName(t).isPrivate());
    }
    String[] fails = { "", "not_private", "not/private", "/" };
    for (String f : fails) {
      assertFalse(new RosName(f).isPrivate());
    }
  }

  @Test
  public void testIsRelative() throws RosNameException {
    RosName n = new RosName("name");
    assertTrue(n.isRelative());
    n = new RosName("/name");
    assertFalse(n.isRelative());
  }

  @Test
  public void testGetParent() throws RosNameException {
    // parent of empty is empty, just like dirname
    assertEquals("", new RosName("").getParent());
    // parent of global is global, just like dirname
    assertEquals("/", new RosName("/").getParent());

    // test with global names
    assertEquals("/wg", new RosName("/wg/name").getParent());
    assertEquals("/wg", new RosName("/wg/name/").getParent());
    System.out.println(new RosName("/wg/").getParent());
    System.out.println(new RosName("/wg/").isGlobal());
    System.out.println(new RosName("/wg/").toString());
    assertEquals("/", new RosName("/wg/").getParent());
    assertEquals("/", new RosName("/wg").getParent());

    // test with relative names
    System.out.println(new RosName("wg/name").getParent());
    assertEquals("wg", new RosName("wg/name").getParent());
    System.out.println(new RosName("wg/").getParent());
    assertEquals("", new RosName("wg/").getParent());
  }

}
