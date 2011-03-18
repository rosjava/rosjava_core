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

package org.ros.internal.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.topic.TopicManager;
import org.ros.internal.transport.tcp.TcpRosServer;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest {

  @Test
  public void PesistentServiceConnectionTest() throws InterruptedException, URISyntaxException {
    ServiceDefinition definition =
        new ServiceDefinition(AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum());

    ServiceServer<AddTwoInts.Request> server =
        new ServiceServer<AddTwoInts.Request>(AddTwoInts.Request.class, "/server", definition) {
          @Override
          public Message buildResponse(AddTwoInts.Request request) {
            AddTwoInts.Response response = new AddTwoInts.Response();
            response.sum = request.a + request.b;
            return response;
          }
        };
    ServiceManager serviceManager = new ServiceManager();
    serviceManager.putService("/add_two_ints", server);
    TcpRosServer tcpServer =
        new TcpRosServer(new InetSocketAddress(0), new TopicManager(), serviceManager);
    tcpServer.start();
    server.setAddress(tcpServer.getAddress());

    ServiceClient<AddTwoInts.Response> client =
        ServiceClient.create(AddTwoInts.Response.class, "/client", new ServiceIdentifier(
            "/add_two_ints", server.getUri(), definition));
    client.connect(tcpServer.getAddress());

    AddTwoInts.Request request = new AddTwoInts.Request();
    request.a = 2;
    request.b = 2;
    final CountDownLatch latch = new CountDownLatch(1);
    client.call(request, new ServiceCallback<AddTwoInts.Response>() {
      @Override
      public void run(AddTwoInts.Response response) {
        assertEquals(response.sum, 4);
        latch.countDown();
      }
    });
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

}
