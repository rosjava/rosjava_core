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

import org.junit.Before;
import org.junit.Test;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.exception.RemoteException;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.service.ServiceException;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.service.test_ros.AddTwoInts;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest {

  private static final String SERVICE_NAME = "/add_two_ints";
  private static final String SERVICE_TYPE = "test_ros/AddTwoInts";

  private MasterServer masterServer;
  private NodeConfiguration nodeConfiguration;
  private NodeFactory nodeFactory;

  @Before
  public void setUp() {
    masterServer = new MasterServer(BindAddress.newPublic(), AdvertiseAddress.newPublic());
    masterServer.start();
    nodeConfiguration = NodeConfiguration.newPrivate(masterServer.getUri());
    nodeFactory = new DefaultNodeFactory();
  }

  @Test
  public void pesistentServiceConnectionTest() throws Exception {
    nodeConfiguration.setNodeName("/server");
    Node serverNode = nodeFactory.newNode(nodeConfiguration);
    CountDownServiceServerListener<AddTwoInts.Request, AddTwoInts.Response> serviceServerListener =
        CountDownServiceServerListener.create();
    ServiceServer<AddTwoInts.Request, AddTwoInts.Response> serviceServer =
        serverNode.newServiceServer(SERVICE_NAME, SERVICE_TYPE,
            new ServiceResponseBuilder<AddTwoInts.Request, AddTwoInts.Response>() {
              @Override
              public AddTwoInts.Response build(AddTwoInts.Request request) {
                AddTwoInts.Response response = new AddTwoInts.Response();
                response.sum = request.a + request.b;
                return response;
              }
            });
    serviceServer.addListener(serviceServerListener);
    assertTrue(serviceServerListener.awaitRegistration(1, TimeUnit.SECONDS));

    nodeConfiguration.setNodeName("/client");
    Node clientNode = nodeFactory.newNode(nodeConfiguration);
    ServiceClient<AddTwoInts.Request, AddTwoInts.Response> client =
        clientNode.newServiceClient(SERVICE_NAME, SERVICE_TYPE);

    // TODO(damonkohler): This is a hack that we should remove once it's
    // possible to block on a connection being established.
    Thread.sleep(100);

    AddTwoInts.Request request = new AddTwoInts.Request();
    request.a = 2;
    request.b = 2;
    final CountDownLatch latch = new CountDownLatch(1);
    client.call(request, new ServiceResponseListener<AddTwoInts.Response>() {
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
  public void requestFailureTest() throws Exception {
    final String errorMessage = "Error!";
    nodeConfiguration.setNodeName("/server");
    Node serverNode = nodeFactory.newNode(nodeConfiguration);
    CountDownServiceServerListener<AddTwoInts.Request, AddTwoInts.Response> serviceServerListener =
        CountDownServiceServerListener.create();
    ServiceServer<AddTwoInts.Request, AddTwoInts.Response> serviceServer =
        serverNode.newServiceServer(SERVICE_NAME, SERVICE_TYPE,
            new ServiceResponseBuilder<AddTwoInts.Request, AddTwoInts.Response>() {
              @Override
              public AddTwoInts.Response build(AddTwoInts.Request request) throws ServiceException {
                throw new ServiceException(errorMessage);
              }
            });
    serviceServer.addListener(serviceServerListener);
    assertTrue(serviceServerListener.awaitRegistration(1, TimeUnit.SECONDS));

    nodeConfiguration.setNodeName("/client");
    Node clientNode = nodeFactory.newNode(nodeConfiguration);
    ServiceClient<AddTwoInts.Request, AddTwoInts.Response> client =
        clientNode.newServiceClient(SERVICE_NAME, SERVICE_TYPE);

    // TODO(damonkohler): This is a hack that we should remove once it's
    // possible to block on a connection being established.
    Thread.sleep(100);

    AddTwoInts.Request request = new AddTwoInts.Request();
    final CountDownLatch latch = new CountDownLatch(1);
    client.call(request, new ServiceResponseListener<AddTwoInts.Response>() {
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
