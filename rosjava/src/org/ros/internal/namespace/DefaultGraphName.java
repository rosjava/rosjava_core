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

import org.ros.Ros;
import org.ros.exception.RosNameException;
import org.ros.namespace.GraphName;

/**
 * ROS graph resource name.
 * 
 * @see "http://www.ros.org/wiki/Names"
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class DefaultGraphName implements GraphName {

  private static final String VALID_ROS_NAME_PATTERN = "^[\\~\\/A-Za-z][\\w_\\/]*$";
  private static final String UNKNOWN_NAME = "/unknown";

  private final String name;

  public static DefaultGraphName createUnknown() {
    return new DefaultGraphName(UNKNOWN_NAME);
  }

  public static DefaultGraphName createRoot() {
    return new DefaultGraphName(GraphName.ROOT);
  }

  /**
   * @param name
   *          the {@link String} representation of this {@link DefaultGraphName}
   */
  public DefaultGraphName(String name) {
    Preconditions.checkNotNull(name);
    validate(name);
    this.name = canonicalize(name);
  }

  /**
   * Returns {@code true} if the supplied {@link String} can be used to
   * construct a {@link DefaultGraphName}.
   * 
   * @param name
   *          the {@link String} representation of a {@link DefaultGraphName} to
   *          validate
   * @return {@code true} if the supplied name is can be used to construct a
   *         {@link DefaultGraphName}
   */
  public static boolean validate(String name) {
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
   * @return the canonical name for this {@link DefaultGraphName}
   */
  public static String canonicalize(String name) {
    Preconditions.checkArgument(validate(name));
    // Trim trailing slashes for canonical representation.
    while (!name.equals(GraphName.ROOT) && name.endsWith("/")) {
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
  @Override
  public boolean isGlobal() {
    return name.startsWith(GraphName.ROOT);
  }

  /**
   * Returns {@code true} if this {@link DefaultGraphName} represents the root
   * namespace.
   */
  @Override
  public boolean isRoot() {
    return name.equals(GraphName.ROOT);
  }

  /**
   * Returns {@code true} if this {@link DefaultGraphName} is empty.
   */
  @Override
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
  @Override
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
  @Override
  public boolean isRelative() {
    return !isPrivate() && !isGlobal();
  }

  /**
   * @return Gets the parent of this name in canonical representation. This may
   *         return an empty name if there is no parent.
   */
  @Override
  public GraphName getParent() {
    if (name.length() == 0) {
      return Ros.createGraphName("");
    }
    if (name.equals(GraphName.ROOT)) {
      return Ros.createGraphName(GraphName.ROOT);
    }
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > 1) {
      return Ros.createGraphName(name.substring(0, slashIdx));
    } else {
      if (isGlobal()) {
        return Ros.createGraphName(GraphName.ROOT);
      } else {
        return Ros.createGraphName("");
      }
    }
  }

  /**
   * Returns a {@link DefaultGraphName} without the leading parent namespace.
   */
  @Override
  public GraphName getBasename() {
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > -1) {
      if (slashIdx + 1 < name.length()) {
        return Ros.createGraphName(name.substring(slashIdx + 1));
      }
      return Ros.createGraphName("");
    }
    return this;
  }

  /**
   * Convert name to a relative name representation. This does not take any
   * namespace into account; it simply strips any preceding characters for
   * global or private name representation.
   * 
   * @return a relative {@link DefaultGraphName}
   */
  @Override
  public GraphName toRelative() {
    if (isPrivate() || isGlobal()) {
      return Ros.createGraphName(name.substring(1));
    } else {
      return this;
    }
  }

  /**
   * Convert name to a global name representation. This does not take any
   * namespace into account; it simply adds in the global prefix "/" if missing.
   * 
   * @return a string with the first
   */
  @Override
  public GraphName toGlobal() {
    if (isGlobal()) {
      return this;
    } else if (isPrivate()) {
      return Ros.createGraphName(GraphName.ROOT + name.substring(1));
    } else {
      return Ros.createGraphName(GraphName.ROOT + name);
    }
  }

  /**
   * Join this {@link DefaultGraphName} with another.
   * 
   * @param other
   *          the {@link DefaultGraphName} to join with, if other is global, this will
   *          return other.
   * @return a {@link DefaultGraphName} representing the concatenation of this
   *         {@link DefaultGraphName} and {@code other}
   */
  @Override
  public GraphName join(GraphName other) {
    if (other.isGlobal() || isEmpty()) {
      return other;
    } else if (isRoot()) {
      return other.toGlobal();
    } else {
      return Ros.createGraphName(toString() + "/" + other.toString());
    }
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

}
