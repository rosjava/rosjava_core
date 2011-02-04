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

package org.ros.node.server;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.google.common.base.Preconditions;

public abstract class Node {

  private WebServer server;

  public <T extends org.ros.node.xmlrpc.Node> void start(int port, Class<T> instanceClass,
      T instance) throws XmlRpcException, IOException {
    Preconditions.checkState(server == null);
    server = new WebServer(port);
    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    phm.setRequestProcessorFactoryFactory(new NodeRequestProcessorFactoryFactory<T>(instance));
    phm.addHandler("", instanceClass);
    xmlRpcServer.setHandlerMapping(phm);
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(false);
    serverConfig.setContentLengthOptional(false);
    server.start();
  }

  public void shutdown() {
    server.shutdown();
    server = null;
  }
}
