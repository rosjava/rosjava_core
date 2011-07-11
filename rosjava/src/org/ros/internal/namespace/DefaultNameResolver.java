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

package org.ros.internal.namespace;

import com.google.common.base.Preconditions;

import org.ros.exception.RosNameException;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;

import java.util.Collections;
import java.util.Map;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class DefaultNameResolver implements NameResolver {

  private final GraphName namespace;
  private final Map<GraphName, GraphName> remappings;

  public DefaultNameResolver(GraphName namespace, Map<GraphName, GraphName> remappings) {
    this.remappings = Collections.unmodifiableMap(remappings);
    this.namespace = namespace;
  }

  @Override
  public GraphName getNamespace() {
    return namespace;
  }

  @Override
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
      throw new RosNameException("Cannot resolve ~private names in arbitrary namespaces.");
    }
    throw new RosNameException("Unable to resolve name: " + name);
  }

  @Override
  public String resolve(String namespace, String name) {
    return resolve(new GraphName(namespace), new GraphName(name)).toString();
  }

  @Override
  public GraphName resolve(GraphName name) {
    return resolve(getNamespace(), name);
  }

  @Override
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

  @Override
  public Map<GraphName, GraphName> getRemappings() {
    return remappings;
  }

  @Override
  public NameResolver createResolver(GraphName name) {
    GraphName resolverNamespace = resolve(name);
    return new DefaultNameResolver(resolverNamespace, remappings);
  }

}
