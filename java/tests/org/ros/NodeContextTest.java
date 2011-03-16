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
package org.ros;

import static org.junit.Assert.*;

import org.ros.exceptions.RosNameException;

import java.util.HashMap;

import org.ros.internal.namespace.GraphName;

import org.ros.namespace.NameResolver;

import org.junit.Test;

public class NodeContextTest {

  @Test
  public void testNodeContext() {
    NodeContext nodeContext = new NodeContext();
    assertEquals(null, nodeContext.getResolver());
  }

  @Test
  public void testResolver() throws RosNameException {
    NodeContext nodeContext = new NodeContext();
    assertEquals(null, nodeContext.getResolver());

    NameResolver resolver = new NameResolver("/test", new HashMap<GraphName, GraphName>());
    nodeContext.setResolver(resolver);
    assertEquals(resolver, nodeContext.getResolver());
  }

}
