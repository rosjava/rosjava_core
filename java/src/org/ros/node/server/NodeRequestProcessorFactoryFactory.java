package org.ros.node.server;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

class NodeRequestProcessorFactoryFactory<T extends org.ros.node.xmlrpc.Node> implements RequestProcessorFactoryFactory {
  private final RequestProcessorFactory factory = new NodeRequestProcessorFactory();
  private final T node;

  public NodeRequestProcessorFactoryFactory(T instance) {
    this.node = instance;
  }

  @SuppressWarnings("unchecked")
  public RequestProcessorFactory getRequestProcessorFactory(Class unused) throws XmlRpcException {
    return factory;
  }

  private class NodeRequestProcessorFactory implements RequestProcessorFactory {
    public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest) throws XmlRpcException {
      return node;
    }
  }
}
