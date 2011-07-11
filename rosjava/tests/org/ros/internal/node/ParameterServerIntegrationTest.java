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

package org.ros.internal.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;

import org.ros.node.NodeConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.Ros;
import org.ros.internal.node.server.MasterServer;
import org.ros.node.Node;
import org.ros.node.ParameterListener;
import org.ros.node.ParameterTree;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterServerIntegrationTest {

  private MasterServer masterServer;
  private Node node;
  private ParameterTree parameters;

  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    masterServer.start();
    node = Ros.newNode("/node_name", NodeConfiguration.newPrivate(masterServer.getUri()));
    parameters = node.newParameterTree();
  }

  @After
  public void tearDown() {
    node.shutdown();
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
    List<String> names = parameters.getNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("/foo/bar"));
    assertTrue(names.contains("/bloop"));
  }

  @Test
  public void testParameterPubSub() throws InterruptedException {
    Node subscriberNode =
        Ros.newNode("/subscriber", NodeConfiguration.newPrivate(masterServer.getUri()));
    Node publisherNode =
        Ros.newNode("/publisher", NodeConfiguration.newPrivate(masterServer.getUri()));

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
