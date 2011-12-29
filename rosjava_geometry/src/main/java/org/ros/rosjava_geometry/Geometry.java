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

import org.ros.message.geometry_msgs.Point;
import org.ros.message.geometry_msgs.Quaternion;
import org.ros.message.geometry_msgs.Transform;
import org.ros.message.geometry_msgs.Vector3;

/**
 * This class contains geometry utilities that work on ROS geometry messages.
 * 
 * @author moesenle@google.com (Lorenz Moesenlechner)
 * 
 */
public class Geometry {
  public static double calculateRotationAngle(Quaternion quaternion) {
    return 2 * Math.acos(quaternion.w);
  }

  public static Vector3 calculateRotationAxis(Quaternion quaternion) {
    double length = Math.sqrt(1 - (quaternion.w * quaternion.w));
    Vector3 result = new Vector3();
    // Only calculate the axis only if the quaternion is not the identity
    // rotation.
    if (length > 1e-9) {
      result.x = quaternion.x / length;
      result.y = quaternion.y / length;
      result.z = quaternion.z / length;
    }
    return result;
  }

  public static Quaternion axisAngleToQuaternion(double x, double y, double z, double angle) {
    Quaternion quaternion = new Quaternion();
    quaternion.x = x * Math.sin(angle / 2);
    quaternion.y = y * Math.sin(angle / 2);
    quaternion.z = z * Math.sin(angle / 2);
    quaternion.w = Math.cos(angle / 2);
    return quaternion;
  }

  public static Quaternion calculateRotationBetweenVectors(Point point1, Point point2) {
    double axisX = point1.y * point2.z - point1.z * point2.y;
    double axisY = point1.z * point2.x - point1.x * point2.z;
    double axisZ = point1.x * point2.y - point1.y * point2.x;
    double length1 = vectorLength(point1);
    double length2 = vectorLength(point2);
    if (length1 == 0.0 || length2 == 0.0) {
      return axisAngleToQuaternion(0, 0, 0, 0);
    }
    double angle =
        Math.acos(dotProduct(point1, point2) / (vectorLength(point1) * vectorLength(point2)));
    return axisAngleToQuaternion(axisX, axisY, axisZ, angle);
  }

  public static double dotProduct(Point point1, Point point2) {
    return point1.x * point2.x + point1.y * point2.y + point1.z * point2.z;
  }

  public static Point vectorMinus(Point point1, Point point2) {
    Point result = new Point();
    result.x = point1.x - point2.x;
    result.y = point1.y - point2.y;
    result.z = point1.z - point2.z;
    return result;
  }

  public static double vectorLength(Point point) {
    return Math.sqrt(point.x * point.x + point.y * point.y + point.z * point.z);
  }

  public static Vector3 invertVector(Vector3 vector) {
    Vector3 result = new Vector3();
    result.x = -vector.x;
    result.y = -vector.y;
    result.z = -vector.z;
    return result;
  }

  public static Quaternion invertQuaternion(Quaternion quaternion) {
    Quaternion result = new Quaternion();
    result.x = -quaternion.x;
    result.y = -quaternion.y;
    result.z = -quaternion.z;
    result.w = quaternion.w;
    return result;
  }

  public static Transform invertTransform(Transform transform) {
    Transform result = new Transform();
    result.translation = invertVector(transform.translation);
    result.rotation = invertQuaternion(transform.rotation);
    return result;
  }
}
