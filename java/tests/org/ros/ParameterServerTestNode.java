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

import org.ros.message.test_ros.Composite;

import org.ros.message.std_msgs.Float64;

import org.ros.namespace.NameResolver;

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.node.RemoteException;
import org.ros.message.std_msgs.Bool;
import org.ros.message.std_msgs.Int64;

/**
 * This node is used in rostest end-to-end integration tests with other client
 * libraries.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterServerTestNode implements RosMain {

  @Override
  public void rosMain(String[] argv, NodeContext nodeContext) throws RosNameException,
      RosInitException {
    try {
      // Node is only used to publish results.
      final Node node;
      node = new Node("test_node", nodeContext);
      node.init();

      Publisher<org.ros.message.std_msgs.String> pub_string = node.createPublisher("string",
          org.ros.message.std_msgs.String.class);
      Publisher<Int64> pub_int = node.createPublisher("int", org.ros.message.std_msgs.Int64.class);
      Publisher<Bool> pub_bool = node.createPublisher("bool", org.ros.message.std_msgs.Bool.class);
      Publisher<Float64> pub_float = node.createPublisher("float",
          org.ros.message.std_msgs.Float64.class);

      ParameterClient param = node.createParameterClient();

      String paramNamespace = (String) param.getParam("parameterNamespace");
      NameResolver resolver = node.getResolver().createResolver(paramNamespace);

      org.ros.message.std_msgs.String string_m = new org.ros.message.std_msgs.String();
      string_m.data = (String) param.getParam(resolver.resolveName("string"));
      Int64 int_m = new org.ros.message.std_msgs.Int64();
      int_m.data = (Integer) param.getParam(resolver.resolveName("int"));
      Bool bool_m = new org.ros.message.std_msgs.Bool();
      bool_m.data = (Boolean) param.getParam(resolver.resolveName("bool"));
      Float64 float_m = new org.ros.message.std_msgs.Float64();
      float_m.data = (Float) param.getParam(resolver.resolveName("float"));

      Composite composite_m = new org.ros.message.test_ros.Composite();
      Object data = param.getParam(resolver.resolveName("composite"));
      
      System.out.println("data: "+data);
      
      try {
        while (true) {
          pub_string.publish(string_m);
          pub_int.publish(int_m);
          pub_bool.publish(bool_m);
          pub_float.publish(float_m);

          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
}
