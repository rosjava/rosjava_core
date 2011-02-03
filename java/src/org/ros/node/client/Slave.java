package org.ros.node.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.node.Response;

public class Slave extends Node<org.ros.node.xmlrpc.Slave>{
  
  public Slave(URL url) throws XmlRpcException, IOException {
    super(url, org.ros.node.xmlrpc.Slave.class);
  }
  
  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    throw new UnsupportedOperationException();
  }

  public Response<URL> getMasterUri(String callerId) throws MalformedURLException {
    List<Object> response = node.getMasterUri(callerId);
    return new Response<URL>((Integer) response.get(0), (String) response.get(1), new URL((String) response.get(2)));
  }

  public List<Object> shutdown(String callerId, String message) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getPid(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getSubscriptions(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getPublications(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String callerId, String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  public List<Object> requestTopic(String callerId, String topic,
      Collection<Collection<String>> protocols) {
    throw new UnsupportedOperationException();
  }
}
