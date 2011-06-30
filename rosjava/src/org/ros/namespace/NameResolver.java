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

import org.ros.exception.RosNameException;
import org.ros.internal.namespace.GraphName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NameResolver {

  private final GraphName namespace;
  private final Map<GraphName, GraphName> remappings;

  public static NameResolver
      createFromString(String namespace, Map<GraphName, GraphName> remappings) {
    return new NameResolver(new GraphName(namespace), remappings);
  }

  public static NameResolver createFromString(String namespace) {
    return NameResolver.createFromString(namespace, new HashMap<GraphName, GraphName>());
  }

  public static NameResolver createDefault(Map<GraphName, GraphName> remappings) {
    return new NameResolver(GraphName.createRoot(), remappings);
  }

  public static NameResolver createDefault() {
    return NameResolver.createDefault(new HashMap<GraphName, GraphName>());
  }

  public NameResolver(GraphName namespace, Map<GraphName, GraphName> remappings) {
    this.remappings = Collections.unmodifiableMap(remappings);
    this.namespace = namespace;
  }

  public GraphName getNamespace() {
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
   */
  public String resolve(String namespace, String name) {
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
   * @param name1
   *          ROS name to join to.
   * @param name2
   *          ROS name to join. Must be relative.
   * @return A concatenation of the two names
   * @throws IllegalArgumentException
   *           If name2 is not a relative name
   */
  public static String join(String name1, String name2) {
    return join(new GraphName(name1), new GraphName(name2));
  }

  /**
   * Join two names together.
   * 
   * @param name1
   *          ROS name to join to.
   * @param name2
   *          ROS name to join. If name2 is global, this will return name2.
   * @return A concatenation of the two names.
   */
  public static String join(GraphName name1, GraphName name2) {
    return name1.join(name2).toString();
  }

  /**
   * Convenience function for looking up a remapping.
   * 
   * @param name
   *          The name to lookup.
   * @return The name if it is not remapped, otherwise the remapped name.
   */
  protected GraphName lookUpRemapping(GraphName name) {
    GraphName rmname = name;
    if (remappings.containsKey(name)) {
      rmname = remappings.get(name);
    }
    return rmname;
  }

  public Map<GraphName, GraphName> getRemappings() {
    return remappings;
  }

  /**
   * @param name
   *          name to resolve
   * @return the name resolved relative to the default namespace
   */
  public String resolve(String name) {
    return resolve(getNamespace().toString(), name);
  }

  /**
   * Construct a new {@link NameResolver} with the same remappings as this
   * resolver has. The namespace of the new resolver will be the value of the
   * name parameter resolved in this namespace.
   * 
   * @param name
   * @return {@link NameResolver} relative to the current namespace.
   */
  public NameResolver createResolver(String name) {
    String resolverNamespace = resolve(name);
    return new NameResolver(new GraphName(resolverNamespace), remappings);
  }
}
