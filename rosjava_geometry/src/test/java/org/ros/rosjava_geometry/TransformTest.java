/*
 * Copyright (C) 2012 Google Inc.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TransformTest {

  @Test
  public void testMultiply() {
    Transform transform1 = new Transform(new Vector3(1, 0, 0), new Quaternion(0, 0, 0, 1));
    Transform transform2 =
        new Transform(new Vector3(0, 1, 0), Quaternion.newFromAxisAngle(new Vector3(0, 0, 1),
            Math.PI / 2));

    Transform result1 = transform1.multiply(transform2);
    assertEquals(1.0, result1.getTranslation().getX(), 1e-9);
    assertEquals(1.0, result1.getTranslation().getY(), 1e-9);
    assertEquals(0.0, result1.getTranslation().getZ(), 1e-9);
    assertEquals(0.0, result1.getRotation().getX(), 1e-9);
    assertEquals(0.0, result1.getRotation().getY(), 1e-9);
    assertEquals(0.7071067811865475, result1.getRotation().getZ(), 1e-9);
    assertEquals(0.7071067811865475, result1.getRotation().getW(), 1e-9);

    Transform result2 = transform2.multiply(transform1);
    assertEquals(0.0, result2.getTranslation().getX(), 1e-9);
    assertEquals(2.0, result2.getTranslation().getY(), 1e-9);
    assertEquals(0.0, result2.getTranslation().getZ(), 1e-9);
    assertEquals(0.0, result2.getRotation().getX(), 1e-9);
    assertEquals(0.0, result2.getRotation().getY(), 1e-9);
    assertEquals(0.7071067811865475, result2.getRotation().getZ(), 1e-9);
    assertEquals(0.7071067811865475, result2.getRotation().getW(), 1e-9);
  }

  @Test
  public void testInvert() {
    Transform transform =
        new Transform(new Vector3(0, 1, 0), Quaternion.newFromAxisAngle(new Vector3(0, 0, 1),
            Math.PI / 2));
    Transform transformInverse = transform.invert();

    assertEquals(-1.0, transformInverse.getTranslation().getX(), 1e-9);
    assertEquals(0.0, transformInverse.getTranslation().getY(), 1e-9);
    assertEquals(0.0, transformInverse.getTranslation().getZ(), 1e-9);
    assertEquals(0.0, transformInverse.getRotation().getX(), 1e-9);
    assertEquals(0.0, transformInverse.getRotation().getY(), 1e-9);
    assertEquals(-0.7071067811865475, transformInverse.getRotation().getZ(), 1e-9);
    assertEquals(0.7071067811865475, transformInverse.getRotation().getW(), 1e-9);

    Transform neutral = transform.multiply(transformInverse);
    assertEquals(0.0, neutral.getTranslation().getX(), 1e-9);
    assertEquals(0.0, neutral.getTranslation().getY(), 1e-9);
    assertEquals(0.0, neutral.getTranslation().getZ(), 1e-9);
    assertEquals(0.0, neutral.getRotation().getX(), 1e-9);
    assertEquals(0.0, neutral.getRotation().getY(), 1e-9);
    assertEquals(0.0, neutral.getRotation().getZ(), 1e-9);
    assertEquals(1.0, neutral.getRotation().getW(), 1e-9);
  }
}
