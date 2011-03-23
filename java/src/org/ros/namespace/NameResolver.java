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

import com.google.common.base.Preconditions;

import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;

import java.util.HashMap;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NameResolver {

  private final String namespace;
  private HashMap<GraphName, GraphName> remappings;

  public NameResolver(String namespace, HashMap<GraphName, GraphName> remappings)
      throws RosNameException {
    this.remappings = remappings;
    this.namespace = GraphName.canonicalizeName(namespace);
  }

  public String getNamespace() {
    return namespace;
  }

  /**
   * Resolve name relative to namespace. If namespace is not global, it will
   * first be resolved to a global name. This method will not resolve private
   * ~names.
   * 
   * This does all remappings of both the namespace and name.
   * 
   * @param namespace
   * @param name
   * @return the fully resolved name relative to the given namespace.
   * @throws RosNameException Will throw on a poorly formated name.
   */
  public String resolveName(String namespace, String name) throws RosNameException {
    GraphName ns = lookUpRemapping(new GraphName(namespace));
    Preconditions.checkArgument(ns.isGlobal(), "namespace must be global: " + ns.toString());
    GraphName n = lookUpRemapping(new GraphName(name));
    if (n.isGlobal()) {
      return n.toString();
    }
    if (n.isRelative()) {
      return join(ns, n);
    } else if (n.isPrivate()) {
      throw new RosNameException("cannot resolve ~private names in arbitrary namespaces");
    } else {
      throw new RosNameException("Bad name: " + name);
    }
  }

  /**
   * Join two names together.
   * 
   * @param name1 ROS name to join to.
   * @param name2 ROS name to join. Must be relative.
   * @return A concatenation of the two names
   * @throws RosNameException If name1 or name2 is an illegal name
   * @throws IllegalArgumentException If name2 is not a relative name
   */
  public static String join(String name1, String name2) throws RosNameException {
    return join(new GraphName(name1), new GraphName(name2));
  }

  /**
   * Join two names together.
   * 
   * @param name1 ROS name to join to.
   * @param name2 ROS name to join. If name2 is global, this will return name2.
   * @return A concatenation of the two names.
   * @throws RosNameException If name1 or name2 is an illegal name
   */
  public static String join(GraphName name1, GraphName name2) throws RosNameException {
    if (name2.isGlobal() || name1.toString().equals("")) {
      return name2.toString();
    } else if (name1.equals(Namespace.GLOBAL_NS)) {
      return Namespace.GLOBAL_NS + name2.toString();
    } else {
      return new GraphName(name1.toString() + "/" + name2.toString()).toString();
    }
  }

  /**
   * Convenience function for looking up a remapping.
   * 
   * @param name The name to lookup.
   * @return The name if it is not remapped, otherwise the remapped name.
   */
  protected GraphName lookUpRemapping(GraphName name) {
    GraphName rmname = name;
    if (remappings.containsKey(name)) {
      rmname = remappings.get(name);
    }
    return rmname;
  }

  public HashMap<GraphName, GraphName> getRemappings() {
    return remappings;
  }

  /**
   * @param name Name to resolve
   * @return The name resolved relative to the default namespace.
   * @throws RosNameException
   */
  public String resolveName(String name) throws RosNameException {
    return resolveName(getNamespace(), name);
  }

  /**
   * Construct a new {@link NameResolver} with a copy of this resolver's
   * remappings. The namespace of the new resolver will be the value of the name
   * parameter resolved in this namespace.
   * 
   * @param name
   * @return {@link NameResolver} relative to the current namespace.
   * @throws RosNameException
   */
  @SuppressWarnings("unchecked")
  public NameResolver createResolver(String name) throws RosNameException {
    String resolverNamespace = resolveName(name);
    return new NameResolver(resolverNamespace, (HashMap<GraphName, GraphName>) remappings.clone());
  }
}
