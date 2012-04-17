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

package org.ros.rosjava_tutorial_services;

import org.ros.internal.node.service.ServiceException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceServer;
import test_ros.AddTwoInts.Request;
import test_ros.AddTwoInts.Response;

/**
 * This is a simple {@link ServiceServer} {@link NodeMain}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Server implements NodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("rosjava_tutorial_services/server");
  }

  @Override
  public void onStart(Node node) {
    node.newServiceServer("add_two_ints", test_ros.AddTwoInts._TYPE,
        new ServiceResponseBuilder<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response>() {
          @Override
          public void build(Request request, Response response) throws ServiceException {
            response.setSum(request.getA() + request.getB());
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
