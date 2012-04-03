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

/**
 * A quaternion.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 */
public class Quaternion {

  private double x;
  private double y;
  private double z;
  private double w;

  public static Quaternion newFromAxisAngle(Vector3 axis, double angle) {
    Vector3 normalized = axis.normalized();
    double sin = Math.sin(angle / 2.0d);
    double cos = Math.cos(angle / 2.0d);
    return new Quaternion(normalized.getX() * sin, normalized.getY() * sin,
        normalized.getZ() * sin, cos);
  }

  public static Quaternion newFromQuaternionMessage(geometry_msgs.Quaternion message) {
    return new Quaternion(message.x(), message.y(), message.z(), message.w());
  }

  public static Quaternion rotationBetweenVectors(Vector3 vector1, Vector3 vector2) {
    Preconditions.checkArgument(vector1.length() > 0,
        "Cannot calculate rotation between zero-length vectors.");
    Preconditions.checkArgument(vector2.length() > 0,
        "Cannot calculate rotation between zero-length vectors.");
    if (vector1.normalized().equals(vector2.normalized())) {
      return newIdentityQuaternion();
    }
    double angle = Math.acos(vector1.dotProduct(vector2) / (vector1.length() * vector2.length()));
    double axisX = vector1.getY() * vector2.getZ() - vector1.getZ() * vector2.getY();
    double axisY = vector1.getZ() * vector2.getX() - vector1.getX() * vector2.getZ();
    double axisZ = vector1.getX() * vector2.getY() - vector1.getY() * vector2.getX();
    return newFromAxisAngle(new Vector3(axisX, axisY, axisZ), angle);
  }

  public static Quaternion newIdentityQuaternion() {
    return new Quaternion(0, 0, 0, 1);
  }

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
    Quaternion vectorQuaternion = new Quaternion(vector.getX(), vector.getY(), vector.getZ(), 0);
    Quaternion rotatedQuaternion = multiply(vectorQuaternion.multiply(invert()));
    return new Vector3(rotatedQuaternion.getX(), rotatedQuaternion.getY(), rotatedQuaternion.getZ());
  }

  public geometry_msgs.Quaternion toQuaternionMessage(geometry_msgs.Quaternion result) {
    result.x(x);
    result.y(y);
    result.z(z);
    result.w(w);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(w);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Quaternion other = (Quaternion) obj;
    if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
      return false;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return String.format("Quaternion<x: %.4f, y: %.4f, z: %.4f, w: %.4f>", x, y, z, w);
  }
}
