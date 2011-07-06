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

import org.ros.Ros;
import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.internal.namespace.NodeNameResolver;

import java.util.HashMap;
import java.util.Map;

public class NodeNameResolverTest extends NameResolverTest {

  @Override
  public void testResolveNameOneArg() {
    Map<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    GraphName nodeName = Ros.createGraphName("/node");
    NodeNameResolver r = NodeNameResolver.create(DefaultNameResolver.createDefault(remappings), nodeName);

    assertEquals("/foo", r.resolve("foo"));
    assertEquals("/foo", r.resolve("/foo"));
    assertEquals("/foo/bar", r.resolve("foo/bar"));

    assertEquals("/node/foo", r.resolve("~foo"));
    assertEquals("/node/foo/bar", r.resolve("~foo/bar"));
    // https://code.ros.org/trac/ros/ticket/3044
    assertEquals("/node/foo", r.resolve("~/foo"));

    nodeName = Ros.createGraphName("/ns1/node");
    r = NodeNameResolver.create(DefaultNameResolver.createDefault(remappings), nodeName);
    assertEquals("/ns1/node/foo", r.resolve("~foo"));
    assertEquals("/ns1/node/foo", r.resolve("~/foo"));
    assertEquals("/ns1/node/foo/bar", r.resolve("~/foo/bar"));

    // Test case where private name is not is same namespace as default
    nodeName = Ros.createGraphName("/ns2/node");
    r = NodeNameResolver.create(DefaultNameResolver.createFromString("/ns1", remappings), nodeName);

    assertEquals("/ns1/foo", r.resolve("foo"));
    assertEquals("/foo", r.resolve("/foo"));
    assertEquals("/ns1/foo/bar", r.resolve("foo/bar"));

    assertEquals("/ns2/node/foo", r.resolve("~foo"));
    assertEquals("/ns2/node/foo/bar", r.resolve("~foo/bar"));
    // https://code.ros.org/trac/ros/ticket/3044
    assertEquals("/ns2/node/foo", r.resolve("~/foo"));
  }

}
