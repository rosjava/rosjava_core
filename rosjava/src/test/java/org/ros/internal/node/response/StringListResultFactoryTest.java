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

package org.ros.internal.node.response;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class StringListResultFactoryTest extends TestCase {

  @Test
  public void testEncodeAndDecode() {
    StringListResultFactory factory = new StringListResultFactory();
    List<String> expected;
    List<String> value;

    expected = Lists.newArrayList();
    value = factory.create(new Object[] {});
    assertEquals(expected, value);
    value = factory.create(new String[] {});
    assertEquals(expected, value);

    expected = Lists.newArrayList(new String[] { "foo" });
    value = factory.create(new Object[] { "foo" });
    assertEquals(expected, value);
    value = factory.create(new String[] { "foo" });
    assertEquals(expected, value);

    expected = Lists.newArrayList(new String[] { "foo", "bar" });
    value = factory.create(new Object[] { "foo", "bar" });
    assertEquals(expected, value);
    value = factory.create(new String[] { "foo", "bar" });
    assertEquals(expected, value);

    try {
      factory.create("bad");
      fail("should not have converted");
    } catch (ClassCastException e) {
    }
    try {
      factory.create(new Object[] { 1 });
      fail("should not have converted");
    } catch (ClassCastException e) {
    }
    try {
      factory.create(new Object[] { "1", 1 });
      fail("should not have converted");
    } catch (ClassCastException e) {
    }

  }

}
