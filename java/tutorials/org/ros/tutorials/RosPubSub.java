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
package org.ros.tutorials;

import org.ros.internal.loader.EnvironmentVariables;

import java.util.HashMap;

import org.ros.exceptions.RosInitException;

import org.ros.CommandLineLoader;

import org.ros.NodeContext;

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.RosLoader;
import org.ros.RosMain;

/**
 * Simple rosjava publisher and subscriber node, requires an external roscore
 * (master) running.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public class RosPubSub extends RosMain {
  Node node;
  // FIXME ugly huge expansion of message name due to String class?
  Publisher<org.ros.message.std.String> pub;

  // callback for string messages
  MessageListener<org.ros.message.std.String> hello_cb = new MessageListener<org.ros.message.std.String>() {

    @Override
    public void onNewMessage(org.ros.message.std.String m) {
      node.getLog().info(m.data);
    }
  };

  @Override
  public void rosMain(String[] argv, NodeContext context) {
    try {
      node = new Node("rosjava/sample_node", context);
      node.init();
      pub = node.createPublisher("~hello", org.ros.message.std.String.class);
      node.createSubscriber("~hello", hello_cb, org.ros.message.std.String.class);

      int seq = 0;
      while (true) {
        org.ros.message.std.String str = new org.ros.message.std.String();
        str.data = "Hello " + seq++;
        pub.publish(str);
        Thread.sleep(100);
      }
    } catch (Exception e) {
      node.getLog().fatal(e);
    }
  }

  public static void main(String[] argv) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, RosInitException {

    // Example of using a string based class loader so that we can load classes
    // dynamically at runtime.
    // TODO(ethan) this is internal stuff, move away.
    HashMap<String, String> fakeEnv = new HashMap<String, String>();
    fakeEnv.put(EnvironmentVariables.ROS_MASTER_URI, "http://localhost:11311");
    RosLoader rl = new CommandLineLoader(argv, fakeEnv);
    NodeContext nodeContext = rl.createContext();
    RosMain rm = rl.loadClass("org.ros.tutorials.RosPubSub");
    rm.rosMain(argv, nodeContext);
  }
}
