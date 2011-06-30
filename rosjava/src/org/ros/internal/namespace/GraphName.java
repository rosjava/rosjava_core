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
import org.ros.namespace.Namespace;

/**
 * ROS graph resource name.
 * 
 * @see "http://www.ros.org/wiki/Names"
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class GraphName {

  private static final String VALID_ROS_NAME_PATTERN = "^[\\~\\/A-Za-z][\\w_\\/]*$";
  private static final String UNKNOWN_NAME = "/unknown";

  private final String name;

  public static GraphName createUnknown() {
    return new GraphName(UNKNOWN_NAME);
  }

  public static GraphName createRoot() {
    return new GraphName(Namespace.GLOBAL);
  }

  /**
   * @param name
   */
  public GraphName(String name) {
    Preconditions.checkNotNull(name);
    validateName(name);
    this.name = canonicalizeName(name);
  }

  public static boolean validateName(String name) {
    // Allow empty names.
    if (name.length() > 0) {
      if (!name.matches(VALID_ROS_NAME_PATTERN)) {
        throw new RosNameException("Invalid unix name, may not contain special characters.");
      }
    }
    return true;
  }

  /**
   * Convert name into canonical representation. Canonical representation has no
   * trailing slashes. Canonical names can be global, private, or relative.
   * 
   * @param name
   * @return the canonical name for this graph node
   */
  public static String canonicalizeName(String name) {
    validateName(name);
    // Trim trailing slashes for canonical representation.
    while (!name.equals(Namespace.GLOBAL) && name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    if (name.startsWith("~/")) {
      name = "~" + name.substring(2);
    }
    return name;
  }

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
  public boolean isGlobal() {
    return name.startsWith(Namespace.GLOBAL);
  }
  
  public boolean isRoot() {
    return name.equals(Namespace.GLOBAL);
  }

  public boolean isEmpty() {
    return name.equals("");
  }

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
  public boolean isPrivate() {
    return name.startsWith("~");
  }

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
  public boolean isRelative() {
    return !isPrivate() && !isGlobal();
  }

  /**
   * @return Gets the parent of this name in canonical representation. This may
   *         return an empty name if there is no parent.
   */
  public GraphName getParent() {
    if (name.length() == 0) {
      return new GraphName("");
    }
    if (name.equals(Namespace.GLOBAL)) {
      return new GraphName(Namespace.GLOBAL);
    }
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > 1) {
      return new GraphName(name.substring(0, slashIdx));
    } else {
      if (isGlobal()) {
        return new GraphName(Namespace.GLOBAL);
      } else {
        return new GraphName("");
      }
    }
  }

  public GraphName getName() {
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > -1) {
      if (slashIdx + 1 < name.length()) {
        return new GraphName(name.substring(slashIdx + 1));
      }
      return new GraphName("");
    }
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return name.equals(obj.toString());
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Convert name to a relative name representation. This does not take any
   * namespace into account; it simply strips any preceding characters for
   * global or private name representation.
   * 
   * @return a string with the first
   */
  public String toRelative() {
    if (isPrivate() || isGlobal()) {
      return name.substring(1);
    } else {
      return name;
    }
  }

  /**
   * Convert name to a global name representation. This does not take any
   * namespace into account; it simply adds in the global prefix "/" if missing.
   * 
   * @return a string with the first
   */
  public GraphName toGlobal() {
    if (isGlobal()) {
      return this;
    } else if (isPrivate()) {
      return new GraphName(Namespace.GLOBAL + name.substring(1));
    } else {
      return new GraphName(Namespace.GLOBAL + name);
    }
  }

  /**
   * Join two names together.
   * 
   * @param other
   *          ROS name to join. If other is global, this will return other.
   * @return A concatenation of the two names.
   */
  public GraphName join(GraphName other) {
    if (other.isGlobal() || isEmpty()) {
      return other;
    } else if (isRoot()) {
      return other.toGlobal();
    } else {
      return new GraphName(toString() + "/" + other.toString());
    }
  }

}
