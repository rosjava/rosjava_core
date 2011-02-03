package org.ros.node.client;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.URL;

public abstract class Node<T extends org.ros.node.xmlrpc.Node> {
  
  protected final T node;
  
  public Node(URL url, Class<T> interfaceClass) {
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

}
