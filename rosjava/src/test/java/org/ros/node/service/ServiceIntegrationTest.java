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
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest extends RosTest {

  private static final String SERVICE_NAME = "/add_two_ints";

  @Test
  public void testPesistentServiceConnection() throws Exception {
    final CountDownServiceServerListener<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(final Node node) {
        ServiceServer<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> serviceServer =
            node.newServiceServer(
                SERVICE_NAME,
                test_ros.AddTwoInts._TYPE,
                new ServiceResponseBuilder<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response>() {
                  @Override
                  public void build(test_ros.AddTwoInts.Request request,
                      test_ros.AddTwoInts.Response response) {
                    response.setSum(request.getA() + request.getB());
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

    final CountDownLatch latch = new CountDownLatch(1);
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceClient<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> serviceClient;
        try {
          serviceClient = node.newServiceClient(SERVICE_NAME, test_ros.AddTwoInts._TYPE);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        test_ros.AddTwoInts.Request request = serviceClient.newMessage();
        request.setA(2);
        request.setB(2);
        serviceClient.call(request, new ServiceResponseListener<test_ros.AddTwoInts.Response>() {
          @Override
          public void onSuccess(test_ros.AddTwoInts.Response response) {
            assertEquals(response.getSum(), 4);
            latch.countDown();
          }

          @Override
          public void onFailure(RemoteException e) {
            throw new RuntimeException(e);
          }
        });
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

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRequestFailure() throws Exception {
    final String errorMessage = "Error!";
    final CountDownServiceServerListener<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceServer<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> serviceServer =
            node.newServiceServer(
                SERVICE_NAME,
                test_ros.AddTwoInts._TYPE,
                new ServiceResponseBuilder<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response>() {
                  @Override
                  public void build(test_ros.AddTwoInts.Request request,
                      test_ros.AddTwoInts.Response response) throws ServiceException {
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

    final CountDownLatch latch = new CountDownLatch(1);
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ServiceClient<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response> serviceClient;
        try {
          serviceClient = node.newServiceClient(SERVICE_NAME, test_ros.AddTwoInts._TYPE);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        test_ros.AddTwoInts.Request request = serviceClient.newMessage();
        serviceClient.call(request, new ServiceResponseListener<test_ros.AddTwoInts.Response>() {
          @Override
          public void onSuccess(test_ros.AddTwoInts.Response message) {
            fail();
          }

          @Override
          public void onFailure(RemoteException e) {
            assertEquals(e.getMessage(), errorMessage);
            latch.countDown();
          }
        });
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

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }
}
