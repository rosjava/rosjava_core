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

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface NameResolver {

  GraphName getNamespace();

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
  GraphName resolve(GraphName namespace, GraphName name);

  String resolve(String namespace, String name);

  /**
   * @param name
   *          name to resolve
   * @return the name resolved relative to the default namespace
   */
  GraphName resolve(GraphName name);

  String resolve(String name);

  Map<GraphName, GraphName> getRemappings();

  /**
   * Construct a new {@link NameResolver} with the same remappings as this
   * resolver has. The namespace of the new resolver will be the value of the
   * name parameter resolved in this namespace.
   * 
   * @param name
   * @return {@link NameResolver} relative to the current namespace.
   */
  NameResolver createResolver(GraphName name);

}