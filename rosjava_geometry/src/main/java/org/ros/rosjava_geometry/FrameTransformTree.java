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
import org.ros.namespace.NameResolver;

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

  private final NameResolver nameResolver;

  /**
   * A {@link Map} of the most recent {@link LazyFrameTransform} by source
   * frame.
   */
  private final Map<GraphName, LazyFrameTransform> transforms;

  public FrameTransformTree(NameResolver nameResolver) {
    this.nameResolver = nameResolver;
    transforms = Maps.newConcurrentMap();
  }

  /**
   * Updates the transform tree with the provided transform.
   * 
   * @param transformStamped
   *          the transform to update
   */
  public void updateTransform(geometry_msgs.TransformStamped transformStamped) {
    GraphName source = nameResolver.resolve(transformStamped.getChildFrameId());
    transforms.put(source, new LazyFrameTransform(transformStamped));
  }

  private FrameTransform getLatestTransform(GraphName frame) {
    LazyFrameTransform lazyFrameTransform = transforms.get(nameResolver.resolve(frame));
    if (lazyFrameTransform != null) {
      return lazyFrameTransform.get();
    }
    return null;
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
    FrameTransform source = newFrameTransformToRoot(sourceFrame);
    FrameTransform target = newFrameTransformToRoot(targetFrame);
    return source.getTargetFrame().equals(target.getTargetFrame());
  }

  /**
   * @return the {@link FrameTransform} from source the frame to the target
   *         frame, or {@code null} if no {@link FrameTransform} could be found
   */
  public FrameTransform newFrameTransform(GraphName sourceFrame, GraphName targetFrame) {
    Preconditions.checkNotNull(sourceFrame);
    Preconditions.checkNotNull(targetFrame);
    FrameTransform source = newFrameTransformToRoot(sourceFrame);
    FrameTransform target = newFrameTransformToRoot(targetFrame);
    if (source.getTargetFrame().equals(target.getTargetFrame())) {
      Transform transform = target.getTransform().invert().multiply(source.getTransform());
      return new FrameTransform(transform, source.getSourceFrame(), target.getSourceFrame());
    }
    return null;
  }

  /**
   * @param frame
   *          the start frame
   * @return the {@link Transform} from {@code frame} to root
   */
  private FrameTransform newFrameTransformToRoot(GraphName frame) {
    GraphName sourceFrame = nameResolver.resolve(frame);
    FrameTransform result =
        new FrameTransform(Transform.newIdentityTransform(), sourceFrame, sourceFrame);
    while (true) {
      FrameTransform parent = getLatestTransform(result.getTargetFrame());
      if (parent == null) {
        return result;
      }
      Transform transform = result.getTransform().multiply(parent.getTransform());
      GraphName targetFrame = nameResolver.resolve(parent.getSourceFrame());
      result = new FrameTransform(transform, sourceFrame, targetFrame);
    }
  }
}
