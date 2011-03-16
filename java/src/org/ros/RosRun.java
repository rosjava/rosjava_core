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

import org.ros.exceptions.RosNameException;

import org.ros.exceptions.RosInitException;

/**
 * [Unstable] rosrun-compatible loader for rosjava-based nodes. This is a work
 * in progress. Important questions, such as how to configure a Java classpath
 * in a ROS-based environment are important to determining the final API and
 * behavior of this interface.
 * 
 * @author kwc
 */
public class RosRun {

  public static void printUsage() {
    System.err.println("Usage: java -jar rosjava.jar org.foo.MyNode [args]");
  }

  // TODO(kwc): in the future, will need some sort of classpath bootstrapping
  /**
   * Usage: rosrun rosjava run org.foo.Node [args]
   * 
   * @param argv
   * @throws RosInitException
   */
  public static void main(String[] argv) {
    if (argv.length == 0) {
      printUsage();
      System.exit(1);
    }
    String nodeClassName = argv[0];
    String[] nodeArgs = new String[argv.length - 1];

    System.out.println("loading node class: " + nodeClassName);
    System.arraycopy(argv, 1, nodeArgs, 0, nodeArgs.length);

    RosLoader rl = new CommandLineLoader(nodeArgs);
    NodeContext nodeContext = null;
    try {
      nodeContext = rl.createContext();
    } catch (RosInitException e1) {
      e1.printStackTrace();
      System.exit(2);
    }
    RosMain rm = null;
    try {
      rm = rl.loadClass(nodeClassName);
    } catch (ClassNotFoundException e) {
      System.err.println("Unable to locate node: " + nodeClassName);
      System.exit(3);
    } catch (InstantiationException e) {
      e.printStackTrace();
      System.err.println("Unable to instantiate node: " + nodeClassName);
      System.exit(4);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      System.err.println("Unable to instantiate node: " + nodeClassName);
      System.exit(5);
    }

    try {
      rm.rosMain(argv, nodeContext);
    } catch (RosInitException e) {
      e.printStackTrace();
    } catch (RosNameException e) {
      System.err.println(e.toString());
    }
  }

}
