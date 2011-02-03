package org.ros.node.server;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

class NodeRequestProcessorFactoryFactory<T extends org.ros.node.xmlrpc.Node>
    implements
      RequestProcessorFactoryFactory {
  private final RequestProcessorFactory factory = new NodeRequestProcessorFactory();
  private final T node;

  public NodeRequestProcessorFactoryFactory(T instance) {
    this.node = instance;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public RequestProcessorFactory getRequestProcessorFactory(Class unused) {
    return factory;
  }

  private class NodeRequestProcessorFactory implements RequestProcessorFactory {
    @Override
    public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest) {
      return node;
    }
  }
}
