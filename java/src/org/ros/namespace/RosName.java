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

/**
 * A simple class for handling name rules.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
//TODO: kwc: unless this becomes part of the user-facing API, should probably stuff this inside of internal
public class RosName {
  private final String name;

  /**
   * @param name
   * @throws RosNameException
   */
  public RosName(String name) throws RosNameException {
    Preconditions.checkNotNull(name);
    try {
      // allow empty name
      if (name.length() > 0) {
        Preconditions.checkArgument(name.matches("^[\\~\\/A-Za-z][\\w_\\/]*$"),
            "Invalid unix name, may not contain special characters.");
      }
    } catch (IllegalArgumentException e) {
      throw new RosNameException(e);
    }
    // trim trailing slashes for canonical representation
    while (name != Namespace.GLOBAL_NS && name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    this.name = name;
  }

  /**
   * This is a /global/name
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
  public Boolean isGlobal() {
    return name.startsWith("/");
  }

  /**
   * This is a ~private/name
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
  public Boolean isPrivate() {
    return name.startsWith("~");
  }

  /**
   * This is a relative/name
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
  public Boolean isRelative() {
    return !isPrivate() && !isGlobal();
  }

  /**
   * @return Gets the parent of this name, may be empty if there is no parent.
   */
  public String getParent() {
    if (name.length() == 0) {
      return "";
    }
    if (name.equals(Namespace.GLOBAL_NS)) {
      return Namespace.GLOBAL_NS;
    }
    int slashIdx = name.lastIndexOf('/');
    if (slashIdx > 1) {
      return name.substring(0, slashIdx);
    } else {
      if (isGlobal()) { 
        return Namespace.GLOBAL_NS;
      } else { 
        return "";
      }
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

  /**
   * Remove the first character from the name.
   * 
   * @return a string with the first
   */
  public String removeFrontDecorator() {
    if (isPrivate() || isGlobal())
      return name.substring(1);
    else
      return name;
  }

}
