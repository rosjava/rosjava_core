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
 * See the rules for names - http://www.ros.org/wiki/Names
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee), kwc@willowgarage.com (Ken
 *         Conley)
 * 
 */
// TODO need to implement ROS namespace remapping. Resolver seems to be the
// right place to do this as it is already aware of default namespace and is
// contained within the Node implementation.
public class Resolver {

  private static Resolver s_default = new Resolver();

  public static Resolver getDefault() {
    return s_default;
  }

  /**
   * 
   * @return The default namespace of this process.
   * @throws RosNameException
   *           If default namespace is set to invalid name.
   */
  public static String getDefaultNamespace() throws RosNameException {
    // Resolution rules: ROS defines two methods for setting the namespace (in
    // order of precedence)
    // 1) The __ns:= command line argument
    // 2) the ROS_NAMESPACE environment variable
    // It is the responsibility of the loader to obey this contract and set the
    // rosNamespace system property

    // This routine does not need to be high performance.
    return new RosName(
        System.getProperty(Namespace.DEFAULT_NAMESPACE_PROPERTY, Namespace.GLOBAL_NS)).toString();
  }

  /**
   * Resolve name relative to namespace. If namespace is not global, it will
   * first be resolved to a global name.
   * 
   * @param namespace
   * @param name
   * @return the fully resolved name relative to the given namespace.
   * @throws RosNameException
   *           Will throw on a poorly formated name.
   */
  public String resolveName(String namespace, String name) throws RosNameException {
    RosName ns = new RosName(namespace);
    Preconditions.checkArgument(ns.isGlobal(), "namespace must be global");
    RosName n = new RosName(name);
    if (n.isGlobal()) {
      return n.toString();
    }
    if (n.isRelative()) {
      return join(new RosName(ns.getParent()), n);
    } else if (n.isPrivate()) {
      String s = n.removeFrontDecorator();
      // allow ~/foo
      if (s.startsWith("/")) { 
        s = s.substring(1);
      }
      return join(ns, new RosName(s));
    }
    throw new RosNameException("Bad name: " + name);
  }

  /**
   * Join two names together.
   * 
   * @param name1
   *          ROS name to join to.
   * @param name2
   *          ROS name to join. Must be relative.
   * @return
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
   * @return
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
