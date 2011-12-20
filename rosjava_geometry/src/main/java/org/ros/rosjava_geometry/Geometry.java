package org.ros.rosjava_geometry;

import org.ros.message.geometry_msgs.Point;
import org.ros.message.geometry_msgs.Quaternion;
import org.ros.message.geometry_msgs.Vector3;

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
}
