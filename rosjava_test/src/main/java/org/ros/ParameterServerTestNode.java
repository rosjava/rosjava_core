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
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;

import java.util.List;
import java.util.Map;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterServerTestNode extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("rosjava/parameter_server_test_node");
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void onStart(ConnectedNode connectedNode) {
    final Publisher<std_msgs.String> pub_tilde =
        connectedNode.newPublisher("tilde", std_msgs.String._TYPE);
    final Publisher<std_msgs.String> pub_string =
        connectedNode.newPublisher("string", std_msgs.String._TYPE);
    final Publisher<std_msgs.Int64> pub_int =
        connectedNode.newPublisher("int", std_msgs.Int64._TYPE);
    final Publisher<std_msgs.Bool> pub_bool =
        connectedNode.newPublisher("bool", std_msgs.Bool._TYPE);
    final Publisher<std_msgs.Float64> pub_float =
        connectedNode.newPublisher("float", std_msgs.Float64._TYPE);
    final Publisher<test_ros.Composite> pub_composite =
        connectedNode.newPublisher("composite", test_ros.Composite._TYPE);
    final Publisher<test_ros.TestArrays> pub_list =
        connectedNode.newPublisher("list", test_ros.TestArrays._TYPE);

    ParameterTree param = connectedNode.getParameterTree();

    Log log = connectedNode.getLog();
    MessageFactory topicMessageFactory = connectedNode.getTopicMessageFactory();

    final std_msgs.String tilde_m = topicMessageFactory.newFromType(std_msgs.String._TYPE);
    tilde_m.setData(param.getString(connectedNode.resolveName("~tilde").toString()));
    log.info("tilde: " + tilde_m.getData());

    GraphName paramNamespace = new GraphName(param.getString("parameter_namespace"));
    GraphName targetNamespace = new GraphName(param.getString("target_namespace"));
    log.info("parameter_namespace: " + paramNamespace);
    log.info("target_namespace: " + targetNamespace);
    NameResolver resolver = connectedNode.getResolver().newChild(paramNamespace);
    NameResolver setResolver = connectedNode.getResolver().newChild(targetNamespace);

    final std_msgs.String string_m = topicMessageFactory.newFromType(std_msgs.String._TYPE);
    string_m.setData(param.getString(resolver.resolve("string")));
    log.info("string: " + string_m.getData());
    final std_msgs.Int64 int_m = topicMessageFactory.newFromType(std_msgs.Int64._TYPE);
    int_m.setData(param.getInteger(resolver.resolve("int")));
    log.info("int: " + int_m.getData());

    final std_msgs.Bool bool_m = topicMessageFactory.newFromType(std_msgs.Bool._TYPE);
    bool_m.setData(param.getBoolean(resolver.resolve("bool")));
    log.info("bool: " + bool_m.getData());
    final std_msgs.Float64 float_m = topicMessageFactory.newFromType(std_msgs.Float64._TYPE);
    float_m.setData(param.getDouble(resolver.resolve("float")));
    log.info("float: " + float_m.getData());

    final test_ros.Composite composite_m =
        topicMessageFactory.newFromType(test_ros.Composite._TYPE);
    Map composite_map = param.getMap(resolver.resolve("composite"));
    composite_m.getA().setW((Double) ((Map) composite_map.get("a")).get("w"));
    composite_m.getA().setX((Double) ((Map) composite_map.get("a")).get("x"));
    composite_m.getA().setY((Double) ((Map) composite_map.get("a")).get("y"));
    composite_m.getA().setZ((Double) ((Map) composite_map.get("a")).get("z"));
    composite_m.getB().setX((Double) ((Map) composite_map.get("b")).get("x"));
    composite_m.getB().setY((Double) ((Map) composite_map.get("b")).get("y"));
    composite_m.getB().setZ((Double) ((Map) composite_map.get("b")).get("z"));

    final test_ros.TestArrays list_m = topicMessageFactory.newFromType(test_ros.TestArrays._TYPE);
    // only using the integer part for easier (non-float) comparison
    @SuppressWarnings("unchecked")
    List<Integer> list = (List<Integer>) param.getList(resolver.resolve("list"));
    int[] data = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      data[i] = list.get(i);
    }
    list_m.setInt32Array(data);

    // Set parameters
    param.set(setResolver.resolve("string"), string_m.getData());
    param.set(setResolver.resolve("int"), (int) int_m.getData());
    param.set(setResolver.resolve("float"), float_m.getData());
    param.set(setResolver.resolve("bool"), bool_m.getData());
    param.set(setResolver.resolve("composite"), composite_map);
    param.set(setResolver.resolve("list"), list);

    connectedNode.executeCancellableLoop(new CancellableLoop() {
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
}
