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
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.Publisher;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class PassthroughTestNode implements NodeMain {

  private Node node;

  @Override
  public void main(NodeConfiguration nodeConfiguration) {
    node = Ros.newNode("test_node", nodeConfiguration);

    // The goal of the passthrough node is simply to retransmit the messages
    // sent to it. This allows us to external verify that the node is compatible
    // with multiple publishers, multiple subscribers, etc...

    // String pass through
    final Publisher<org.ros.message.std_msgs.String> pub_string =
        node.newPublisher("string_out", "std_msgs/String");
    MessageListener<org.ros.message.std_msgs.String> string_cb =
        new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onNewMessage(org.ros.message.std_msgs.String m) {
            pub_string.publish(m);
          }
        };
    node.newSubscriber("string_in", "std_msgs/String", string_cb);

    // Int64 pass through
    final Publisher<org.ros.message.std_msgs.Int64> pub_int64 =
        node.newPublisher("int64_out", "std_msgs/Int64");
    MessageListener<org.ros.message.std_msgs.Int64> int64_cb =
        new MessageListener<org.ros.message.std_msgs.Int64>() {
          @Override
          public void onNewMessage(org.ros.message.std_msgs.Int64 m) {
            pub_int64.publish(m);
          }
        };
    node.newSubscriber("int64_in", "std_msgs/Int64", int64_cb);

    // TestHeader pass through
    final Publisher<org.ros.message.test_ros.TestHeader> pub_header =
        node.newPublisher("test_header_out", "test_ros/TestHeader");
    MessageListener<org.ros.message.test_ros.TestHeader> header_cb =
        new MessageListener<org.ros.message.test_ros.TestHeader>() {
          @Override
          public void onNewMessage(org.ros.message.test_ros.TestHeader m) {
            m.orig_caller_id = m.caller_id;
            m.caller_id = node.getName();
            pub_header.publish(m);
          }
        };
    node.newSubscriber("test_header_in", "test_ros/TestHeader", header_cb);

    // TestComposite pass through
    final Publisher<org.ros.message.test_ros.Composite> pub_composite =
        node.newPublisher("composite_out", "test_ros/Composite");
    MessageListener<org.ros.message.test_ros.Composite> composite_cb =
        new MessageListener<org.ros.message.test_ros.Composite>() {
          @Override
          public void onNewMessage(org.ros.message.test_ros.Composite m) {
            pub_composite.publish(m);
          }
        };
    node.newSubscriber("composite_in", "test_ros/Composite", composite_cb);
  }

  @Override
  public void shutdown() {
    node.shutdown();
  }

}
