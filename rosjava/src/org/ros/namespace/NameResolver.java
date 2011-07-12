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

import org.ros.exception.RosRuntimeException;

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

  public static NameResolver create(String namespace, Map<GraphName, GraphName> remappings) {
    return new NameResolver(new GraphName(namespace), remappings);
  }

  public static NameResolver create(GraphName namespace) {
    return new NameResolver(namespace, new HashMap<GraphName, GraphName>());
  }

  public static NameResolver create(String namespace) {
    return create(new GraphName(namespace));
  }

  public static NameResolver create(Map<GraphName, GraphName> remappings) {
    return new NameResolver(GraphName.newRoot(), remappings);
  }

  public static NameResolver create() {
    return create(GraphName.newRoot());
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
  public GraphName resolve(GraphName namespace, GraphName name) {
    GraphName remappedNamespace = lookUpRemapping(namespace);
    Preconditions.checkArgument(remappedNamespace.isGlobal(),
        "Namespace must be global. Tried to resolve: " + remappedNamespace);
    GraphName remappedName = lookUpRemapping(name);
    if (remappedName.isGlobal()) {
      return remappedName;
    }
    if (remappedName.isRelative()) {
      return remappedNamespace.join(remappedName);
    }
    if (remappedName.isPrivate()) {
      throw new RosRuntimeException("Cannot resolve ~private names in arbitrary namespaces.");
    }
    throw new RosRuntimeException("Unable to resolve graph name: " + name);
  }

  public String resolve(String namespace, String name) {
    return resolve(new GraphName(namespace), new GraphName(name)).toString();
  }

  /**
   * @param name
   *          name to resolve
   * @return the name resolved relative to the default namespace
   */
  public GraphName resolve(GraphName name) {
    return resolve(getNamespace(), name);
  }

  public String resolve(String name) {
    return resolve(getNamespace(), new GraphName(name)).toString();
  }

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
   * Construct a new {@link NameResolver} with the same remappings as this
   * resolver has. The namespace of the new resolver will be the value of the
   * name parameter resolved in this namespace.
   * 
   * @param name
   * @return {@link NameResolver} relative to the current namespace.
   */
  public NameResolver createResolver(GraphName name) {
    GraphName resolverNamespace = resolve(name);
    return new NameResolver(resolverNamespace, remappings);
  }

}
