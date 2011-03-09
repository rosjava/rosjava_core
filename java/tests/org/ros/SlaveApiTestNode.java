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
import org.ros.exceptions.RosNameException;
import org.ros.message.std.Int64;

/**
 * This node is used to test the slave API externally using integration tests in
 * the test_ros package.
 * 
 * cd test_ros/test/client_verification/ ./test_slave_api.py --text
 * --profile=rosjava_profile.yaml
 * 
 * @author kwc
 * 
 */
public class SlaveApiTestNode {

  public static void main(java.lang.String[] argv) throws RosInitException, RosNameException {
    // Node node = new Node(argv, "sample_node"); this crashes when topic is
    // subscribed to
    Node node = new Node(argv, "test_node");
    node.init();

    // basic chatter in/out test
    Publisher<org.ros.message.std.String> pub_string =
        node.createPublisher("chatter_out", org.ros.message.std.String.class);
    MessageListener<org.ros.message.std.String> chatter_cb =
        new MessageListener<org.ros.message.std.String>() {
          @Override
          public void onNewMessage(org.ros.message.std.String m) {
          }
        };

    // Subscribers currently don't work
    Subscriber<org.ros.message.std.String> sub_string =
        node.createSubscriber("chatter_in", chatter_cb, org.ros.message.std.String.class);

    // have at least one case of dual pub/sub on same topic
    Publisher<Int64> pub_int64_pubsub = node.createPublisher("int64", Int64.class);
    MessageListener<Int64> int64_cb = new MessageListener<Int64>() {
      @Override
      public void onNewMessage(Int64 m) {
      }
    };
    
    Subscriber<Int64> sub_int64_pubsub = node.createSubscriber("int64", int64_cb, Int64.class);

    // don't do any performance optimizations here, want to make sure that
    // gc/etc... is working.
    while (true) {
      org.ros.message.std.String chatter = new org.ros.message.std.String();
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
}
