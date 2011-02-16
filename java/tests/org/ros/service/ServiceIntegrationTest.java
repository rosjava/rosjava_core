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

package org.ros.service;

import static org.junit.Assert.assertEquals;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;
import org.ros.message.srv.AddTwoInts.Request;
import org.ros.node.client.MasterClient;
import org.ros.node.server.MasterServer;
import org.ros.node.server.SlaveServer;
import org.ros.topic.ServiceCallback;
import org.ros.topic.ServiceClient;
import org.ros.topic.ServiceServer;

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest {

  @Test
  public void PesistentServiceConnectionTest() throws IOException, XmlRpcException {
    MasterServer masterServer = new MasterServer("localhost", 0);
    masterServer.start();
    MasterClient masterClient = new MasterClient(masterServer.getAddress());

    SlaveServer serverSlave = new SlaveServer("/serverSlave", masterClient, "localhost", 0);
    serverSlave.start();
    ServiceServer<AddTwoInts.Request> server =
        new ServiceServer<AddTwoInts.Request>(AddTwoInts.Request.class,
            serverSlave.toSlaveDescription(), AddTwoInts.__s_getDataType(),
            AddTwoInts.__s_getMD5Sum(), "localhost", 0) {
          @Override
          public Message buildResponse(AddTwoInts.Request message) {
            return message;
          }
        };
    server.start();

    SlaveServer clientSlave = new SlaveServer("/clientSlave", masterClient, "localhost", 0);
    clientSlave.start();
    ServiceClient<AddTwoInts.Response> client =
        ServiceClient.create(AddTwoInts.Response.class, clientSlave.toSlaveDescription(),
            AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum());
    client.start(server.getAddress());

    final AddTwoInts serviceMessage = new AddTwoInts();
    Request request = serviceMessage.createRequest();
    client.call(request, new ServiceCallback<AddTwoInts.Response>() {
      @Override
      public void run(AddTwoInts.Response response) {
        assertEquals(response.sum, 4);
      }
    });
  }

}
