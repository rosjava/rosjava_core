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

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(NodeServer.class);

  private final InetSocketAddress address;
  private final WebServer server;

  private boolean running;

  public NodeServer(SocketAddress address) {
    // TODO(damonkohler): Since this address is the one we bind to, it doesn't
    // necessarily have the right port information.
    this.address = (InetSocketAddress) address;
    server = new WebServer(this.address.getPort(), this.address.getAddress());
    running = false;
  }

  public <T extends org.ros.internal.node.xmlrpc.Node> void start(Class<T> instanceClass, T instance)
      throws XmlRpcException, IOException, URISyntaxException {
    Preconditions.checkState(!running);
    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    phm.setRequestProcessorFactoryFactory(new NodeRequestProcessorFactoryFactory<T>(instance));
    phm.addHandler("", instanceClass);
    xmlRpcServer.setHandlerMapping(phm);
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(false);
    serverConfig.setContentLengthOptional(false);
    server.start();
    running = true;
    if (DEBUG) {
      log.info("Slave node bound to: " + getUri());
    }
  }

  void shutdown() {
    Preconditions.checkState(running);
    server.shutdown();
  }

  // TODO(damonkohler): Using getHostName() here should return the public
  // hostname without the user having to specify it. If that fails, we should
  // add hostname to the NodeServer constructor.
  public URI getUri() throws MalformedURLException, URISyntaxException {
    Preconditions.checkState(running);
    return new URL("http", address.getHostName(), server.getPort(), "").toURI();
  }

}
