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

package org.ros;

import org.ros.exception.RosInitException;

/**
 * Class loader for finding {@link NodeMain}s given manifests, launch files,
 * etc.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public abstract class RosLoader {

  public abstract NodeConfiguration createConfiguration() throws RosInitException;

  /**
   * @param name the name of the class
   * @return an instance of {@link NodeMain}
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public NodeMain loadClass(String name) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    Class<?> clazz = getClass().getClassLoader().loadClass(name);
    return NodeMain.class.cast(clazz.newInstance());
  }

}
