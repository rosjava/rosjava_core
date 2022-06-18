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

import org.junit.Assert;
import org.junit.Test;
import org.ros.RosTest;
import org.ros.exception.*;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.ServiceClientNode;
import rosjava_test_msgs.AddTwoIntsResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest extends RosTest {

    private static final String SERVICE_NAME = "/add_two_ints";
    private static final String SERVER_NAME = "server";
    private static final String CLIENT = "client";

    @Test
    public void testPesistentServiceConnection() throws Exception {
        final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
                CountDownServiceServerListener.newDefault();
        nodeMainExecutor.execute(new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of(SERVER_NAME);
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
                    connectedNode.newServiceServer(SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE, (a, b) -> {
                    });
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
                return GraphName.of(CLIENT);
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
                final rosjava_test_msgs.AddTwoIntsRequest request = serviceClient.newMessage();
                {
//                   final rosjava_test_msgs.AddTwoIntsRequest request = serviceClient.newMessage();
                    request.setA(2);
                    request.setB(2);
                    serviceClient.call(request, new ServiceResponseListener<>() {
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
                }
                {
                    // Regression test for issue 122.
//                    final rosjava_test_msgs.AddTwoIntsRequest request = serviceClient.newMessage();
                    request.setA(3);
                    request.setB(3);
                    serviceClient.call(request, new ServiceResponseListener<>() {
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
            }
        }, nodeConfiguration);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    /**
     * Test the behaviour discussed in https://github.com/rosjava/rosjava_core/issues/272
     * Creating two service servers with the same name from the same nodes is prevented with a {@link DuplicateServiceException}
     *
     * See also this comment https://github.com/rosjava/rosjava_core/issues/272#issuecomment-1159455438
     * @throws Exception
     */
    @Test
    public void testMultipleServiceDeclarationSameNode() throws Exception {
        final AtomicInteger dualServiceDeclarationDetected = new AtomicInteger(0);
        final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
                CountDownServiceServerListener.newFromCounts(1, 0, 0, 2);

        final AbstractNodeMain node = new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of(SERVER_NAME);
            }

            @Override
            public void onStart(final ConnectedNode connectedNode) {

                try {
                    final ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer1 =
                            connectedNode.newServiceServer(
                                    SERVICE_NAME,
                                    rosjava_test_msgs.AddTwoInts._TYPE,
                                    (request, response) -> response.setSum(request.getA() + request.getB()));
                    serviceServer1.addListener(countDownServiceServerListener);
                } catch (DuplicateServiceException e) {
                    // Only one ServiceServer with a given name can be created.
                    dualServiceDeclarationDetected.incrementAndGet();
                }
                try {
                    final ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer1 =
                            connectedNode.newServiceServer(
                                    SERVICE_NAME,
                                    rosjava_test_msgs.AddTwoInts._TYPE,
                                    (request, response) -> response.setSum(request.getA() + request.getB()));
                    serviceServer1.addListener(countDownServiceServerListener);
                } catch (DuplicateServiceException e) {
                    // Only one ServiceServer with a given name can be created.
                    dualServiceDeclarationDetected.incrementAndGet();
                }


            }
        };
        this.nodeMainExecutor.execute(node, nodeConfiguration);

        assertTrue(countDownServiceServerListener.awaitMasterRegistrationSuccess(2, TimeUnit.SECONDS));
        if (dualServiceDeclarationDetected.get() != 1) {
            fail("Dual registration not detected");
        }

    }


    /**
     * Test the behaviour discussed in https://github.com/rosjava/rosjava_core/issues/272
     * Creating two service servers with the same name from two different nodes results in the first service server being called even from new clients.
     *
     * See also this comment https://github.com/rosjava/rosjava_core/issues/272#issuecomment-1159455438
     * @throws Exception
     */
    @Test
    public void testMultipleServiceDeclarationDifferentNodes() throws Exception {
        final CountDownLatch clientsCompleted = new CountDownLatch(4);
        final CountDownLatch  server1started=new CountDownLatch(1);
        final CountDownLatch  server2started=new CountDownLatch(1);
        final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
                CountDownServiceServerListener.newFromCounts(1, 0, 0, 2);

        final AbstractNodeMain node1 = new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of(SERVER_NAME + 1);
            }

            @Override
            public void onStart(final ConnectedNode connectedNode) {
                try {
                    final ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer1 =
                            connectedNode.newServiceServer(
                                    SERVICE_NAME,
                                    rosjava_test_msgs.AddTwoInts._TYPE,
                                    (request, response) -> response.setSum(1));
                    serviceServer1.addListener(countDownServiceServerListener);
                } catch (DuplicateServiceException e) {
                    // Only one ServiceServer with a given name can be created.

                }
                server1started.countDown();
            }


        };
        this.nodeMainExecutor.execute(node1, nodeConfiguration);
        server1started.await();
        final ServiceClientNode<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> client1 = new ServiceClientNode<>(SERVER_NAME + CLIENT + 1, SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE);

        this.nodeMainExecutor.execute(client1, nodeConfiguration);
        client1.awaitConnection();
        client1.getServiceClient().call(client1.getServiceClient().newMessage(), new ServiceResponseListener<>() {
            @Override
            public void onSuccess(AddTwoIntsResponse response) {
                Assert.assertEquals(response.getSum(), 1);
                clientsCompleted.countDown();
            }

            @Override
            public void onFailure(RemoteException e) {

            }
        });


        final AbstractNodeMain node2 = new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of(SERVER_NAME + 2);
            }

            @Override
            public void onStart(final ConnectedNode connectedNode) {

                try {
                    final ServiceServer<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> serviceServer1 =
                            connectedNode.newServiceServer(
                                    SERVICE_NAME,
                                    rosjava_test_msgs.AddTwoInts._TYPE,
                                    (request, response) -> response.setSum(2));
                    serviceServer1.addListener(countDownServiceServerListener);
                } catch (DuplicateServiceException e) {
                    // Only one ServiceServer with a given name can be created.

                }
                server2started.countDown();
            }
        };

        this.nodeMainExecutor.execute(node2, nodeConfiguration);
        server2started.await();
        client1.getServiceClient().call(client1.getServiceClient().newMessage(), new ServiceResponseListener<>() {
            @Override
            public void onSuccess(AddTwoIntsResponse response) {
                Assert.assertEquals(response.getSum(), 1);
                clientsCompleted.countDown();
            }

            @Override
            public void onFailure(RemoteException e) {

            }
        });


        final ServiceClientNode<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> client2 = new ServiceClientNode<>(SERVER_NAME + CLIENT + 2, SERVICE_NAME, rosjava_test_msgs.AddTwoInts._TYPE);

        this.nodeMainExecutor.execute(client2, nodeConfiguration);
        client2.awaitConnection();
        client2.getServiceClient().call(client2.getServiceClient().newMessage(), new ServiceResponseListener<>() {
            @Override
            public void onSuccess(AddTwoIntsResponse response) {
                Assert.assertEquals(response.getSum(), 2);
                clientsCompleted.countDown();
            }

            @Override
            public void onFailure(RemoteException e) {

            }
        });
        client1.getServiceClient().call(client1.getServiceClient().newMessage(), new ServiceResponseListener<>() {
            @Override
            public void onSuccess(AddTwoIntsResponse response) {
                Assert.assertEquals(response.getSum(), 1);
                clientsCompleted.countDown();
            }

            @Override
            public void onFailure(RemoteException e) {

            }
        });
        clientsCompleted.await();
    }


    @Test
    public void testRequestFailure() throws Exception {
        final String errorMessage = "Error!";
        final CountDownServiceServerListener<rosjava_test_msgs.AddTwoIntsRequest, rosjava_test_msgs.AddTwoIntsResponse> countDownServiceServerListener =
                CountDownServiceServerListener.newDefault();
        nodeMainExecutor.execute(new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of(SERVER_NAME);
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
                return GraphName.of(CLIENT);
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
