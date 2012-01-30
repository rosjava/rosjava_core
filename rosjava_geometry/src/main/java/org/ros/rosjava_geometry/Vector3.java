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
 * A three dimensional vector.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 * 
 */
public class Vector3 {

  private double x;
  private double y;
  private double z;

  public Vector3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3 add(Vector3 other) {
    return new Vector3(x + other.x, y + other.y, z + other.z);
  }

  public Vector3 subtract(Vector3 other) {
    return new Vector3(x - other.x, y - other.y, z - other.z);
  }

  public Vector3 invert() {
    return new Vector3(-x, -y, -z);
  }

  public double dotProduct(Vector3 other) {
    return x * other.x + y * other.y + z * other.z;
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }
  
  public Vector3 normalized() {
    return new Vector3(x / length(), y / length(), z / length());
  }

  public org.ros.message.geometry_msgs.Vector3 toVector3Message() {
    org.ros.message.geometry_msgs.Vector3 result = new org.ros.message.geometry_msgs.Vector3();
    result.x = x;
    result.y = y;
    result.z = z;
    return result;
  }

  public org.ros.message.geometry_msgs.Point toPointMessage() {
    org.ros.message.geometry_msgs.Point result = new org.ros.message.geometry_msgs.Point();
    result.x = x;
    result.y = y;
    result.z = z;
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

  public static Vector3 newFromVector3Message(org.ros.message.geometry_msgs.Vector3 message) {
    return new Vector3(message.x, message.y, message.z);
  }

  public static Vector3 newFromPointMessage(org.ros.message.geometry_msgs.Point message) {
    return new Vector3(message.x, message.y, message.z);
  }

  public static Vector3 newIdentityVector3() {
    return new Vector3(0, 0, 0);
  }

  @Override
  public String toString() {
    return String.format("Vector3<x: %.4f, y: %.4f, z: %.4f>", x, y, z);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
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
    Vector3 other = (Vector3) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
      return false;
    return true;
  }
}
