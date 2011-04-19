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

import com.google.common.collect.Lists;

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;

/**
 * This is a rosrun-compatible loader for rosjava-based nodes.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosRun {

  public static void printUsage() {
    System.err.println("Usage: java -jar rosjava.jar org.foo.MyNode [args]");
  }

  // TODO(kwc): In the future, will need some sort of classpath bootstrapping.
  /**
   * Usage: rosrun rosjava run org.foo.Node [args]
   * 
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    if (argv.length == 0) {
      printUsage();
      System.exit(1);
    }

    CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(argv));
    String nodeClassName = loader.getNodeClassName();
    System.out.println("Loading node class: " + loader.getNodeClassName());
    NodeContext nodeContext = null;
    try {
      nodeContext = loader.createContext();
    } catch (RosInitException e1) {
      e1.printStackTrace();
      System.exit(2);
    }

    NodeMain nodeMain = null;
    try {
      nodeMain = loader.loadClass(nodeClassName);
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
      nodeMain.run(loader.getArgv(), nodeContext);
    } catch (RosInitException e) {
      e.printStackTrace();
      System.exit(6);
    } catch (RosNameException e) {
      System.err.println(e.toString());
      System.exit(7);
    }
  }

}
