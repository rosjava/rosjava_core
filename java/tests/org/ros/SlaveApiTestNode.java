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
import org.ros.message.std_msgs.Int64;

import java.util.List;

/**
 * This node is used to test the slave API externally using rostest.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class SlaveApiTestNode implements NodeMain {

  @Override
  public void run(List<String> argv, NodeContext nodeContext) throws RosInitException {
    final Node node = new Node("test_node", nodeContext);

    // Basic chatter in/out test.
    Publisher<org.ros.message.std_msgs.String> pub_string =
        node.createPublisher("chatter_out", org.ros.message.std_msgs.String.class);
    MessageListener<org.ros.message.std_msgs.String> chatter_cb =
        new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onSuccess(org.ros.message.std_msgs.String m) {
            System.out.println("String: " + m.data);
          }

          @Override
          public void onFailure(Exception e) {
            throw new RuntimeException(e);
          }
        };

    node.createSubscriber("chatter_in", chatter_cb, org.ros.message.std_msgs.String.class);

    // Have at least one case of dual pub/sub on the same topic.
    Publisher<Int64> pub_int64_pubsub = node.createPublisher("int64", Int64.class);
    MessageListener<Int64> int64_cb = new MessageListener<Int64>() {
      @Override
      public void onSuccess(Int64 m) {
      }

      @Override
      public void onFailure(Exception e) {
        throw new RuntimeException(e);
      }
    };

    node.createSubscriber("int64", int64_cb, Int64.class);

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

}
