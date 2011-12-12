package org.ros.rosjava_geometry;

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
}
