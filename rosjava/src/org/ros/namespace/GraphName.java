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


/**
 * ROS graph resource name.
 * 
 * @see "http://www.ros.org/wiki/Names"
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface GraphName {

  public final static String ROOT = "/";

  /**
   * This is a /global/name.
   * 
   * <ul>
   * <li>
   * If node node1 in the global / namespace accesses the resource /bar, that
   * will resolve to the name /bar.</li>
   * <li>
   * If node node2 in the /wg/ namespace accesses the resource /foo, that will
   * resolve to the name /foo.</li>
   * <li>
   * If node node3 in the /wg/ namespace accesses the resource /foo/bar, that
   * will resolve to the name /foo/bar.</li>
   * </ul>
   * 
   * @return If this name is a global name then return true.
   */
  boolean isGlobal();

  /**
   * Returns {@code true} if this {@link GraphName} represents the root
   * namespace.
   */
  boolean isRoot();

  /**
   * Returns {@code true} if this {@link GraphName} is empty.
   */
  boolean isEmpty();

  /**
   * Is this a ~private/name.
   * 
   * <ul>
   * <li>
   * If node node1 in the global / namespace accesses the resource ~bar, that
   * will resolve to the name /node1/bar.
   * <li>
   * If node node2 in the /wg/ namespace accesses the resource ~foo, that will
   * resolve to the name /wg/node2/foo.
   * <li>If node node3 in the /wg/ namespace accesses the resource ~foo/bar,
   * that will resolve to the name /wg/node3/foo/bar.
   * </ul>
   * 
   * @return true if the name is a private name.
   */
  boolean isPrivate();

  /**
   * Is this a relative/name.
   * 
   * <ul>
   * <li>If node node1 in the global / namespace accesses the resource ~bar,
   * that will resolve to the name /node1/bar.
   * <li>If node node2 in the /wg/ namespace accesses the resource ~foo, that
   * will resolve to the name /wg/node2/foo.
   * <li>If node node3 in the /wg/ namespace accesses the resource ~foo/bar,
   * that will resolve to the name /wg/node3/foo/bar.
   * </ul>
   * 
   * @return true if the name is a relative name.
   */
  boolean isRelative();

  /**
   * @return Gets the parent of this name in canonical representation. This may
   *         return an empty name if there is no parent.
   */
  GraphName getParent();

  /**
   * Returns a {@link GraphName} without the leading parent namespace.
   */
  GraphName getBasename();

  /**
   * Convert name to a relative name representation. This does not take any
   * namespace into account; it simply strips any preceding characters for
   * global or private name representation.
   * 
   * @return a relative {@link GraphName}
   */
  GraphName toRelative();

  /**
   * Convert name to a global name representation. This does not take any
   * namespace into account; it simply adds in the global prefix "/" if missing.
   * 
   * @return a string with the first
   */
  GraphName toGlobal();

  /**
   * Join this {@link GraphName} with another.
   * 
   * @param other
   *          the {@link GraphName} to join with, if other is global, this will
   *          return other.
   * @return a {@link GraphName} representing the concatenation of this
   *         {@link GraphName} and {@code other}
   */
  GraphName join(GraphName other);

}