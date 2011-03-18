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

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.NodeContext;
import org.ros.Publisher;
import org.ros.RosMain;

/**
 * Simple rosjava publisher and subscriber node, requires an external roscore
 * (master) running.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public class RosPubSub implements RosMain {
  Node node;
  Publisher<org.ros.message.std_msgs.String> pub;

  // callback for string messages
  MessageListener<org.ros.message.std_msgs.String> helloCallback = new MessageListener<org.ros.message.std_msgs.String>() {

    @Override
    public void onNewMessage(org.ros.message.std_msgs.String m) {
      node.getLog().info(m.data);
    }
  };

  @Override
  public void rosMain(String[] argv, NodeContext context) {
    try {
      node = new Node("rosjava_sample_node", context);
      node.init();
      pub = node.createPublisher("~hello", org.ros.message.std_msgs.String.class);
      node.createSubscriber("~hello", helloCallback, org.ros.message.std_msgs.String.class);

      int seq = 0;
      while (true) {
        org.ros.message.std_msgs.String str = new org.ros.message.std_msgs.String();
        str.data = "Hello " + seq++;
        pub.publish(str);
        Thread.sleep(100);
      }
    } catch (Exception e) {
      node.getLog().fatal(e);
    }
  }

}
