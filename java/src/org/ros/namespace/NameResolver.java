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

import org.ros.internal.namespace.RosName;

import org.ros.exceptions.RosNameException;

import java.util.HashMap;

/**
 * See the rules for names - http://www.ros.org/wiki/Names The resolver will do
 * ros name remappings for the user, if it has been initialized.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee), kwc@willowgarage.com (Ken
 *         Conley)
 * 
 */
// TODO need to implement ROS namespace remapping. Resolver seems to be the
// right place to do this as it is already aware of default namespace and is
// contained within the Node implementation.
public class NameResolver {

  private final HashMap<RosName, RosName> remappings;
  private final String namespace;

  /**
   * @param remappings
   * @throws RosNameException
   * 
   */
  public NameResolver(String namespace, HashMap<RosName, RosName> remappings) throws RosNameException {
    this.remappings = remappings;
    this.namespace = RosName.canonicalizeName(namespace);
  }

  public String getNamespace() {
    return namespace;
  }

  /**
   * Resolve name relative to namespace. If namespace is not global, it will
   * first be resolved to a global name.
   * 
   * This does all remappings of both the namespace and name.
   * 
   * @param namespace
   * @param name
   * @return the fully resolved name relative to the given namespace.
   * @throws RosNameException
   *           Will throw on a poorly formated name.
   */
  public String resolveName(String namespace, String name) throws RosNameException {
    RosName ns = lookUpRemapping(new RosName(namespace));
    Preconditions.checkArgument(ns.isGlobal(), "namespace must be global");
    RosName n = lookUpRemapping(new RosName(name));
    if (n.isGlobal()) {
      return n.toString();
    }
    if (n.isRelative()) {
      return join(ns.getParent(), n);
    } else if (n.isPrivate()) {
      String s = n.toRelative();
      // allow ~/foo
      if (s.startsWith("/")) {
        s = s.substring(1);
      }
      return join(ns, new RosName(s));
    }
    throw new RosNameException("Bad name: " + name);
  }

  /**
   * Convenience function for looking up a remapping.
   * 
   * @param name
   *          The name to lookup.
   * @return The name if it is not remapped, otherwise the remapped name.
   */
  private RosName lookUpRemapping(RosName name) {
    RosName rmname = name;
    if (remappings.containsKey(name)) {
      rmname = remappings.get(name);
    }
    return rmname;
  }

  @VisibleForTesting
  public HashMap<RosName, RosName> getRemappings() {
    return remappings;
  }

  /**
   * @param name
   *          Name to resolve
   * @return The name resolved relative to the default namespace.
   * @throws RosNameException
   */
  public String resolveName(String name) throws RosNameException {
    return resolveName(namespace, name);
  }

  /**
   * Join two names together.
   * 
   * @param name1
   *          ROS name to join to.
   * @param name2
   *          ROS name to join. Must be relative.
   * @return A concatination of the two names
   * @throws RosNameException
   *           If name1 or name2 is an illegal name
   * @throws IllegalArgumentException
   *           If name2 is not a relative name
   */
  public static String join(String name1, String name2) throws RosNameException {
    return join(new RosName(name1), new RosName(name2));
  }

  /**
   * Join two names together.
   * 
   * @param name1
   *          ROS name to join to.
   * @param name2
   *          ROS name to join. Must be relative.
   * @return A concatenation of the two names.
   * @throws RosNameException
   *           If name1 or name2 is an illegal name
   * @throws IllegalArgumentException
   *           If name2 is not a relative name.
   */
  public static String join(RosName name1, RosName name2) throws RosNameException {
    // TODO: review - another possible behavior is to just return name2
    Preconditions.checkArgument(name2.isRelative(),
        "name2 cannot be joined as it is global or private");
    if (name1.equals(Namespace.GLOBAL_NS)) {
      return Namespace.GLOBAL_NS + name2.toString();
    } else {
      return new RosName(name1.toString() + "/" + name2.toString()).toString();
    }
  }
}
