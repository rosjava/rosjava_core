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

import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;

import java.util.HashMap;

public class NodeNameResolverTest extends NameResolverTest {

  @Override
  public void testResolveNameOneArg() throws RosNameException {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    GraphName nodeName = new GraphName("/node");
    NodeNameResolver r = NodeNameResolver.create(new NameResolver(Namespace.GLOBAL_NS, remappings),
        nodeName);

    assertEquals("/foo", r.resolveName("foo"));
    assertEquals("/foo", r.resolveName("/foo"));
    assertEquals("/foo/bar", r.resolveName("foo/bar"));

    assertEquals("/node/foo", r.resolveName("~foo"));
    assertEquals("/node/foo/bar", r.resolveName("~foo/bar"));
    // https://code.ros.org/trac/ros/ticket/3044
    assertEquals("/node/foo", r.resolveName("~/foo"));

    nodeName = new GraphName("/ns1/node");
    r = NodeNameResolver.create(new NameResolver(Namespace.GLOBAL_NS, remappings), nodeName);
    assertEquals("/ns1/node/foo", r.resolveName("~foo"));
    assertEquals("/ns1/node/foo", r.resolveName("~/foo"));
    assertEquals("/ns1/node/foo/bar", r.resolveName("~/foo/bar"));

    // Test case where private name is not is same namespace as default
    nodeName = new GraphName("/ns2/node");
    r = NodeNameResolver.create(new NameResolver("/ns1", remappings), nodeName);

    assertEquals("/ns1/foo", r.resolveName("foo"));
    assertEquals("/foo", r.resolveName("/foo"));
    assertEquals("/ns1/foo/bar", r.resolveName("foo/bar"));

    assertEquals("/ns2/node/foo", r.resolveName("~foo"));
    assertEquals("/ns2/node/foo/bar", r.resolveName("~foo/bar"));
    // https://code.ros.org/trac/ros/ticket/3044
    assertEquals("/ns2/node/foo", r.resolveName("~/foo"));
  }

  @Override
  public NameResolver createGlobalResolver() throws RosNameException {
    return createGlobalResolver(new HashMap<GraphName, GraphName>());
  }

  @Override
  public NameResolver createGlobalResolver(HashMap<GraphName, GraphName> remappings)
      throws RosNameException {
    GraphName nodeName = new GraphName("/node");
    NodeNameResolver r = NodeNameResolver.create(new NameResolver(Namespace.GLOBAL_NS, remappings),
        nodeName);
    return r;
  }

}
