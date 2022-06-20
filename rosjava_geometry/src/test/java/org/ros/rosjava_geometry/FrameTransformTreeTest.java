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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.DefaultMessageFactory;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.namespace.GraphName;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class FrameTransformTreeTest {

  private FrameTransformTree frameTransformTree;
  private MessageDefinitionProvider messageDefinitionProvider;
  private MessageFactory messageFactory;

  @Before
  public void before() {
    this.frameTransformTree = new FrameTransformTree();
    this.messageDefinitionProvider = new MessageDefinitionReflectionProvider();
    this.messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
  }

  @After
  public void after(){
     this.frameTransformTree = null;
    this.messageDefinitionProvider = null;
    this.messageFactory = null;
  }

  @Test
  public void testUpdateAndGet() {
    final FrameTransform frameTransform = new FrameTransform(Transform.identity(),GraphName.of("foo"), GraphName.of("bar"), new Time());
    frameTransformTree.update(frameTransform);
    assertEquals(frameTransform, frameTransformTree.get("foo"));
  }

  @Test
  public void testUpdateAndGetWithTransformStampedMessage() {
    FrameTransform frameTransform = new FrameTransform(Transform.identity(),
        GraphName.of("foo"), GraphName.of("bar"), new Time());
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(),
        "foo", "bar", new Time()));
    assertEquals(frameTransform, frameTransformTree.get("foo"));
  }

  private geometry_msgs.TransformStamped newTransformStampedMessage(
      Transform transform, String source, String target, Time time) {
    geometry_msgs.TransformStamped message = messageFactory
        .newFromType(geometry_msgs.TransformStamped._TYPE);
    FrameTransform frameTransform = new FrameTransform(transform,
        GraphName.of(source), GraphName.of(target), time);
    frameTransform.toTransformStampedMessage(message);
    return message;
  }

  @Test
  public void testIdentityTransforms() {
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(),
        "baz", "bar", new Time()));
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(),
        "bar", "foo", new Time()));

    // Full tree transform.
    {
      FrameTransform frameTransform = frameTransformTree
          .transform("baz", "foo");
      assertTrue(frameTransform != null);
      assertEquals(GraphName.of("baz"), frameTransform.getSourceFrame());
      assertEquals(GraphName.of("foo"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Same node transform.
    {
      FrameTransform frameTransform = frameTransformTree
          .transform("baz", "baz");
      assertTrue(frameTransform != null);
      assertEquals(GraphName.of("baz"), frameTransform.getSourceFrame());
      assertEquals(GraphName.of("baz"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Same node transform.
    {
      FrameTransform frameTransform = frameTransformTree
          .transform("bar", "bar");
      assertTrue(frameTransform != null);
      assertEquals(GraphName.of("bar"), frameTransform.getSourceFrame());
      assertEquals(GraphName.of("bar"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Root-to-root transform.
    {
      FrameTransform frameTransform = frameTransformTree
          .transform("foo", "foo");
      assertTrue(frameTransform != null);
      assertEquals(GraphName.of("foo"), frameTransform.getSourceFrame());
      assertEquals(GraphName.of("foo"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Root-to-leaf transform.
    {
      FrameTransform frameTransform = frameTransformTree
          .transform("foo", "baz");
      assertTrue(frameTransform != null);
      assertEquals(GraphName.of("foo"), frameTransform.getSourceFrame());
      assertEquals(GraphName.of("baz"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }
  }

  /**
   * Fills the {@link FrameTransformTree} with the following frame topography:
   * 
   * <pre>
   *       foo
   *    bar   bop
   * baz         fuz
   * </pre>
   */
  private void updateFrameTransformTree() {
    {
      Transform transform = Transform.translation(0, 1, 0);
      frameTransformTree.update(newTransformStampedMessage(transform, "bar",
          "foo", new Time()));
    }
    {
      Transform transform = Transform.xRotation(Math.PI / 2);
      frameTransformTree.update(newTransformStampedMessage(transform, "baz",
          "bar", new Time()));
    }
    {
      Transform transform = Transform.translation(1, 0, 0);
      frameTransformTree.update(newTransformStampedMessage(transform, "bop",
          "foo", new Time()));
    }
    {
      Transform transform = Transform.yRotation(Math.PI / 2);
      frameTransformTree.update(newTransformStampedMessage(transform, "fuz",
          "bop", new Time()));
    }
  }

  private void checkBazToFooTransform(FrameTransform frameTransform) {
    // If we were to reverse the order of the transforms in our implementation,
    // we would expect the translation vector to be <0, 0, 1> instead.
    Transform transform = Transform.translation(0, 1, 0).multiply(
        Transform.xRotation(Math.PI / 2));
    Quaternion rotationAndScale = transform.getRotationAndScale();
    assertTrue(String.format("%s is not neutral.", rotationAndScale),
        rotationAndScale.isAlmostNeutral(1e-9));
    assertEquals(GraphName.of("baz"), frameTransform.getSourceFrame());
    assertEquals(GraphName.of("foo"), frameTransform.getTargetFrame());
    assertTrue(transform.almostEquals(frameTransform.getTransform(), 1e-9));
  }

  @Test
  public void testTransformBazToRoot() {
    updateFrameTransformTree();
    checkBazToFooTransform(frameTransformTree.transformToRoot(GraphName
        .of("baz")));
  }

  @Test
  public void testTransformBazToFoo() {
    updateFrameTransformTree();
    checkBazToFooTransform(frameTransformTree.transform("baz", "foo"));
    checkBazToFooTransform(frameTransformTree.transform("foo", "baz").invert());
  }

  private void checkFuzToFooTransform(FrameTransform frameTransform) {
    // If we were to reverse the order of the transforms in our implementation,
    // we would expect the translation vector to be <0, 0, 1> instead.
    Transform transform = Transform.translation(1, 0, 0).multiply(
        Transform.yRotation(Math.PI / 2));
    Quaternion rotationAndScale = transform.getRotationAndScale();
    assertTrue(String.format("%s is not neutral.", rotationAndScale),
        rotationAndScale.isAlmostNeutral(1e-9));
    assertEquals(GraphName.of("fuz"), frameTransform.getSourceFrame());
    assertEquals(GraphName.of("foo"), frameTransform.getTargetFrame());
    assertTrue(
        String.format("Expected %s != %s", transform,
            frameTransform.getTransform()),
        transform.almostEquals(frameTransform.getTransform(), 1e-9));
  }

  @Test
  public void testTransformFuzToRoot() {
    updateFrameTransformTree();
    checkFuzToFooTransform(frameTransformTree.transformToRoot(GraphName
        .of("fuz")));
  }

  @Test
  public void testTransformFuzToFoo() {
    updateFrameTransformTree();
    checkFuzToFooTransform(frameTransformTree.transform("fuz", "foo"));
    checkFuzToFooTransform(frameTransformTree.transform("foo", "fuz").invert());
  }

  @Test
  public void testTransformBazToFuz() {
    updateFrameTransformTree();
    FrameTransform frameTransform = frameTransformTree.transform("baz", "fuz");
    Transform transform = Transform.yRotation(Math.PI / 2).invert()
        .multiply(Transform.translation(1, 0, 0).invert())
        .multiply(Transform.translation(0, 1, 0))
        .multiply(Transform.xRotation(Math.PI / 2));
    assertTrue(transform.getRotationAndScale().isAlmostNeutral(1e-9));
    assertEquals(GraphName.of("baz"), frameTransform.getSourceFrame());
    assertEquals(GraphName.of("fuz"), frameTransform.getTargetFrame());
    assertTrue(
        String.format("Expected %s != %s", transform,
            frameTransform.getTransform()),
        transform.almostEquals(frameTransform.getTransform(), 1e-9));
  }

  @Test
  public void testTimeTravel() {
    FrameTransform frameTransform1 = new FrameTransform(Transform.identity(),
        GraphName.of("foo"), GraphName.of("bar"), new Time());
    FrameTransform frameTransform2 = new FrameTransform(Transform.identity(),
        GraphName.of("foo"), GraphName.of("bar"), new Time(2));
    FrameTransform frameTransform3 = new FrameTransform(Transform.identity(),
        GraphName.of("foo"), GraphName.of("bar"), new Time(4));
    frameTransformTree.update(frameTransform1);
    frameTransformTree.update(frameTransform2);
    frameTransformTree.update(frameTransform3);
    assertEquals(frameTransform3, frameTransformTree.get("foo"));
    assertEquals(frameTransform1, frameTransformTree.get("foo", new Time()));
    assertEquals(frameTransform1, frameTransformTree.get("foo", new Time(0.5)));
    assertEquals(frameTransform2, frameTransformTree.get("foo", new Time(1.5)));
    assertEquals(frameTransform2, frameTransformTree.get("foo", new Time(2)));
    assertEquals(frameTransform3, frameTransformTree.get("foo", new Time(10)));
  }
}
