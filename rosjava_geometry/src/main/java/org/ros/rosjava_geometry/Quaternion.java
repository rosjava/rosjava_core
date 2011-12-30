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

/**
 * Representation of a Quaternion.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 * 
 */
public class Quaternion {
  private double x;
  private double y;
  private double z;
  private double w;

  public Quaternion(double x, double y, double z, double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public double getAngle() {
    return 2 * Math.acos(w);
  }

  public Vector3 getAxis() {
    double length = Math.sqrt(1 - w * w);
    
    if (length > 1e-9) {
      return new Vector3(x / length, y / length, z / length);
    }
    return new Vector3(0, 0, 0);
  }

  public Quaternion invert() {
    return new Quaternion(-x, -y, -z, w);
  }

  public Quaternion multiply(Quaternion other) {
    return new Quaternion(w * other.x + x * other.w + y * other.z - z * other.y, w * other.y + y
        * other.w + z * other.x - x * other.z, w * other.z + z * other.w + x * other.y - y
        * other.x, w * other.w - x * other.x - y * other.y - z * other.z);
  }

  public Vector3 rotateVector(Vector3 vector) {
    double vectorLength = vector.length();
    Quaternion vectorQuaternion =
        new Quaternion(vector.getX() / vectorLength, vector.getY() / vectorLength, vector.getZ()
            / vectorLength, 0);
    Quaternion rotatedQuaternion = multiply(vectorQuaternion.multiply(invert()));
    return new Vector3(rotatedQuaternion.getX() * vectorLength, rotatedQuaternion.getY()
        * vectorLength, rotatedQuaternion.getZ() * vectorLength);
  }

  public org.ros.message.geometry_msgs.Quaternion toQuaternionMessage() {
    org.ros.message.geometry_msgs.Quaternion result =
        new org.ros.message.geometry_msgs.Quaternion();
    result.x = x;
    result.y = y;
    result.z = z;
    result.w = w;
    return result;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  public double getW() {
    return w;
  }

  public void setW(double w) {
    this.w = w;
  }

  public static Quaternion makeFromAxisAngle(Vector3 axis, double angle) {
    double sinFactor = Math.sin(angle / 2);
    double axisLength = axis.length();
    return new Quaternion(axis.getX() / axisLength * sinFactor, axis.getY() / axisLength
        * sinFactor, axis.getZ() / axisLength * sinFactor, Math.cos(angle / 2));
  }

  public static Quaternion makeFromQuaternionMessage(
      org.ros.message.geometry_msgs.Quaternion message) {
    return new Quaternion(message.x, message.y, message.z, message.w);
  }

  public static Quaternion rotationBetweenVectors(Vector3 vector1, Vector3 vector2) {
    double axisX = vector1.getY() * vector2.getZ() - vector1.getZ() * vector2.getY();
    double axisY = vector1.getZ() * vector2.getX() - vector1.getX() * vector2.getZ();
    double axisZ = vector1.getX() * vector2.getY() - vector1.getY() * vector2.getX();
    double length1 = vector1.length();
    double length2 = vector2.length();
    if (length1 == 0.0 || length2 == 0.0) {
      return makeFromAxisAngle(new Vector3(0, 0, 0), 0);
    }
    double angle = Math.acos(vector1.dotProduct(vector2) / (length1 * length2));
    return makeFromAxisAngle(new Vector3(axisX, axisY, axisZ), angle);
  }

  public static Quaternion makeIdentityQuaternion() {
    return new Quaternion(0, 0, 0, 1);
  }

}
