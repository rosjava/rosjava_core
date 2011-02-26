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

import org.ros.exceptions.RosNameException;

/**
 * See the rules for names - http://www.ros.org/wiki/Names
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class NamespaceTools {
  /**
   * 
   * @param namespace
   * @param name
   * @return the fully resolved name relative to the given namespace.
   * @throws RosNameException Will throw on a poorly formated name.
   */
  public static String resolveName(Namespace namespace, String name) throws RosNameException {
    RosName n = new RosName(name);
    RosName ns = new RosName(namespace.getName());
    if (n.isGlobal())
      return n.toString();
    if (n.isRelative()) {
      String parent = ns.getParent();
      if (parent.isEmpty())
        return "/"+ n.toString();
      else
        return parent + "/" + n.toString();
    } else if (n.isPrivate()) {
      String parent = ns.toString();
      return parent + "/" + n.removeFrontDecorator();
    }
    throw new RosNameException("Bad name!");
  }
}
