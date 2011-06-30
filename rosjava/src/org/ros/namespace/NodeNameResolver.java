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

import org.ros.Node;
import org.ros.internal.namespace.GraphName;

import java.util.Map;

/**
 * Resolver for {@link Node} names. Node namespace must handle the ~name syntax
 * for private names.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeNameResolver extends NameResolver {

  private final String privateNamespace;

  public static NodeNameResolver create(NameResolver defaultResolver, GraphName nodeName) {
    return new NodeNameResolver(defaultResolver.getNamespace().toString(), nodeName.toString(),
        defaultResolver.getRemappings());
  }

  private NodeNameResolver(String defaultNamespace, String privateNamespace,
      Map<GraphName, GraphName> remappings) {
    super(new GraphName(defaultNamespace), remappings);
    this.privateNamespace = privateNamespace;
  }

  /**
   * @param name
   *          name to resolve
   * @return the name resolved relative to the default or private namespace
   */
  @Override
  public String resolve(String name) {
    GraphName graphName = lookUpRemapping(new GraphName(name));
    if (graphName.isPrivate()) {
      return resolve(privateNamespace, graphName.toRelative().toString());
    }
    return super.resolve(name);
  }

}
