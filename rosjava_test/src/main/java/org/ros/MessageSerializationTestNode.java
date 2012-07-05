/*
 * Copyright (C) 2012 Google Inc.
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

import nav_msgs.Odometry;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationTestNode implements NodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("message_serialization_test_node");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    final Publisher<nav_msgs.Odometry> publisher =
        connectedNode.newPublisher("odom_echo", nav_msgs.Odometry._TYPE);
    Subscriber<nav_msgs.Odometry> subscriber =
        connectedNode.newSubscriber("odom", nav_msgs.Odometry._TYPE);
    subscriber.addMessageListener(new MessageListener<Odometry>() {
      @Override
      public void onNewMessage(Odometry message) {
        publisher.publish(message);
      }
    });
  }

  @Override
  public void onShutdown(Node node) {
  }

  @Override
  public void onShutdownComplete(Node node) {
  }

  @Override
  public void onError(Node node, Throwable throwable) {
  }
}
