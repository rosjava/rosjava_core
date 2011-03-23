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

package org.ros.internal.node.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.ros.MessageListener;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.Node;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest {

  private MasterServer masterServer;

  @Before
  public void setUp() throws XmlRpcException, IOException {
    masterServer = new MasterServer(BindAddress.createPublic(0), AdvertiseAddress.createPublic());
    masterServer.start();
  }

  @Test
  public void PesistentServiceConnectionTest() throws Exception {
    ServiceDefinition definition =
        new ServiceDefinition("/add_two_ints", AddTwoInts.__s_getDataType(),
            AddTwoInts.__s_getMD5Sum());

    Node serverNode = Node.createPrivate(new GraphName("/server"), masterServer.getUri(), 0, 0);
    ServiceServer<AddTwoInts.Request> server =
        serverNode.createServiceServer(definition, AddTwoInts.Request.class,
            new ServiceResponseBuilder<AddTwoInts.Request>() {
              @Override
              public Message build(AddTwoInts.Request request) {
                AddTwoInts.Response response = new AddTwoInts.Response();
                response.sum = request.a + request.b;
                return response;
              }
            });

    Node clientNode = Node.createPrivate(new GraphName("/client"), masterServer.getUri(), 0, 0);
    ServiceClient<AddTwoInts.Response> client =
        clientNode.createServiceClient(new ServiceIdentifier(server.getUri(), definition),
            AddTwoInts.Response.class);

    AddTwoInts.Request request = new AddTwoInts.Request();
    request.a = 2;
    request.b = 2;
    final CountDownLatch latch = new CountDownLatch(1);
    client.call(request, new MessageListener<AddTwoInts.Response>() {
      @Override
      public void onNewMessage(AddTwoInts.Response message) {
        assertEquals(message.sum, 4);
        latch.countDown();
      }
    });
    assertTrue(latch.await(1000, TimeUnit.SECONDS));
  }
}
