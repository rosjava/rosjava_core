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

/**
 * Resolver for {@link Node} names. Node namespace must handle the ~name syntax
 * for private names.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeNameResolver extends NameResolver {

  private final String privateNamespace;

  /**
   * @param remappings
   * @throws RosNameException
   * 
   */
  private NodeNameResolver(String defaultNamespace, String privateNamespace,
      HashMap<GraphName, GraphName> remappings) throws RosNameException {
    super(defaultNamespace, remappings);
    this.privateNamespace = privateNamespace;
  }

  /**
   * @param name
   *          Name to resolve
   * @return The name resolved relative to the default namespace.
   * @throws RosNameException
   */
  @Override
  public String resolveName(String name) throws RosNameException {
    GraphName n = lookUpRemapping(new GraphName(name));
    if (n.isPrivate()) {
      String s = n.toRelative();
      // allow ~/foo
      if (s.startsWith("/")) {
        s = s.substring(1);
      }
      return resolveName(privateNamespace, s);
    } else {
      return resolveName(getNamespace(), name);
    }

  }

  public static NodeNameResolver create(NameResolver defaultResolver, GraphName nodeName)
      throws RosNameException {
    return new NodeNameResolver(defaultResolver.getNamespace(), nodeName.toString(),
        defaultResolver.getRemappings());
  }
}
