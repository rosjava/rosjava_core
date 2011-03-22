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

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.node.RemoteException;
import org.ros.message.std_msgs.Bool;
import org.ros.message.std_msgs.Float64;
import org.ros.message.std_msgs.Int64;
import org.ros.message.test_ros.Composite;
import org.ros.namespace.NameResolver;

import java.util.List;
import java.util.Map;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterServerTestNode implements NodeMain {

  @SuppressWarnings("rawtypes")
  @Override
  public void run(List<String> argv, NodeContext nodeContext) throws RosNameException,
      RosInitException {
    try {
      // Node is only used to publish results.
      final Node node = new Node("test_node", nodeContext);

      Publisher<org.ros.message.std_msgs.String> pub_tilde = node.createPublisher("tilde",
          org.ros.message.std_msgs.String.class);
      Publisher<org.ros.message.std_msgs.String> pub_string = node.createPublisher("string",
          org.ros.message.std_msgs.String.class);
      Publisher<Int64> pub_int = node.createPublisher("int", org.ros.message.std_msgs.Int64.class);
      Publisher<Bool> pub_bool = node.createPublisher("bool", org.ros.message.std_msgs.Bool.class);
      Publisher<Float64> pub_float = node.createPublisher("float",
          org.ros.message.std_msgs.Float64.class);
      Publisher<Composite> pub_composite = node.createPublisher("composite",
          org.ros.message.test_ros.Composite.class);
      
      ParameterClient param = node.createParameterClient();

      org.ros.message.std_msgs.String tilde_m = new org.ros.message.std_msgs.String();
      tilde_m.data = (String) param.getParam(node.resolveName("~tilde"));
      
      String paramNamespace = (String) param.getParam("parameter_namespace");
      node.getLog().info("parameter_namespace: "+paramNamespace);
      NameResolver resolver = node.getResolver().createResolver(paramNamespace);

      org.ros.message.std_msgs.String string_m = new org.ros.message.std_msgs.String();
      string_m.data = (String) param.getParam(resolver.resolveName("string"));
      Int64 int_m = new org.ros.message.std_msgs.Int64();
      int_m.data = (Integer) param.getParam(resolver.resolveName("int"));
      Bool bool_m = new org.ros.message.std_msgs.Bool();
      bool_m.data = (Boolean) param.getParam(resolver.resolveName("bool"));
      Float64 float_m = new org.ros.message.std_msgs.Float64();
      float_m.data = (Double) param.getParam(resolver.resolveName("float"));

      Composite composite_m = new org.ros.message.test_ros.Composite();
      Map data = (Map) param.getParam(resolver.resolveName("composite"));
      composite_m.a.w = (Double) ((Map)data.get("a")).get("w");
      composite_m.a.x = (Double) ((Map)data.get("a")).get("x");
      composite_m.a.y = (Double) ((Map)data.get("a")).get("y");
      composite_m.a.z = (Double) ((Map)data.get("a")).get("z");
      composite_m.b.x = (Double) ((Map)data.get("b")).get("x");
      composite_m.b.y = (Double) ((Map)data.get("b")).get("y");
      composite_m.b.z = (Double) ((Map)data.get("b")).get("z");
      
      try {
        while (true) {
          pub_tilde.publish(tilde_m);
          pub_string.publish(string_m);
          pub_int.publish(int_m);
          pub_bool.publish(bool_m);
          pub_float.publish(float_m);
          pub_composite.publish(composite_m);
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
  
}
