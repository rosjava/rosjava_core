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

package org.ros.internal.node.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(NodeServer.class);

  private final WebServer server;
  private final AdvertiseAddress advertiseAddress;

  public NodeServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress) {
    InetSocketAddress address = bindAddress.toInetSocketAddress();
    server = new WebServer(address.getPort(), address.getAddress());
    this.advertiseAddress = advertiseAddress;
    this.advertiseAddress.setPortCallable(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return server.getPort();
      }
    });
  }

  public <T extends org.ros.internal.node.xmlrpc.Node> void start(Class<T> instanceClass, T instance)
      throws XmlRpcException, IOException {
    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    phm.setRequestProcessorFactoryFactory(new NodeRequestProcessorFactoryFactory<T>(instance));
    phm.addHandler("", instanceClass);
    xmlRpcServer.setHandlerMapping(phm);
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(false);
    serverConfig.setContentLengthOptional(false);
    server.start();
    if (DEBUG) {
      log.info("Bound to: " + getUri());
    }
  }

  void shutdown() {
    server.shutdown();
  }

  public URI getUri() {
    try {
      return advertiseAddress.toUri("http");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
