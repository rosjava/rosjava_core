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

import org.ros.message.Time;
import org.ros.namespace.GraphName;

/**
 * A transformation in terms of translation and rotation.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 */
public class Transform {

  private Vector3 translation;
  private Quaternion rotation;

  public Transform(Vector3 translation, Quaternion rotation) {
    this.setTranslation(translation);
    this.setRotation(rotation);
  }

  public Transform multiply(Transform other) {
    return new Transform(transformVector(other.getTranslation()),
        transformQuaternion(other.getRotation()));
  }

  public Transform invert() {
    Quaternion inverseRotation = rotation.invert();
    return new Transform(inverseRotation.rotateVector(translation.invert()), inverseRotation);
  }

  public Vector3 transformVector(Vector3 vector) {
    return translation.add(rotation.rotateVector(vector));
  }

  public Quaternion transformQuaternion(Quaternion quaternion) {
    return rotation.multiply(quaternion);
  }

  public geometry_msgs.Transform toTransformMessage(geometry_msgs.Transform result) {
    result.setTranslation(translation.toVector3Message(result.getTranslation()));
    result.setRotation(rotation.toQuaternionMessage(result.getRotation()));
    return result;
  }

  public geometry_msgs.TransformStamped toTransformStampedMessage(GraphName frame,
      GraphName childFrame, Time stamp, geometry_msgs.TransformStamped result) {
    result.getHeader().setFrameId(frame.toString());
    result.getHeader().setStamp(stamp);
    result.setChildFrameId(childFrame.toString());
    result.setTransform(toTransformMessage(result.getTransform()));
    return result;
  }

  public geometry_msgs.Pose toPoseMessage(geometry_msgs.Pose result) {
    result.setPosition(translation.toPointMessage(result.getPosition()));
    result.setOrientation(rotation.toQuaternionMessage(result.getOrientation()));
    return result;
  }

  public geometry_msgs.PoseStamped toPoseStampedMessage(GraphName frame, Time stamp,
      geometry_msgs.PoseStamped result) {
    result.getHeader().setFrameId(frame.toString());
    result.getHeader().setStamp(stamp);
    result.setPose(toPoseMessage(result.getPose()));
    return result;
  }

  public Vector3 getTranslation() {
    return translation;
  }

  public void setTranslation(Vector3 translation) {
    this.translation = translation;
  }

  public Quaternion getRotation() {
    return rotation;
  }

  public void setRotation(Quaternion rotation) {
    this.rotation = rotation;
  }

  public static Transform newFromTransformMessage(geometry_msgs.Transform message) {
    return new Transform(Vector3.newFromVector3Message(message.getTranslation()),
        Quaternion.newFromQuaternionMessage(message.getRotation()));
  }

  public static Transform newFromPoseMessage(geometry_msgs.Pose message) {
    return new Transform(Vector3.newFromPointMessage(message.getPosition()),
        Quaternion.newFromQuaternionMessage(message.getOrientation()));
  }

  public static Transform newIdentityTransform() {
    return new Transform(Vector3.newIdentityVector3(), Quaternion.newIdentityQuaternion());
  }

  @Override
  public String toString() {
    return String.format("Transform<%s, %s>", translation, rotation);
  }
}
