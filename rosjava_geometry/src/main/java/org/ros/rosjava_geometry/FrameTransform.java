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

package org.ros.rosjava_geometry;

import org.ros.namespace.GraphName;

/**
 * Describes a {@link Transform} from a source frame to a target frame.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class FrameTransform {

  private final Transform transform;
  private final GraphName source;
  private final GraphName target;

  public static FrameTransform
      fromTransformStamped(geometry_msgs.TransformStamped transformStamped) {
    Transform transform = Transform.newFromTransformMessage(transformStamped.getTransform());
    String source = transformStamped.getHeader().getFrameId();
    String target = transformStamped.getChildFrameId();
    return new FrameTransform(transform, GraphName.of(source), GraphName.of(target));
  }

  public FrameTransform(Transform transform, GraphName source, GraphName target) {
    this.transform = transform;
    this.source = source;
    this.target = target;
  }

  public Transform getTransform() {
    return transform;
  }

  public GraphName getSourceFrame() {
    return source;
  }

  public GraphName getTargetFrame() {
    return target;
  }

  @Override
  public String toString() {
    return String.format("FrameTransform<Source: %s, Target: %s, %s>", source, target, transform);
  }
}
