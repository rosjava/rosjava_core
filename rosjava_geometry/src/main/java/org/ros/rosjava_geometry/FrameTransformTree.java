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

package org.ros.rosjava_geometry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import geometry_msgs.TransformStamped;
import org.ros.namespace.GraphName;

import java.util.Map;

/**
 * A tree of {@link FrameTransform}s.
 * 
 * <p>
 * {@link FrameTransformTree} does not currently support time travel. Lookups
 * always use the newest {@link TransformStamped}.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 * 
 */
public class FrameTransformTree {

  /**
   * A {@link Map} from child frame ID to the child frame's most recent
   * transform.
   */
  private final Map<GraphName, geometry_msgs.TransformStamped> transforms;

  // TODO(damonkohler): Use NameResolver?
  private GraphName prefix;

  public FrameTransformTree() {
    transforms = Maps.newConcurrentMap();
    prefix = null;
  }

  /**
   * Adds a transform.
   * 
   * @param transform
   *          the transform to add
   */
  public void updateTransform(geometry_msgs.TransformStamped transform) {
    GraphName frame = GraphName.of(transform.getChildFrameId());
    transforms.put(frame, transform);
  }

  private geometry_msgs.TransformStamped getLatestTransform(GraphName frame) {
    GraphName fullyQualifiedFrame = makeFullyQualified(frame);
    return transforms.get(fullyQualifiedFrame);
  }

  /**
   * @param sourceFrame
   *          the source frame
   * @param targetFrame
   *          the target frame
   * @return {@code true} if there exists a {@link FrameTransform} from
   *         {@code sourceFrame} to {@code targetFrame}, {@code false} otherwise
   */
  public boolean canTransform(GraphName sourceFrame, GraphName targetFrame) {
    Preconditions.checkNotNull(sourceFrame);
    Preconditions.checkNotNull(targetFrame);
    FrameTransform sourceFrameTransform = newFrameTransformToRoot(sourceFrame);
    FrameTransform targetFrameTransform = newFrameTransformToRoot(targetFrame);
    return sourceFrameTransform.getTargetFrame().equals(targetFrameTransform.getTargetFrame());
  }

  /**
   * @return the {@link FrameTransform} from source the frame to the target
   *         frame
   */
  public FrameTransform newFrameTransform(GraphName sourceFrame, GraphName targetFrame) {
    Preconditions.checkNotNull(sourceFrame);
    Preconditions.checkNotNull(targetFrame);
    Preconditions.checkArgument(canTransform(sourceFrame, targetFrame),
        String.format("Cannot transform between %s and %s.", sourceFrame, targetFrame));
    FrameTransform sourceFrameTransform = newFrameTransformToRoot(sourceFrame);
    FrameTransform targetFrameTransform = newFrameTransformToRoot(targetFrame);
    Transform transform =
        targetFrameTransform.getTransform().invert().multiply(sourceFrameTransform.getTransform());
    return new FrameTransform(transform, sourceFrameTransform.getSourceFrame(),
        targetFrameTransform.getSourceFrame());
  }

  /**
   * @param frame
   *          the start frame
   * @return the {@link Transform} from {@code frame} to root
   */
  private FrameTransform newFrameTransformToRoot(GraphName frame) {
    GraphName sourceFrame = makeFullyQualified(frame);
    Transform result = Transform.newIdentityTransform();
    GraphName targetFrame = sourceFrame;
    while (true) {
      TransformStamped transformStamped = getLatestTransform(targetFrame);
      if (transformStamped == null) {
        return new FrameTransform(result, sourceFrame, targetFrame);
      }
      result = Transform.newFromTransformMessage(transformStamped.getTransform()).multiply(result);
      targetFrame = makeFullyQualified(GraphName.of(transformStamped.getHeader().getFrameId()));
    }
  }

  public void setPrefix(GraphName prefix) {
    this.prefix = prefix;
  }

  public void setPrefix(String prefix) {
    setPrefix(GraphName.of(prefix));
  }

  private GraphName makeFullyQualified(GraphName frame) {
    Preconditions.checkNotNull(frame, "Frame not specified.");
    if (prefix != null) {
      return prefix.join(frame);
    }
    return frame.toGlobal();
  }
}
