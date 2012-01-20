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

package org.ros.node.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ros.RosTest;
import org.ros.concurrent.Holder;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.node.service.ServiceException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.service.test_ros.AddTwoInts;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest extends RosTest {

  private static final String SERVICE_NAME = "/add_two_ints";
  private static final String SERVICE_TYPE = "test_ros/AddTwoInts";

  @Test
  public void testPesistentServiceConnection() throws Exception {
    final CountDownServiceServerListener<AddTwoInts.Request, AddTwoInts.Response> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.executeNodeMain(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceServer<AddTwoInts.Request, AddTwoInts.Response> serviceServer =
            node.newServiceServer(SERVICE_NAME, SERVICE_TYPE,
                new ServiceResponseBuilder<AddTwoInts.Request, AddTwoInts.Response>() {
                  @Override
                  public AddTwoInts.Response build(AddTwoInts.Request request) {
                    AddTwoInts.Response response = new AddTwoInts.Response();
                    response.sum = request.a + request.b;
                    return response;
                  }
                });
        serviceServer.addListener(countDownServiceServerListener);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("server");
      }
    }, nodeConfiguration);

    countDownServiceServerListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS);

    final Holder<ServiceClient<AddTwoInts.Request, AddTwoInts.Response>> holder = Holder.newEmpty();
    nodeMainExecutor.executeNodeMain(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceClient<AddTwoInts.Request, AddTwoInts.Response> serviceClient;
        try {
          serviceClient = node.newServiceClient(SERVICE_NAME, SERVICE_TYPE);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        holder.set(serviceClient);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("client");
      }
    }, nodeConfiguration);

    holder.await(1, TimeUnit.SECONDS);

    // TODO(damonkohler): This is a hack that we should remove once it's
    // possible to block on a connection being established.
    Thread.sleep(100);

    AddTwoInts.Request request = new AddTwoInts.Request();
    request.a = 2;
    request.b = 2;
    final CountDownLatch latch = new CountDownLatch(1);
    holder.get().call(request, new ServiceResponseListener<AddTwoInts.Response>() {
      @Override
      public void onSuccess(AddTwoInts.Response message) {
        assertEquals(message.sum, 4);
        latch.countDown();
      }

      @Override
      public void onFailure(RemoteException e) {
        throw new RuntimeException(e);
      }
    });
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRequestFailure() throws Exception {
    final String errorMessage = "Error!";
    final CountDownServiceServerListener<AddTwoInts.Request, AddTwoInts.Response> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.executeNodeMain(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceServer<AddTwoInts.Request, AddTwoInts.Response> serviceServer =
            node.newServiceServer(SERVICE_NAME, SERVICE_TYPE,
                new ServiceResponseBuilder<AddTwoInts.Request, AddTwoInts.Response>() {
                  @Override
                  public AddTwoInts.Response build(AddTwoInts.Request request)
                      throws ServiceException {
                    throw new ServiceException(errorMessage);
                  }
                });
        serviceServer.addListener(countDownServiceServerListener);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("server");
      }
    }, nodeConfiguration);

    countDownServiceServerListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS);

    final Holder<ServiceClient<AddTwoInts.Request, AddTwoInts.Response>> holder = Holder.newEmpty();
    nodeMainExecutor.executeNodeMain(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceClient<AddTwoInts.Request, AddTwoInts.Response> serviceClient;
        try {
          serviceClient = node.newServiceClient(SERVICE_NAME, SERVICE_TYPE);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        holder.set(serviceClient);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("client");
      }
    }, nodeConfiguration);

    holder.await(1, TimeUnit.SECONDS);

    // TODO(damonkohler): This is a hack that we should remove once it's
    // possible to block on a connection being established.
    Thread.sleep(100);

    AddTwoInts.Request request = new AddTwoInts.Request();
    final CountDownLatch latch = new CountDownLatch(1);
    holder.get().call(request, new ServiceResponseListener<AddTwoInts.Response>() {
      @Override
      public void onSuccess(AddTwoInts.Response message) {
        fail();
      }

      @Override
      public void onFailure(RemoteException e) {
        assertEquals(e.getMessage(), errorMessage);
        latch.countDown();
      }
    });
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }
}
