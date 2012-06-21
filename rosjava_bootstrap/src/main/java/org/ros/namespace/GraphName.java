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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.ros.exception.RosRuntimeException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ROS graph resource name.
 * 
 * @see <a href="http://www.ros.org/wiki/Names">Names documentation</a>
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class GraphName {

  @VisibleForTesting
  static final String ANONYMOUS_PREFIX = "anonymous_";

  private static final String EMPTY = "";
  private static final String ROOT = "/";
  private static final String SEPARATOR = "/";
  private static final String VALID_ROS_NAME_PATTERN = "^[\\~\\/A-Za-z][\\w_\\/]*$";

  private static final Cache<String, GraphName> cache = CacheBuilder.newBuilder().build();

  private static AtomicInteger anonymousCounter = new AtomicInteger();

  private final String name;

  // TODO(damonkohler): This is not safe across multiple hosts/processes.
  // Instead, try to use the same algorithm as in cpp and Python.
  /**
   * Creates an anonymous {@link GraphName}.
   * 
   * @return a new {@link GraphName} suitable for creating an anonymous node
   */
  public static GraphName newAnonymous() {
    return GraphName.of(ANONYMOUS_PREFIX + anonymousCounter.incrementAndGet());
  }

  /**
   * @return a {@link GraphName} representing the root namespace
   */
  public static GraphName root() {
    return GraphName.of(ROOT);
  }

  /**
   * @return an empty {@link GraphName}
   */
  public static GraphName empty() {
    return GraphName.of(EMPTY);
  }

  /**
   * Returns a {@link GraphName} instance representing the specified name. If a
   * new instance is not required, this method should generally be used in
   * preference to the constructor {@link #GraphName(String)}, as this method is
   * likely to yield significantly better space and time performance by caching
   * frequently requested values.
   * 
   * @param name
   * @return a {@link GraphName} instance representing the specified name
   */
  public static GraphName of(String name) {
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(validate(name), "Invalid graph name: " + name);
    final String canonicalName = canonicalize(name);
    try {
      return cache.get(canonicalName, new Callable<GraphName>() {
        @Override
        public GraphName call() throws Exception {
          return new GraphName(canonicalName);
        }
      });
    } catch (ExecutionException e) {
      throw new RosRuntimeException(e);
    }
  }

  /**
   * Constructs a new canonical {@link GraphName}.
   * 
   * @param name
   *          the name of this resource
   */
  public GraphName(String name) {
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(validate(name), "Invalid graph name: " + name);
    this.name = canonicalize(name);
  }

  /**
   * Returns {@code true} if the supplied {@link String} can be used to
   * construct a {@link GraphName}.
   * 
   * @param name
   *          the {@link String} representation of a {@link GraphName} to
   *          validate
   * @return {@code true} if the supplied name is can be used to construct a
   *         {@link GraphName}
   */
  public static boolean validate(String name) {
    // Allow empty names.
    if (name.length() > 0) {
      if (!name.matches(VALID_ROS_NAME_PATTERN)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Convert name into canonical representation. Canonical representation has no
   * trailing slashes. Canonical names can be global, private, or relative.
   * 
   * @param name
   * @return the canonical name for this {@link GraphName}
   */
  public static String canonicalize(String name) {
    Preconditions.checkArgument(validate(name));
    // Trim trailing slashes for canonical representation.
    while (!name.equals(GraphName.ROOT) && name.endsWith(SEPARATOR)) {
      name = name.substring(0, name.length() - 1);
    }
    if (name.startsWith("~/")) {
      name = "~" + name.substring(2);
    }
    return name;
  }

  /**
   * Is this a /global/name?
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
   * @return {@code true} if this name is a global name, {@code false} otherwise
   */
  public boolean isGlobal() {
    return name.startsWith(GraphName.ROOT);
  }

  /**
   * @return {@code true} if this {@link GraphName} represents the root
   *         namespace, {@code false} otherwise
   */
  public boolean isRoot() {
    return name.equals(GraphName.ROOT);
  }

  /**
   * @return {@code true} if this {@link GraphName} is empty, {@code false}
   *         otherwise
   */
  public boolean isEmpty() {
    return name.equals("");
  }

  /**
   * Is this a ~private/name?
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
   * @return {@code true} if the name is a private name, {@code false} otherwise
   */
  public boolean isPrivate() {
    return name.startsWith("~");
  }

  /**
   * Is this a relative/name?
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
   * @return the parent of this {@link GraphName} in its canonical
   *         representation or an empty {@link GraphName} if there is no parent
   */
  public GraphName getParent() {
    if (name.length() == 0) {
      return GraphName.empty();
    }
    if (name.equals(GraphName.ROOT)) {
      return GraphName.root();
    }
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > 1) {
      return GraphName.of(name.substring(0, slashIdx));
    } else {
      if (isGlobal()) {
        return GraphName.root();
      } else {
        return GraphName.empty();
      }
    }
  }

  /**
   * @return a {@link GraphName} without the leading parent namespace
   */
  public GraphName getBasename() {
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > -1) {
      if (slashIdx + 1 < name.length()) {
        return GraphName.of(name.substring(slashIdx + 1));
      }
      return GraphName.empty();
    }
    return this;
  }

  /**
   * Convert name to a relative name representation. This does not take any
   * namespace into account; it simply strips any preceding characters for
   * global or private name representation.
   * 
   * @return a relative {@link GraphName}
   */
  public GraphName toRelative() {
    if (isPrivate() || isGlobal()) {
      return GraphName.of(name.substring(1));
    } else {
      return this;
    }
  }

  /**
   * Convert name to a global name representation. This does not take any
   * namespace into account; it simply adds in the global prefix "/" if missing.
   * 
   * @return a global {@link GraphName}
   */
  public GraphName toGlobal() {
    if (isGlobal()) {
      return this;
    } else if (isPrivate()) {
      return GraphName.of(GraphName.ROOT + name.substring(1));
    } else {
      return GraphName.of(GraphName.ROOT + name);
    }
  }

  /**
   * Join this {@link GraphName} with another.
   * 
   * @param other
   *          the {@link GraphName} to join with, if other is global, this will
   *          return other.
   * @return a {@link GraphName} representing the concatenation of this
   *         {@link GraphName} and {@code other}
   */
  public GraphName join(GraphName other) {
    if (other.isGlobal() || isEmpty()) {
      return other;
    } else if (isRoot()) {
      return other.toGlobal();
    } else {
      return GraphName.of(toString() + SEPARATOR + other.toString());
    }
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GraphName other = (GraphName) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
