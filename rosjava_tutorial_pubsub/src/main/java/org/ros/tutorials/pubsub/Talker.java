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

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * A simple {@link Publisher} {@link Node}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Talker implements NodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("rosjava_tutorial_pubsub/talker");
  }

  @Override
  public void onStart(final Node node) {
    final Publisher<org.ros.message.std_msgs.String> publisher =
        node.newPublisher("chatter", "std_msgs/String");
    // This CancellableLoop will be canceled automatically when the Node shuts
    // down.
    node.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;

      @Override
      protected void setup() {
        sequenceNumber = 0;
      }

      @Override
      protected void loop() throws InterruptedException {
        org.ros.message.std_msgs.String str = new org.ros.message.std_msgs.String();
        str.data = "Hello world! " + sequenceNumber;
        publisher.publish(str);
        sequenceNumber++;
        Thread.sleep(1000);
      }
    });
  }

  @Override
  public void onShutdown(Node node) {
  }

  @Override
  public void onShutdownComplete(Node node) {
  }
}
