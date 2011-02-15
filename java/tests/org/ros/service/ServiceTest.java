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

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceTest {

  @Test
  public void PesistentServiceConnectionTest() throws IOException, XmlRpcException {
    org.ros.node.server.Master masterServer = new org.ros.node.server.Master("localhost", 0);
    masterServer.start();
    org.ros.node.client.Master masterClient =
        new org.ros.node.client.Master(masterServer.getAddress());

    org.ros.node.server.Slave serverSlave =
        new org.ros.node.server.Slave("/serverSlave", masterClient, "localhost", 0);
    serverSlave.start();
    org.ros.service.server.Service<org.ros.message.srv.AddTwoInts.Request> server =
        new org.ros.service.server.Service<org.ros.message.srv.AddTwoInts.Request>(
            org.ros.message.srv.AddTwoInts.Request.class, serverSlave.toSlaveDescription(),
            AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum(), "localhost", 0) {
          @Override
          public Message buildResponse(org.ros.message.srv.AddTwoInts.Request message) {
            return message;
          }
        };
    server.start();

    org.ros.node.server.Slave clientSlave =
        new org.ros.node.server.Slave("/clientSlave", masterClient, "localhost", 0);
    clientSlave.start();
    org.ros.service.client.Service<org.ros.message.srv.AddTwoInts.Response> client =
        org.ros.service.client.Service.create(org.ros.message.srv.AddTwoInts.Response.class,
            clientSlave.toSlaveDescription(), AddTwoInts.__s_getDataType(),
            AddTwoInts.__s_getMD5Sum());
    client.start(server.getAddress());

    final org.ros.message.srv.AddTwoInts serviceMessage = new org.ros.message.srv.AddTwoInts();
    Request request = serviceMessage.createRequest();
    client.call(request, new ServiceCallback<org.ros.message.srv.AddTwoInts.Response>() {
      @Override
      public void run(org.ros.message.srv.AddTwoInts.Response response) {
        assertEquals(response.sum, 4);
      }
    });
  }

}
