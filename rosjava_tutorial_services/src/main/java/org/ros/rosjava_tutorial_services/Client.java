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

import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import test_ros.AddTwoInts.Response;

/**
 * A simple {@link ServiceClient} {@link NodeMain}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Client implements NodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("rosjava_tutorial_services/client");
  }

  @Override
  public void onStart(final Node node) {
    ServiceClient<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> serviceClient;
    try {
      serviceClient = node.newServiceClient("add_two_ints", test_ros.AddTwoInts._TYPE);
    } catch (ServiceNotFoundException e) {
      throw new RosRuntimeException(e);
    }
    final test_ros.AddTwoInts.Request request = serviceClient.newMessage();
    request.setA(2);
    request.setB(2);
    serviceClient.call(request, new ServiceResponseListener<test_ros.AddTwoInts.Response>() {
      @Override
      public void onSuccess(Response response) {
        node.getLog().info(
            String.format("%d + %d = %d", request.getA(), request.getB(), response.getSum()));
      }

      @Override
      public void onFailure(RemoteException e) {
        throw new RosRuntimeException(e);
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
