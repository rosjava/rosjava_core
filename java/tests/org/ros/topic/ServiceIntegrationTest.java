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

package org.ros.topic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ros.message.Message;
import org.ros.message.srv.AddTwoInts;
import org.ros.message.srv.AddTwoInts.Request;

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIntegrationTest {

  @Test
  public void PesistentServiceConnectionTest() throws IOException {
    ServiceDefinition definition =
        new ServiceDefinition(AddTwoInts.__s_getDataType(), AddTwoInts.__s_getMD5Sum());

    ServiceServer<AddTwoInts.Request> server =
        new ServiceServer<AddTwoInts.Request>(AddTwoInts.Request.class, "/server", definition,
            "localhost", 0) {
          @Override
          public Message buildResponse(AddTwoInts.Request message) {
            return message;
          }
        };
    server.start();

    ServiceClient<AddTwoInts.Response> client = ServiceClient.create("/client", definition);
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
