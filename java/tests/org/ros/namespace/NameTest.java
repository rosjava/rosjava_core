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
  public void testValidNames() throws RosNameException {
    try {
      RosName n = new RosName("abc");
      n = new RosName("ab7");
      n = new RosName("ab7_kdfJKSDJFGkd");
      n = new RosName("/abc");
      n = new RosName("~garge");
      n = new RosName("~kjdfkj/dfa");
    } catch (IllegalArgumentException e) {
      fail("These names should be valid" + e.toString());
    }
  }

  @Test
  public void testInvalidNames() throws RosNameException {
    try {
      RosName n = new RosName("d23-+_)(abc");
      fail("bad name not caught");
    } catch (IllegalArgumentException e) {

    }
    try {
      RosName n = new RosName("");
      fail("bad name not caught");
    } catch (IllegalArgumentException e) {

    }
    try {
      RosName n = new RosName("/");
      fail("bad name not caught");
    } catch (IllegalArgumentException e) {

    }
    try {
      RosName n = new RosName("5/");
      fail("bad name not caught");
    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testIsGlobal() throws RosNameException {
    RosName n = new RosName("/gobal/name");
    assertTrue(n.isGlobal());
    n = new RosName("notgobal/name");
    assertFalse(n.isGlobal());
  }

  @Test
  public void testIsPrivate() throws RosNameException {
    RosName n = new RosName("~name");
    assertTrue(n.isPrivate());
    n = new RosName("name/m");
    assertFalse(n.isPrivate());
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
    RosName n = new RosName("/wg/name");
    assertTrue(n.getParent().equals("/wg"));
    n = new RosName("/wg");
    assertTrue(n.getParent().equals(""));
  }

}
