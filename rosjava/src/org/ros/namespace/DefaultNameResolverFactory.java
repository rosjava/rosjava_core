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

import org.ros.internal.namespace.DefaultNameResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNameResolverFactory implements NameResolverFactory {

  @Override
  public NameResolver newNameResolver(GraphName namespace, Map<GraphName, GraphName> remappings) {
    return new DefaultNameResolver(namespace, remappings);
  }

  @Override
  public NameResolver newNameResolver(String namespace, Map<GraphName, GraphName> remappings) {
    return newNameResolver(new GraphName(namespace), remappings);
  }

  @Override
  public NameResolver newNameResolver(GraphName namespace) {
    return newNameResolver(namespace, new HashMap<GraphName, GraphName>());
  }

  @Override
  public NameResolver newNameResolver(String namespace) {
    return newNameResolver(new GraphName(namespace));
  }

  @Override
  public NameResolver newNameResolver(Map<GraphName, GraphName> remappings) {
    return newNameResolver(GraphName.newRoot(), remappings);
  }

  @Override
  public NameResolver newNameResolver() {
    return newNameResolver(GraphName.newRoot());
  }

}
