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
import org.ros.exception.DuplicateServiceException;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest extends RosTest {

  private static final String SERVICE_NAME = "/add_two_ints";

  @Test
  public void testPesistentServiceConnection() throws Exception {
    final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.execute(new AbstractNodeMain() {
      @Override
      public GraphName getDefaultNodeName() {
        return GraphName.of("server");
      }

      @Override
      public void onStart(final ConnectedNode connectedNode) {
        ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer =
            connectedNode
                .newServiceServer(
                    SERVICE_NAME,
                    rosjava_test_msgs.AddTwoInts._TYPE,
                        (request, response) -> response.setSum(request.getA() + request.getB()));
        try {
          connectedNode.newServiceServer(SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE, null);
          fail();
        } catch (DuplicateServiceException e) {
          // Only one ServiceServer with a given name can be created.
        }
        serviceServer.addListener(countDownServiceServerListener);
      }
    }, nodeConfiguration);

    assertTrue(countDownServiceServerListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));

    final CountDownLatch latch = new CountDownLatch(2);
    nodeMainExecutor.execute(new AbstractNodeMain() {
      @Override
      public GraphName getDefaultNodeName() {
        return GraphName.of("client");
      }

      @Override
      public void onStart(ConnectedNode connectedNode) {
        ServiceClient<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceClient;
        try {
          serviceClient = connectedNode.newServiceClient(SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE);
          // Test that requesting another client for the same service returns
          // the same instance.
          ServiceClient<?, ?> duplicate =
              connectedNode.newServiceClient(SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE);
          assertEquals(serviceClient, duplicate);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        rosjava_test_msgs.AddTwoIntsRequest request = serviceClient.newMessage();
        request.setA(2);
        request.setB(2);
        serviceClient.call(request, new ServiceResponseListener<rosjava_test_msgs.AddTwoIntsResponse>() {
          @Override
          public void onSuccess(rosjava_test_msgs.AddTwoIntsResponse response) {
            assertEquals(response.getSum(), 4);
            latch.countDown();
          }

          @Override
          public void onFailure(RemoteException e) {
            throw new RuntimeException(e);
          }
        });

        // Regression test for issue 122.
        request.setA(3);
        request.setB(3);
        serviceClient.call(request, new ServiceResponseListener<rosjava_test_msgs.AddTwoIntsResponse>() {
          @Override
          public void onSuccess(rosjava_test_msgs.AddTwoIntsResponse response) {
            assertEquals(response.getSum(), 6);
            latch.countDown();
          }

          @Override
          public void onFailure(RemoteException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }, nodeConfiguration);

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRequestFailure() throws Exception {
    final String errorMessage = "Error!";
    final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
        CountDownServiceServerListener.newDefault();
    nodeMainExecutor.execute(new AbstractNodeMain() {
      @Override
      public GraphName getDefaultNodeName() {
        return GraphName.of("server");
      }

      @Override
      public void onStart(ConnectedNode connectedNode) {
        ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer =
            connectedNode
                .newServiceServer(
                    SERVICE_NAME,
                    rosjava_test_msgs.AddTwoInts._TYPE,
                        (request, response) -> {
                          throw new ServiceException(errorMessage);
                        });
        serviceServer.addListener(countDownServiceServerListener);
      }
    }, nodeConfiguration);

    assertTrue(countDownServiceServerListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));

    final CountDownLatch latch = new CountDownLatch(1);
    nodeMainExecutor.execute(new AbstractNodeMain() {
      @Override
      public GraphName getDefaultNodeName() {
        return GraphName.of("client");
      }

      @Override
      public void onStart(ConnectedNode connectedNode) {
        ServiceClient<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceClient;
        try {
          serviceClient = connectedNode.newServiceClient(SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE);
        } catch (ServiceNotFoundException e) {
          throw new RosRuntimeException(e);
        }
        rosjava_test_msgs.AddTwoIntsRequest request = serviceClient.newMessage();
        serviceClient.call(request, new ServiceResponseListener<rosjava_test_msgs.AddTwoIntsResponse>() {
          @Override
          public void onSuccess(rosjava_test_msgs.AddTwoIntsResponse message) {
            fail();
          }

          @Override
          public void onFailure(RemoteException e) {
            assertEquals(e.getMessage(), errorMessage);
            latch.countDown();
          }
        });
      }
    }, nodeConfiguration);

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }
}
