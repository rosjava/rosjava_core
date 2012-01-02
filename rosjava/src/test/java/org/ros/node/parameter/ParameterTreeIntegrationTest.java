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

package org.ros.node.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.exception.ParameterClassCastException;
import org.ros.exception.ParameterNotFoundException;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterTreeIntegrationTest {

  private MasterServer masterServer;
  private Node node;
  private ParameterTree parameters;
  private NodeFactory nodeFactory;
  private NodeConfiguration nodeConfiguration;

  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.newPublic(), AdvertiseAddress.newPublic());
    masterServer.start();
    nodeFactory = new DefaultNodeFactory();
    nodeConfiguration = NodeConfiguration.newPrivate(masterServer.getUri());
    nodeConfiguration.setNodeName("node_name");
    node = nodeFactory.newNode(nodeConfiguration);
    parameters = node.newParameterTree();
  }

  @After
  public void tearDown() {
    node.shutdown();
    masterServer.shutdown();
  }

  @Test
  public void testGetNonExistentParameter() {
    try {
      parameters.getBoolean("bloop");
      fail();
    } catch (ParameterNotFoundException e) {
      // Thrown when a parameter does not exist.
    }
  }

  @Test
  public void testGetParameterOfWrongType() {
    parameters.set("bloop", "foo");
    try {
      parameters.getBoolean("bloop");
      fail();
    } catch (ParameterClassCastException e) {
      // Thrown when a parameter is of the wrong type.
    }
  }

  @Test
  public void testGetParameterWithDefault() {
    assertTrue(parameters.getBoolean("bloop", true));
    List<String> expectedList = Lists.newArrayList("foo", "bar", "baz");
    assertEquals(expectedList, parameters.getList("bloop", expectedList));
    parameters.set("bloop", expectedList);
    assertEquals(expectedList, parameters.getList("bloop", Lists.newArrayList()));
  }

  @Test
  public void testGetParameterWithDefaultOfWrongType() {
    parameters.set("bloop", "foo");
    try {
      parameters.getBoolean("bloop", true);
      fail();
    } catch (ParameterClassCastException e) {
      // Thrown when a parameter is of the wrong type.
    }
  }

  @Test
  public void testSetAndGetStrings() {
    parameters.set("/foo/bar", "baz");
    assertEquals("baz", parameters.getString("/foo/bar"));
    parameters.set("/foo/bar", "baz");
    assertEquals("baz", parameters.getString("/foo/bar"));
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("bar", "baz");
    assertEquals(expected, parameters.getMap("/foo"));
  }

  @Test
  public void testSetAndGetAllTypes() {
    String name = "/foo/bar";
    parameters.set(name, true);
    assertTrue(parameters.getBoolean(name));
    parameters.set(name, 42);
    assertEquals(42, parameters.getInteger(name));
    parameters.set(name, 0.42d);
    assertEquals(0.42d, parameters.getDouble(name), 0.01);
    parameters.set(name, "foo");
    assertEquals("foo", parameters.getString(name));
    List<String> expectedList = Lists.newArrayList("foo", "bar", "baz");
    parameters.set(name, expectedList);
    assertEquals(expectedList, parameters.getList(name));
    Map<String, String> expectedMap = Maps.newHashMap();
    expectedMap.put("foo", "bar");
    expectedMap.put("baz", "bloop");
    parameters.set(name, expectedMap);
    assertEquals(expectedMap, parameters.getMap(name));
  }

  @Test
  public void testDeleteAndHas() {
    parameters.set("/foo/bar", "baz");
    assertTrue(parameters.has("/foo/bar"));
    parameters.delete("/foo/bar");
    assertFalse(parameters.has("/foo/bar"));
  }

  @Test
  public void testGetNames() {
    parameters.set("/foo/bar", "baz");
    parameters.set("/bloop", "doh");
    Collection<GraphName> names = parameters.getNames();
    assertEquals(2, names.size());
    assertTrue(names.contains(new GraphName("/foo/bar")));
    assertTrue(names.contains(new GraphName("/bloop")));
  }

  @Test
  public void testParameterPubSub() throws InterruptedException {
    nodeConfiguration.setNodeName("subscriber");
    Node subscriberNode = nodeFactory.newNode(nodeConfiguration);
    nodeConfiguration.setNodeName("publisher");
    Node publisherNode = nodeFactory.newNode(nodeConfiguration);

    ParameterTree subscriberParameters = subscriberNode.newParameterTree();
    final CountDownLatch latch = new CountDownLatch(1);
    subscriberParameters.addParameterListener("/foo/bar", new ParameterListener() {
      @Override
      public void onNewValue(Object value) {
        assertEquals(42, value);
        latch.countDown();
      }
    });

    ParameterTree publisherParameters = publisherNode.newParameterTree();
    publisherParameters.set("/foo/bar", 42);

    assertTrue(latch.await(1, TimeUnit.SECONDS));

    subscriberNode.shutdown();
    publisherNode.shutdown();
  }
}
