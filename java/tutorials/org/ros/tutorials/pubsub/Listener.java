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

package org.ros.tutorials.pubsub;

import org.apache.commons.logging.Log;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.NodeConfiguration;
import org.ros.NodeMain;
import org.ros.Subscriber;

/**
 * This is a simple rosjava {@link Subscriber} {@link Node}. It assumes an
 * external roscore is already running.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Listener implements NodeMain {

  @Override
  public void run(NodeConfiguration context) {
    Node node = null;
    try {
      node = new Node("listener", context);
      final Log log = node.getLog();
      node.createSubscriber("chatter", new MessageListener<org.ros.message.std_msgs.String>() {
        @Override
        public void onNewMessage(org.ros.message.std_msgs.String message) {
          log.info("I heard: \"" + message.data + "\"");
        }
      }, org.ros.message.std_msgs.String.class);
    } catch (Exception e) {
      if (node != null) {
        node.getLog().fatal(e);
      } else {
        e.printStackTrace();
      }
    }
  }

}
