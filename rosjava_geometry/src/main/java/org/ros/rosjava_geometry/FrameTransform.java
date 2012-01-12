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
