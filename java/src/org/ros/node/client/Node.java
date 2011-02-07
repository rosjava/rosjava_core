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

package org.ros.node.client;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.URL;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 */
public class Node<T extends org.ros.node.xmlrpc.Node> {
  
  protected final T node;
  private final URL url;
  
  public Node(URL url, Class<T> interfaceClass) {
    this.url = url;
    
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);
    config.setEnabledForExtensions(true);
    config.setConnectionTimeout(60 * 1000);
    config.setReplyTimeout(60 * 1000);

    XmlRpcClient client = new XmlRpcClient();
    client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
    client.setConfig(config);

    ClientFactory factory = new ClientFactory(client);
    node = interfaceClass.cast(factory.newInstance(getClass().getClassLoader(), interfaceClass, ""));
  }

  /**
   * @return the URL address of the remote node
   */
  public URL getRemoteAddress() {
    return url;
  }

}
