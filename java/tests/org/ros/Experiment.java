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
package org.ros;

import org.ros.service.ServiceClient;
import org.ros.service.ServiceDefinition;
import org.ros.service.ServiceIdentifier;

import org.ros.message.srv.AddTwoInts;
import org.ros.node.RemoteException;
import org.ros.node.Response;
import org.ros.node.client.MasterClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Experiment {

  public static void main(String[] args) throws RemoteException, URISyntaxException,
      UnknownHostException, IOException, InterruptedException {
    MasterClient master;
    try {
      master = new MasterClient(new URL("http://localhost:11311/"));
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return;
    }

    Response<URI> lookupService = Response.checkOk(master.lookupService("/foo", "add_two_ints"));
    URI uri = lookupService.getValue();
    System.out.print(uri.toString());

    ServiceClient<AddTwoInts.Response> client = ServiceClient.create(
        AddTwoInts.Response.class,
        "/foo",
        new ServiceIdentifier("/add_two_ints", uri, new ServiceDefinition(AddTwoInts
            .__s_getDataType(), AddTwoInts.__s_getMD5Sum())));
    InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    client.start(address);
    AddTwoInts.Request request = new AddTwoInts.Request();
    request.a = 3;
    request.b = 2;
    System.out.print(client.call(request).sum);
  }

}
