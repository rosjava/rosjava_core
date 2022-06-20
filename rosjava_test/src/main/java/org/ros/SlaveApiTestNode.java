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

import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * This node is used to test the slave API externally using rostest.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class SlaveApiTestNode extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/slave_api_test_node");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    // Basic chatter in/out test.
    final Publisher<std_msgs.String> pub_string =
        connectedNode.newPublisher("chatter_out", std_msgs.String._TYPE);
    MessageListener<std_msgs.String> chatter_cb = new MessageListener<std_msgs.String>() {
      @Override
      public void onNewMessage(std_msgs.String m) {
        System.out.println("String: " + m.getData());
      }
    };

    Subscriber<std_msgs.String> stringSubscriber =
        connectedNode.newSubscriber("chatter_in", std_msgs.String._TYPE);
    stringSubscriber.addMessageListener(chatter_cb);

    // Have at least one case of dual pub/sub on the same topic.
    final Publisher<std_msgs.Int64> pub_int64_pubsub =
        connectedNode.newPublisher("int64", std_msgs.Int64._TYPE);
    MessageListener<std_msgs.Int64> int64_cb = new MessageListener<std_msgs.Int64>() {
      @Override
      public void onNewMessage(std_msgs.Int64 m) {
      }
    };

    Subscriber<std_msgs.Int64> int64Subscriber =
        connectedNode.newSubscriber("int64", "std_msgs/std_msgs.Int64");
    int64Subscriber.addMessageListener(int64_cb);

    // Don't do any performance optimizations here. We want to make sure that
    // GC, etc. is working.
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        MessageFactory defaultMessageFactory = connectedNode.getDefaultMessageFactory();
        std_msgs.String chatter = defaultMessageFactory.newFromType(std_msgs.String._TYPE);
        chatter.setData("hello " + System.currentTimeMillis());
        pub_string.publish(chatter);

        std_msgs.Int64 num = defaultMessageFactory.newFromType(std_msgs.Int64._TYPE);
        num.setData(1);
        pub_int64_pubsub.publish(num);
        Thread.sleep(100);
      }
    });
  }
}
