package org.ros.rosjava_geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ros.message.geometry_msgs.Quaternion;
import org.ros.message.geometry_msgs.Vector3;

public class GeometryTest {

  @Test
  public void testCalculateRotationAngleAxis() {
    Quaternion quaternion = new Quaternion();
    Vector3 axis;

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = 0;
    quaternion.w = 1;
    assertEquals(0.0, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(0, axis.z, 1e-9);

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = 1;
    quaternion.w = 0;
    assertEquals(Math.PI, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(1, axis.z, 1e-9);

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = -0.7071067811865475;
    quaternion.w = 0.7071067811865475;
    // The actual angle is -Math.PI / 2 but this is represented by a flipped
    // rotation axis in the quaternion.
    assertEquals(Math.PI / 2, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(-1, axis.z, 1e-9);

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = 0.9238795325112867;
    quaternion.w = 0.38268343236508984;
    assertEquals(0.75 * Math.PI, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(1, axis.z, 1e-9);

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = -0.9238795325112867;
    quaternion.w = 0.38268343236508984;
    assertEquals(0.75 * Math.PI, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(-1, axis.z, 1e-9);

    quaternion.x = 0;
    quaternion.y = 0;
    quaternion.z = 0.7071067811865475;
    quaternion.w = -0.7071067811865475;
    assertEquals(1.5 * Math.PI, Geometry.calculateRotationAngle(quaternion), 1e-9);
    axis = Geometry.calculateRotationAxis(quaternion);
    assertEquals(0, axis.x, 1e-9);
    assertEquals(0, axis.y, 1e-9);
    assertEquals(1, axis.z, 1e-9);
  }

  @Test
  public void testAxisAngleToQuaternion() {
    Quaternion quaternion;

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, 0);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(0, quaternion.z, 1e-9);
    assertEquals(1, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, Math.PI);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(1, quaternion.z, 1e-9);
    assertEquals(0, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, Math.PI / 2);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(0.7071067811865475, quaternion.z, 1e-9);
    assertEquals(0.7071067811865475, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, -Math.PI / 2);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(-0.7071067811865475, quaternion.z, 1e-9);
    assertEquals(0.7071067811865475, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, 0.75 * Math.PI);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(0.9238795325112867, quaternion.z, 1e-9);
    assertEquals(0.38268343236508984, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, -0.75 * Math.PI);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(-0.9238795325112867, quaternion.z, 1e-9);
    assertEquals(0.38268343236508984, quaternion.w, 1e-9);

    quaternion = Geometry.axisAngleToQuaternion(0, 0, 1, 1.5 * Math.PI);
    assertEquals(0, quaternion.x, 1e-9);
    assertEquals(0, quaternion.y, 1e-9);
    assertEquals(0.7071067811865475, quaternion.z, 1e-9);
    assertEquals(-0.7071067811865475, quaternion.w, 1e-9);
  }
}
