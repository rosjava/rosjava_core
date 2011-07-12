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

package org.ros;

import org.apache.commons.logging.Log;
import org.ros.message.std_msgs.Bool;
import org.ros.message.std_msgs.Float64;
import org.ros.message.std_msgs.Int64;
import org.ros.message.test_ros.Composite;
import org.ros.message.test_ros.TestArrays;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;

import java.util.Arrays;
import java.util.Map;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterServerTestNode implements NodeMain {

  private Node node;

  @SuppressWarnings("rawtypes")
  @Override
  public void main(NodeConfiguration nodeConfiguration) {
    node = new DefaultNodeFactory().newNode("param_client", nodeConfiguration);

    Publisher<org.ros.message.std_msgs.String> pub_tilde =
        node.newPublisher("tilde", "std_msgs/String");
    Publisher<org.ros.message.std_msgs.String> pub_string =
        node.newPublisher("string", "std_msgs/String");
    Publisher<Int64> pub_int = node.newPublisher("int", "std_msgs/Int64");
    Publisher<Bool> pub_bool = node.newPublisher("bool", "std_msgs/Bool");
    Publisher<Float64> pub_float = node.newPublisher("float", "std_msgs/Float64");
    Publisher<Composite> pub_composite = node.newPublisher("composite", "test_ros/Composite");
    Publisher<TestArrays> pub_list = node.newPublisher("list", "test_ros/TestArrays");

    ParameterTree param = node.newParameterTree();

    Log log = node.getLog();

    org.ros.message.std_msgs.String tilde_m = new org.ros.message.std_msgs.String();
    tilde_m.data = param.getString(node.resolveName("~tilde"));
    log.info("tilde: " + tilde_m.data);

    GraphName paramNamespace = new GraphName(param.getString("parameter_namespace"));
    GraphName targetNamespace = new GraphName(param.getString("target_namespace"));
    log.info("parameter_namespace: " + paramNamespace);
    log.info("target_namespace: " + targetNamespace);
    NameResolver resolver = node.getResolver().createResolver(paramNamespace);
    NameResolver setResolver = node.getResolver().createResolver(targetNamespace);

    org.ros.message.std_msgs.String string_m = new org.ros.message.std_msgs.String();
    string_m.data = param.getString(resolver.resolve("string"));
    log.info("string: " + string_m.data);
    Int64 int_m = new org.ros.message.std_msgs.Int64();
    int_m.data = param.getInteger(resolver.resolve("int"));
    log.info("int: " + int_m.data);
    Bool bool_m = new org.ros.message.std_msgs.Bool();
    bool_m.data = param.getBoolean(resolver.resolve("bool"));
    log.info("bool: " + bool_m.data);
    Float64 float_m = new org.ros.message.std_msgs.Float64();
    float_m.data = param.getDouble(resolver.resolve("float"));
    log.info("float: " + float_m.data);

    Composite composite_m = new org.ros.message.test_ros.Composite();
    Map composite_map = param.getMap(resolver.resolve("composite"));
    composite_m.a.w = (Double) ((Map) composite_map.get("a")).get("w");
    composite_m.a.x = (Double) ((Map) composite_map.get("a")).get("x");
    composite_m.a.y = (Double) ((Map) composite_map.get("a")).get("y");
    composite_m.a.z = (Double) ((Map) composite_map.get("a")).get("z");
    composite_m.b.x = (Double) ((Map) composite_map.get("b")).get("x");
    composite_m.b.y = (Double) ((Map) composite_map.get("b")).get("y");
    composite_m.b.z = (Double) ((Map) composite_map.get("b")).get("z");

    TestArrays list_m = new org.ros.message.test_ros.TestArrays();
    // only using the integer part for easier (non-float) comparison
    Object[] list = param.getList(resolver.resolve("list")).toArray();
    list_m.int32_array = new int[list.length];
    for (int i = 0; i < list.length; i++) {
      list_m.int32_array[i] = (Integer) list[i];
    }

    // Set parameters
    param.set(setResolver.resolve("string"), string_m.data);
    param.set(setResolver.resolve("int"), (int) int_m.data);
    param.set(setResolver.resolve("float"), float_m.data);
    param.set(setResolver.resolve("bool"), bool_m.data);
    param.set(setResolver.resolve("composite"), composite_map);
    param.set(setResolver.resolve("list"), Arrays.asList(list));

    while (true) {
      pub_tilde.publish(tilde_m);
      pub_string.publish(string_m);
      pub_int.publish(int_m);
      pub_bool.publish(bool_m);
      pub_float.publish(float_m);
      pub_composite.publish(composite_m);
      pub_list.publish(list_m);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
  }

  @Override
  public void shutdown() {
    node.shutdown();
  }

}
