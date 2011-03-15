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

import org.ros.exceptions.RosInitException;

/**
 * Prototype class loader for finding RosMain's from manifests,launch files,
 * etc.. This might be an internal thing.. just playing with it for now.
 * 
 * @author Ethan Rublee (ethan.rublee@gmail.com)
 */
public abstract class RosLoader {

  public abstract NodeContext createContext() throws RosInitException;

  /**
   * @param name
   *          The name of the class
   * @return an instance of a RosMain, may be run.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public RosMain loadClass(String name) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    Class<?> clazz = getClass().getClassLoader().loadClass(name);

    return RosMain.class.cast(clazz.newInstance());
  }
}