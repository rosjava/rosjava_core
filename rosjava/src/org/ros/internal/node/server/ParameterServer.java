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

package org.ros.internal.node.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.ros.internal.namespace.GraphName;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterServer {

  private final Map<String, Object> tree;

  public ParameterServer() {
    tree = Maps.newHashMap();
  }

  private Stack<String> getGraphNameParts(GraphName name) {
    Stack<String> parts = new Stack<String>();
    GraphName tip = name;
    while (!tip.isRoot()) {
      parts.add(tip.getName().toString());
      tip = tip.getParent();
    }
    return parts;
  }

  @SuppressWarnings("unchecked")
  public Object get(GraphName name) {
    Preconditions.checkArgument(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Object possibleSubtree = tree;
    while (!parts.empty() && possibleSubtree != null) {
      if (!(possibleSubtree instanceof Map)) {
        return null;
      }
      possibleSubtree = ((Map<String, Object>) possibleSubtree).get(parts.pop());
    }
    return possibleSubtree;
  }

  @SuppressWarnings("unchecked")
  public void set(GraphName name, Object value) {
    Preconditions.checkArgument(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Map<String, Object> subtree = tree;
    while (!parts.empty()) {
      String part = parts.pop();
      if (parts.empty()) {
        subtree.put(part, value);
        return;
      } else if (subtree.containsKey(part) && subtree.get(part) instanceof Map) {
        subtree = (Map<String, Object>) subtree.get(part);
      } else {
        Map<String, Object> newSubtree = Maps.newHashMap();
        subtree.put(part, newSubtree);
        subtree = newSubtree;
      }
    }
  }

  public void delete(GraphName name) {
    throw new UnsupportedOperationException();
  }

  public Object search(GraphName name) {
    throw new UnsupportedOperationException();
  }

  public boolean has(GraphName name) {
    Preconditions.checkArgument(name.isGlobal());
    throw new UnsupportedOperationException();
  }

  public Collection<GraphName> getNames() {
    throw new UnsupportedOperationException();
  }

}
