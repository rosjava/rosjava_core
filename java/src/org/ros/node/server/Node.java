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

  public <T extends org.ros.node.xmlrpc.Node> void start(int port, Class<T> instanceClass, T instance)
      throws XmlRpcException, IOException {
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
