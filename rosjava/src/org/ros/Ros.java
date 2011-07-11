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

import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.internal.node.DefaultNode;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Ros {

  private static NodeFactory nodeFactory;

  private static final class DefaultNodeFactory implements NodeFactory {
    @Override
    public Node newNode(GraphName name, NodeConfiguration configuration) {
      return new DefaultNode(name, configuration);
    }
  }

  static {
    setNodeFactory(new DefaultNodeFactory());
  }

  private Ros() {
    // Utility class
  }

  public static NodeFactory getNodeFactory() {
    return nodeFactory;
  }

  public static void setNodeFactory(NodeFactory nodeFactory) {
    Ros.nodeFactory = nodeFactory;
  }

  public static Node newNode(GraphName name, NodeConfiguration configuration) {
    return nodeFactory.newNode(name, configuration);
  }

  public static Node newNode(String name, NodeConfiguration configuration) {
    return nodeFactory.newNode(new GraphName(name), configuration);
  }

  public static NameResolver newNameResolver(GraphName namespace,
      Map<GraphName, GraphName> remappings) {
    return new DefaultNameResolver(namespace, remappings);
  }

  public static NameResolver
      newNameResolver(String namespace, Map<GraphName, GraphName> remappings) {
    return newNameResolver(new GraphName(namespace), remappings);
  }

  public static NameResolver newNameResolver(GraphName namespace) {
    return newNameResolver(namespace, new HashMap<GraphName, GraphName>());
  }

  public static NameResolver newNameResolver(String namespace) {
    return newNameResolver(new GraphName(namespace));
  }

  public static NameResolver newNameResolver(Map<GraphName, GraphName> remappings) {
    return newNameResolver(GraphName.newRoot(), remappings);
  }

  public static NameResolver newNameResolver() {
    return newNameResolver(GraphName.newRoot());
  }

}
