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

import org.junit.Test;
import org.ros.internal.message.DefaultMessageFactory;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class FrameTransformTreeTest {

  @Test
  public void testIdentityTransforms() {
    MessageDefinitionProvider messageDefinitionProvider = new MessageDefinitionReflectionProvider();
    MessageFactory messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
    NameResolver nameResolver = NameResolver.newRoot();
    FrameTransformTree frameTransformTree = new FrameTransformTree(nameResolver);

    {
      geometry_msgs.TransformStamped message =
          messageFactory.newFromType(geometry_msgs.TransformStamped._TYPE);
      Transform transform = Transform.newIdentityTransform();
      FrameTransform frameTransform =
          new FrameTransform(transform, GraphName.of("baz"), GraphName.of("bar"));
      frameTransform.toTransformStampedMessage(new Time(), message);
      frameTransformTree.updateTransform(message);
    }

    {
      geometry_msgs.TransformStamped message =
          messageFactory.newFromType(geometry_msgs.TransformStamped._TYPE);
      Transform transform = Transform.newIdentityTransform();
      FrameTransform frameTransform =
          new FrameTransform(transform, GraphName.of("bar"), GraphName.of("foo"));
      frameTransform.toTransformStampedMessage(new Time(), message);
      frameTransformTree.updateTransform(message);
    }

    FrameTransform frameTransform =
        frameTransformTree.newFrameTransform(GraphName.of("baz"), GraphName.of("foo"));
    assertEquals(nameResolver.resolve("baz"), frameTransform.getSourceFrame());
    assertEquals(nameResolver.resolve("foo"), frameTransform.getTargetFrame());
    assertEquals(Transform.newIdentityTransform(), frameTransform.getTransform());
  }

  @Test
  public void testTransformToRoot() {
    MessageDefinitionProvider messageDefinitionProvider = new MessageDefinitionReflectionProvider();
    MessageFactory messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
    NameResolver nameResolver = NameResolver.newRoot();
    FrameTransformTree frameTransformTree = new FrameTransformTree(nameResolver);

    {
      geometry_msgs.TransformStamped message =
          messageFactory.newFromType(geometry_msgs.TransformStamped._TYPE);
      Vector3 vector = Vector3.newZeroVector();
      Quaternion quaternion = new Quaternion(Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
      Transform transform = new Transform(vector, quaternion);
      GraphName source = GraphName.of("baz");
      GraphName target = GraphName.of("bar");
      FrameTransform frameTransform = new FrameTransform(transform, source, target);
      frameTransform.toTransformStampedMessage(new Time(), message);
      frameTransformTree.updateTransform(message);
    }

    {
      geometry_msgs.TransformStamped message =
          messageFactory.newFromType(geometry_msgs.TransformStamped._TYPE);
      Vector3 vector = new Vector3(0, 1, 0);
      Quaternion quaternion = Quaternion.newIdentityQuaternion();
      Transform transform = new Transform(vector, quaternion);
      GraphName source = GraphName.of("bar");
      GraphName target = GraphName.of("foo");
      FrameTransform frameTransform = new FrameTransform(transform, source, target);
      frameTransform.toTransformStampedMessage(new Time(), message);
      frameTransformTree.updateTransform(message);
    }

    FrameTransform frameTransform =
        frameTransformTree.newFrameTransform(GraphName.of("baz"), GraphName.of("foo"));
    // If we were to reverse the order of the transforms in our implementation,
    // we would expect the translation vector to be <0, 0, 1> instead.
    Vector3 vector = new Vector3(0, 1, 0);
    Quaternion quaternion = new Quaternion(Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
    Transform transform = new Transform(vector, quaternion);
    assertEquals(nameResolver.resolve("baz"), frameTransform.getSourceFrame());
    assertEquals(nameResolver.resolve("foo"), frameTransform.getTargetFrame());
    assertEquals(transform, frameTransform.getTransform());
  }
}
