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

import org.ros.message.MessageListener;
import org.ros.message.std_msgs.Int64;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * This node is used to test the slave API externally using rostest.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class SlaveApiTestNode implements NodeMain {

  private Node node;

  @Override
  public void main(NodeConfiguration nodeConfiguration) {
    node = new DefaultNodeFactory().newNode("test_node", nodeConfiguration);

    // Basic chatter in/out test.
    Publisher<org.ros.message.std_msgs.String> pub_string =
        node.newPublisher("chatter_out", "std_msgs/String");
    MessageListener<org.ros.message.std_msgs.String> chatter_cb =
        new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onNewMessage(org.ros.message.std_msgs.String m) {
            System.out.println("String: " + m.data);
          }
        };

    node.newSubscriber("chatter_in", "std_msgs/String", chatter_cb);

    // Have at least one case of dual pub/sub on the same topic.
    Publisher<Int64> pub_int64_pubsub = node.newPublisher("int64", "std_msgs/Int64");
    MessageListener<Int64> int64_cb = new MessageListener<Int64>() {
      @Override
      public void onNewMessage(Int64 m) {
      }
    };

    node.newSubscriber("int64", "std_msgs/Int64", int64_cb);

    // Don't do any performance optimizations here. We want to make sure that
    // GC, etc. is working.
    while (true) {
      org.ros.message.std_msgs.String chatter = new org.ros.message.std_msgs.String();
      chatter.data = "hello " + System.currentTimeMillis();
      pub_string.publish(chatter);

      Int64 num = new Int64();
      num.data = 1;
      pub_int64_pubsub.publish(num);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void shutdown() {
    node.shutdown();
  }

}
