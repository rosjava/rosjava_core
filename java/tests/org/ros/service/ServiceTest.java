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

import org.junit.Test;
import org.ros.message.Message;

import java.io.IOException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceTest {

  @Test
  public void PesistentServiceConnectionTest() throws IOException {
    org.ros.service.server.Service<org.ros.message.std.String> server =
      new org.ros.service.server.Service<org.ros.message.std.String>(
          org.ros.message.std.String.class, null, "localhost", 0) {
      @Override
      public Message buildResponse(org.ros.message.std.String message) {
        return message;
      }
    };
    org.ros.service.client.Service<org.ros.message.std.String> client = org.ros.service.client.Service
        .create(org.ros.message.std.String.class);
    client.start(server.getAddress());
    final org.ros.message.std.String message = new org.ros.message.std.String();
    message.data = "Hello, ROS!";
    client.call(message, new ServiceCallback<org.ros.message.std.String>() {
      @Override
      public void run(org.ros.message.std.String response) {
        assertEquals(message, response);
      }
    });
  }

}
