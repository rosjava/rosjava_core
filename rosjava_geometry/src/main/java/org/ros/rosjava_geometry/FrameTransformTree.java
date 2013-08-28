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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import geometry_msgs.TransformStamped;
import org.ros.concurrent.CircularBlockingDeque;
import org.ros.message.Time;
import java.util.Map;

/**
 * A tree of {@link FrameTransform}s.
 * <p>
 * {@link FrameTransformTree} does not currently support time travel. Lookups
 * always use the newest {@link TransformStamped}.
 *
 * @author damonkohler@google.com (Damon Kohler)
 * @author moesenle@google.com (Lorenz Moesenlechner)
 */
public class FrameTransformTree {

  private static final int TRANSFORM_QUEUE_CAPACITY = 16;

  private final Object mutex;

  /**
   * A {@link Map} of the most recent {@link LazyFrameTransform} by source
   * frame. Lookups by target frame or by the pair of source and target are both
   * unnecessary because every frame can only have exactly one target.
   */
  private final Map<FrameName, CircularBlockingDeque<LazyFrameTransform>> transforms;

  public FrameTransformTree() {
    mutex = new Object();
    transforms = Maps.newConcurrentMap();
  }

  /**
   * Updates the tree with the provided {@link geometry_msgs.TransformStamped}
   * message.
   * <p>
   * Note that the tree is updated lazily. Modifications to the provided
   * {@link geometry_msgs.TransformStamped} message may cause unpredictable
   * results.
   *
   * @param transformStamped
   *          the {@link geometry_msgs.TransformStamped} message to update with
   */
  public void update(geometry_msgs.TransformStamped transformStamped) {
    Preconditions.checkNotNull(transformStamped);
    FrameName transformName = FrameName.of(transformStamped.getChildFrameId());
    LazyFrameTransform lazyFrameTransform = new LazyFrameTransform(transformStamped);
    add(transformName, lazyFrameTransform);
  }

  @VisibleForTesting
  void update(FrameTransform frameTransform) {
    Preconditions.checkNotNull(frameTransform);
    FrameName source = frameTransform.getSourceFrame();
    LazyFrameTransform lazyFrameTransform = new LazyFrameTransform(frameTransform);
    add(source, lazyFrameTransform);
  }

  private void add(FrameName source, LazyFrameTransform lazyFrameTransform) {
    if (!transforms.containsKey(source)) {
      transforms.put(source, new CircularBlockingDeque<LazyFrameTransform>(
          TRANSFORM_QUEUE_CAPACITY));
    }
    synchronized (mutex) {
      transforms.get(source).addFirst(lazyFrameTransform);
    }
  }

  /**
   * Returns the most recent {@link FrameTransform} for target {@code source}.
   *
   * @param source
   *          the frame to look up
   * @return the most recent {@link FrameTransform} for {@code source} or
   *         {@code null} if no transform for {@code source} is available
   */
  public FrameTransform lookUp(FrameName source) {
    Preconditions.checkNotNull(source);
    return getLatest(source);
  }

  private FrameTransform getLatest(FrameName source) {
    CircularBlockingDeque<LazyFrameTransform> deque = transforms.get(source);
    if (deque == null) {
      return null;
    }
    LazyFrameTransform lazyFrameTransform = deque.peekFirst();
    if (lazyFrameTransform == null) {
      return null;
    }
    return lazyFrameTransform.get();
  }

  /**
   * @see #lookUp(FrameName)
   */
  public FrameTransform get(String source) {
    Preconditions.checkNotNull(source);
    return lookUp(FrameName.of(source));
  }

  /**
   * Returns the {@link FrameTransform} for {@code source} closest to
   * {@code time}.
   *
   * @param source
   *          the frame to look up
   * @param time
   *          the transform for {@code frame} closest to this {@link Time} will
   *          be returned
   * @return the most recent {@link FrameTransform} for {@code source} or
   *         {@code null} if no transform for {@code source} is available
   */
  public FrameTransform lookUp(FrameName source, Time time) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(time);
    return get(source, time);
  }

  // TODO(damonkohler): Use an efficient search.
  private FrameTransform get(FrameName source, Time time) {
    CircularBlockingDeque<LazyFrameTransform> deque = transforms.get(source);
    if (deque == null) {
      return null;
    }
    LazyFrameTransform result = null;
    synchronized (mutex) {
      long offset = 0;
      for (LazyFrameTransform lazyFrameTransform : deque) {
        if (result == null) {
          result = lazyFrameTransform;
          offset = Math.abs(time.subtract(result.get().getTime()).totalNsecs());
          continue;
        }
        long newOffset = Math.abs(time.subtract(lazyFrameTransform.get().getTime()).totalNsecs());
        if (newOffset < offset) {
          result = lazyFrameTransform;
          offset = newOffset;
        }
      }
    }
    if (result == null) {
      return null;
    }
    return result.get();
  }

  /**
   * @see #lookUp(FrameName, Time)
   */
  public FrameTransform get(String source, Time time) {
    Preconditions.checkNotNull(source);
    return lookUp(FrameName.of(source), time);
  }

  /**
   * @return the {@link FrameTransform} from source the frame to the target
   *         frame, or {@code null} if no {@link FrameTransform} could be found
   */
  public FrameTransform transform(FrameName source, FrameName target) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(target);
    if (source.equals(target)) {
      return new FrameTransform(Transform.identity(), source, target, null);
    }
    FrameTransform sourceToRoot = transformToRoot(source);
    FrameTransform targetToRoot = transformToRoot(target);
    if (sourceToRoot == null && targetToRoot == null) {
      return null;
    }
    if (sourceToRoot == null) {
      if (targetToRoot.getTargetFrame().equals(source)) {
        // resolvedSource is root.
        return targetToRoot.invert();
      } else {
        return null;
      }
    }
    if (targetToRoot == null) {
      if (sourceToRoot.getTargetFrame().equals(target)) {
        // resolvedTarget is root.
        return sourceToRoot;
      } else {
        return null;
      }
    }
    if (sourceToRoot.getTargetFrame().equals(targetToRoot.getTargetFrame())) {
      // Neither resolvedSource nor resolvedTarget is root and both share the
      // same root.
      Transform transform =
          targetToRoot.getTransform().invert().multiply(sourceToRoot.getTransform());
      return new FrameTransform(transform, source, target, sourceToRoot.getTime());
    }
    // No known transform.
    return null;
  }

  /**
   * @see #transform(FrameName, FrameName)
   */
  public FrameTransform transform(String source, String target) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(target);
    return transform(FrameName.of(source), FrameName.of(target));
  }

  /**
   * @param source
   *          the resolved source frame
   * @return the {@link Transform} from {@code source} to root
   */
  @VisibleForTesting
  FrameTransform transformToRoot(FrameName source) {
    FrameTransform result = getLatest(source);
    if (result == null) {
      return null;
    }
    while (true) {
      FrameTransform resultToParent = lookUp(result.getTargetFrame(), result.getTime());
      if (resultToParent == null) {
        return result;
      }
      // Now resultToParent.getSourceFrame() == result.getTargetFrame()
      Transform transform = resultToParent.getTransform().multiply(result.getTransform());
      FrameName target = resultToParent.getTargetFrame();
      result = new FrameTransform(transform, source, target, result.getTime());
    }
  }
}
