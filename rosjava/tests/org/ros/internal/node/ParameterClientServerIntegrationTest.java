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

import org.ros.internal.namespace.DefaultNameResolver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.ParameterListener;
import org.ros.ParameterTree;
import org.ros.internal.namespace.DefaultGraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterClientServerIntegrationTest {

  private MasterServer masterServer;
  private Node node;
  private ParameterTree parameters;

  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    masterServer.start();
    node = Node.createPrivate(new DefaultGraphName("/node_name"), masterServer.getUri(), 0, 0);
    parameters = node.createParameterTree(DefaultNameResolver.createDefault());
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
    Node subscriber = Node.createPrivate(new DefaultGraphName("/subscriber"), masterServer.getUri(), 0, 0);
    Node publisher = Node.createPrivate(new DefaultGraphName("/publisher"), masterServer.getUri(), 0, 0);

    ParameterTree subscriberParameters =
        subscriber.createParameterTree(DefaultNameResolver.createDefault());
    final CountDownLatch latch = new CountDownLatch(1);
    subscriberParameters.addParameterListener("/foo/bar", new ParameterListener() {
      @Override
      public void onNewValue(Object value) {
        assertEquals(42, value);
        latch.countDown();
      }
    });

    ParameterTree publisherParameters =
        publisher.createParameterTree(DefaultNameResolver.createDefault());
    publisherParameters.set("/foo/bar", 42);

    assertTrue(latch.await(1, TimeUnit.SECONDS));

    subscriber.shutdown();
    publisher.shutdown();
  }

}
