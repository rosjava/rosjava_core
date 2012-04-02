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
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageFactory;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterServerTestNode implements NodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("rosjava/parameter_server_test_node");
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void onStart(Node node) {
    final Publisher<std_msgs.String> pub_tilde = node.newPublisher("tilde", std_msgs.String._TYPE);
    final Publisher<std_msgs.String> pub_string =
        node.newPublisher("string", std_msgs.String._TYPE);
    final Publisher<std_msgs.Int64> pub_int = node.newPublisher("int", "std_msgs/Int64");
    final Publisher<std_msgs.Bool> pub_bool = node.newPublisher("bool", "std_msgs/Bool");
    final Publisher<std_msgs.Float64> pub_float = node.newPublisher("float", "std_msgs/Float64");
    final Publisher<test_ros.Composite> pub_composite =
        node.newPublisher("composite", "test_ros/Composite");
    final Publisher<test_ros.TestArrays> pub_list =
        node.newPublisher("list", "test_ros/TestArrays");

    ParameterTree param = node.newParameterTree();

    Log log = node.getLog();
    MessageFactory topicMessageFactory = node.getTopicMessageFactory();

    final std_msgs.String tilde_m = topicMessageFactory.newFromType(std_msgs.String._TYPE);
    tilde_m.setString("data", param.getString(node.resolveName("~tilde").toString()));
    log.info("tilde: " + tilde_m.getString("data"));

    GraphName paramNamespace = new GraphName(param.getString("parameter_namespace"));
    GraphName targetNamespace = new GraphName(param.getString("target_namespace"));
    log.info("parameter_namespace: " + paramNamespace);
    log.info("target_namespace: " + targetNamespace);
    NameResolver resolver = node.getResolver().newChild(paramNamespace);
    NameResolver setResolver = node.getResolver().newChild(targetNamespace);

    final std_msgs.String string_m = topicMessageFactory.newFromType(std_msgs.String._TYPE);
    string_m.setString("data", param.getString(resolver.resolve("string")));
    log.info("string: " + string_m.getString("data"));
    final std_msgs.Int64 int_m = topicMessageFactory.newFromType(std_msgs.Int64._TYPE);
    int_m.setInt64("data", param.getInteger(resolver.resolve("int")));
    log.info("int: " + int_m.data());

    final std_msgs.Bool bool_m = topicMessageFactory.newFromType(std_msgs.Bool._TYPE);
    bool_m.data(param.getBoolean(resolver.resolve("bool")));
    log.info("bool: " + bool_m.data());
    final std_msgs.Float64 float_m = topicMessageFactory.newFromType(std_msgs.Float64._TYPE);
    float_m.data(param.getDouble(resolver.resolve("float")));
    log.info("float: " + float_m.data());

    final test_ros.Composite composite_m =
        topicMessageFactory.newFromType(test_ros.Composite._TYPE);
    Map composite_map = param.getMap(resolver.resolve("composite"));
    composite_m.a().w((Double) ((Map) composite_map.get("a")).get("w"));
    composite_m.a().x((Double) ((Map) composite_map.get("a")).get("x"));
    composite_m.a().y((Double) ((Map) composite_map.get("a")).get("y"));
    composite_m.a().z((Double) ((Map) composite_map.get("a")).get("z"));
    composite_m.b().x((Double) ((Map) composite_map.get("b")).get("x"));
    composite_m.b().y((Double) ((Map) composite_map.get("b")).get("y"));
    composite_m.b().z((Double) ((Map) composite_map.get("b")).get("z"));

    final test_ros.TestArrays list_m = topicMessageFactory.newFromType(test_ros.TestArrays._TYPE);
    // only using the integer part for easier (non-float) comparison
    Object[] list = param.getList(resolver.resolve("list")).toArray();
    list_m.int32_array(new ArrayList<Integer>());
    for (int i = 0; i < list.length; i++) {
      list_m.int32_array().add((Integer) list[i]);
    }

    // Set parameters
    param.set(setResolver.resolve("string"), string_m.data());
    param.set(setResolver.resolve("int"), (int) int_m.data());
    param.set(setResolver.resolve("float"), float_m.data());
    param.set(setResolver.resolve("bool"), bool_m.data());
    param.set(setResolver.resolve("composite"), composite_map);
    param.set(setResolver.resolve("list"), Arrays.asList(list));

    node.executeCancellableLoop(new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        pub_tilde.publish(tilde_m);
        pub_string.publish(string_m);
        pub_int.publish(int_m);
        pub_bool.publish(bool_m);
        pub_float.publish(float_m);
        pub_composite.publish(composite_m);
        pub_list.publish(list_m);
        Thread.sleep(100);
      }
    });
  }

  @Override
  public void onShutdown(Node node) {
  }

  @Override
  public void onShutdownComplete(Node node) {
  }
}
