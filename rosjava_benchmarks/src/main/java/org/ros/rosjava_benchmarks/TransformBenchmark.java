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

package org.ros.rosjava_benchmarks;

import org.ros.concurrent.CancellableLoop;
import org.ros.concurrent.Rate;
import org.ros.concurrent.WallTimeRate;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_geometry.FrameTransformTree;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TransformBenchmark extends AbstractNodeMain {

  private final AtomicInteger counter;

  private Time time;

  public TransformBenchmark() {
    counter = new AtomicInteger();
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("transform_benchmark");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    final geometry_msgs.TransformStamped turtle1 =
        connectedNode.getTopicMessageFactory().newFromType(geometry_msgs.TransformStamped._TYPE);
    turtle1.getHeader().setFrameId("world");
    turtle1.setChildFrameId("turtle1");
    final geometry_msgs.TransformStamped turtle2 =
        connectedNode.getTopicMessageFactory().newFromType(geometry_msgs.TransformStamped._TYPE);
    turtle2.getHeader().setFrameId("world");
    turtle2.setChildFrameId("turtle2");
    final FrameTransformTree frameTransformTree = new FrameTransformTree(NameResolver.newRoot());
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        updateTransform(turtle1, connectedNode, frameTransformTree);
        updateTransform(turtle2, connectedNode, frameTransformTree);
        frameTransformTree.transform("turtle1", "turtle2");
        counter.incrementAndGet();
      }
    });

    time = connectedNode.getCurrentTime();
    final Publisher<std_msgs.String> statusPublisher =
        connectedNode.newPublisher("status", std_msgs.String._TYPE);
    final Rate rate = new WallTimeRate(1);
    final std_msgs.String status = statusPublisher.newMessage();
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        Time now = connectedNode.getCurrentTime();
        Duration delta = now.subtract(time);
        if (delta.totalNsecs() > TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS)) {
          double hz = counter.getAndSet(0) * 1e9 / delta.totalNsecs();
          status.setData(String.format("%.2f Hz", hz));
          statusPublisher.publish(status);
          time = now;
        }
        rate.sleep();
      }
    });
  }

  private void updateTransform(geometry_msgs.TransformStamped transformStamped,
      ConnectedNode connectedNode, FrameTransformTree frameTransformTree) {
    transformStamped.getHeader().setStamp(connectedNode.getCurrentTime());
    transformStamped.getTransform().getRotation().setW(Math.random());
    transformStamped.getTransform().getRotation().setX(Math.random());
    transformStamped.getTransform().getRotation().setY(Math.random());
    transformStamped.getTransform().getRotation().setZ(Math.random());
    transformStamped.getTransform().getTranslation().setX(Math.random());
    transformStamped.getTransform().getTranslation().setY(Math.random());
    transformStamped.getTransform().getTranslation().setZ(Math.random());
    frameTransformTree.update(transformStamped);
  }
}
