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

import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.DefaultMessageFactory;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class FrameTransformTreeTest {

  private NameResolver nameResolver;
  private FrameTransformTree frameTransformTree;
  private MessageDefinitionProvider messageDefinitionProvider;
  private MessageFactory messageFactory;

  @Before
  public void before() {
    nameResolver = NameResolver.newRoot();
    frameTransformTree = new FrameTransformTree(nameResolver);
    messageDefinitionProvider = new MessageDefinitionReflectionProvider();
    messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
  }

  @Test
  public void testUpdateAndGet() {
    FrameTransform frameTransform =
        new FrameTransform(Transform.identity(), nameResolver.resolve("foo"),
            nameResolver.resolve("bar"), new Time());
    frameTransformTree.update(frameTransform);
    assertEquals(frameTransform, frameTransformTree.get("foo"));
  }

  @Test
  public void testUpdateAndGetWithTransformStampedMessage() {
    FrameTransform frameTransform =
        new FrameTransform(Transform.identity(), nameResolver.resolve("foo"),
            nameResolver.resolve("bar"), new Time());
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(), "foo", "bar",
        new Time()));
    assertEquals(frameTransform, frameTransformTree.get("foo"));
  }

  private geometry_msgs.TransformStamped newTransformStampedMessage(Transform transform,
      String source, String target, Time time) {
    geometry_msgs.TransformStamped message =
        messageFactory.newFromType(geometry_msgs.TransformStamped._TYPE);
    FrameTransform frameTransform =
        new FrameTransform(transform, nameResolver.resolve(source), nameResolver.resolve(target),
            time);
    frameTransform.toTransformStampedMessage(message);
    return message;
  }

  @Test
  public void testIdentityTransforms() {
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(), "baz", "bar",
        new Time()));
    frameTransformTree.update(newTransformStampedMessage(Transform.identity(), "bar", "foo",
        new Time()));

    // Full tree transform.
    {
      FrameTransform frameTransform = frameTransformTree.transform("baz", "foo");
      assertTrue(frameTransform != null);
      assertEquals(nameResolver.resolve("baz"), frameTransform.getSourceFrame());
      assertEquals(nameResolver.resolve("foo"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Same node transform.
    {
      FrameTransform frameTransform = frameTransformTree.transform("baz", "baz");
      assertTrue(frameTransform != null);
      assertEquals(nameResolver.resolve("baz"), frameTransform.getSourceFrame());
      assertEquals(nameResolver.resolve("baz"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Same node transform.
    {
      FrameTransform frameTransform = frameTransformTree.transform("bar", "bar");
      assertTrue(frameTransform != null);
      assertEquals(nameResolver.resolve("bar"), frameTransform.getSourceFrame());
      assertEquals(nameResolver.resolve("bar"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Root-to-root transform.
    {
      FrameTransform frameTransform = frameTransformTree.transform("foo", "foo");
      assertTrue(frameTransform != null);
      assertEquals(nameResolver.resolve("foo"), frameTransform.getSourceFrame());
      assertEquals(nameResolver.resolve("foo"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }

    // Root-to-leaf transform.
    {
      FrameTransform frameTransform = frameTransformTree.transform("foo", "baz");
      assertTrue(frameTransform != null);
      assertEquals(nameResolver.resolve("foo"), frameTransform.getSourceFrame());
      assertEquals(nameResolver.resolve("baz"), frameTransform.getTargetFrame());
      assertEquals(Transform.identity(), frameTransform.getTransform());
    }
  }

  @Test
  public void testTransformToRoot() {
    {
      Vector3 vector = Vector3.zero();
      Quaternion quaternion = new Quaternion(Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
      Transform transform = new Transform(vector, quaternion);
      frameTransformTree.update(newTransformStampedMessage(transform, "baz", "bar", new Time()));
    }

    {
      Vector3 vector = new Vector3(0, 1, 0);
      Quaternion quaternion = Quaternion.identity();
      Transform transform = new Transform(vector, quaternion);
      frameTransformTree.update(newTransformStampedMessage(transform, "bar", "foo", new Time()));
    }

    FrameTransform frameTransform = frameTransformTree.transform("baz", "foo");
    // If we were to reverse the order of the transforms in our implementation,
    // we would expect the translation vector to be <0, 0, 1> instead.
    Vector3 vector = new Vector3(0, 1, 0);
    Quaternion quaternion = new Quaternion(Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
    Transform transform = new Transform(vector, quaternion);
    assertEquals(nameResolver.resolve("baz"), frameTransform.getSourceFrame());
    assertEquals(nameResolver.resolve("foo"), frameTransform.getTargetFrame());
    assertEquals(transform, frameTransform.getTransform());
  }

  @Test
  public void testTimeTravel() {
    FrameTransform frameTransform1 =
        new FrameTransform(Transform.identity(), nameResolver.resolve("foo"),
            nameResolver.resolve("bar"), new Time());
    FrameTransform frameTransform2 =
        new FrameTransform(Transform.identity(), nameResolver.resolve("foo"),
            nameResolver.resolve("bar"), new Time(2));
    FrameTransform frameTransform3 =
        new FrameTransform(Transform.identity(), nameResolver.resolve("foo"),
            nameResolver.resolve("bar"), new Time(4));
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
