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
 * A test of dynamic loading for ros classes.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public class RosTestMain {

  /**
   * @param args
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   * @throws RosInitException 
   */
  public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, RosInitException {
    // Example of using a string based class loader so that we can load classes
    // dynamically at runtime.
    CommandLineLoader loader = new CommandLineLoader(args);
    RosMain rm = loader.loadClass("org.ros.tutorials.RosPubSub");
    rm.rosMain(loader.getCleanCommandLineArgs(), loader.createContext());
  }

}
