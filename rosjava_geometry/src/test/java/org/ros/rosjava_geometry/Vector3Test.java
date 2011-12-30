package org.ros.rosjava_geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Vector3Test {

  @Test
  public void testAdd() {
    Vector3 vector1 = new Vector3(1, 2, 3);
    Vector3 vector2 = new Vector3(2, 3, 4);
    Vector3 result = vector1.add(vector2);
    assertEquals(result.getX(), 3, 1e-9);
    assertEquals(result.getY(), 5, 1e-9);
    assertEquals(result.getZ(), 7, 1e-9);
  }

  @Test
  public void testSubtract() {
    Vector3 vector1 = new Vector3(1, 2, 3);
    Vector3 vector2 = new Vector3(2, 3, 4);
    Vector3 result = vector1.subtract(vector2);
    assertEquals(result.getX(), -1, 1e-9);
    assertEquals(result.getY(), -1, 1e-9);
    assertEquals(result.getZ(), -1, 1e-9);
  }

  @Test
  public void testInvert() {
    Vector3 result = new Vector3(1, 1, 1).invert();
    assertEquals(result.getX(), -1, 1e-9);
    assertEquals(result.getY(), -1, 1e-9);
    assertEquals(result.getZ(), -1, 1e-9);
  }

  @Test
  public void testDotProduct() {
    Vector3 vector1 = new Vector3(1, 2, 3);
    Vector3 vector2 = new Vector3(2, 3, 4);
    assertEquals(20.0, vector1.dotProduct(vector2), 1e-9);
  }

  @Test
  public void testLength() {
    assertEquals(2, new Vector3(2, 0, 0).length(), 1e-9);
    assertEquals(2, new Vector3(0, 2, 0).length(), 1e-9);
    assertEquals(2, new Vector3(0, 0, 2).length(), 1e-9);
    assertEquals(Math.sqrt(3), new Vector3(1, 1, 1).length(), 1e-9);
  }

}
