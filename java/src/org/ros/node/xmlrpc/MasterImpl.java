package org.ros.node.xmlrpc;

import java.util.List;

public class MasterImpl implements Master {
  
  private final org.ros.node.server.Master master;

  public MasterImpl(org.ros.node.server.Master master) {
    this.master = master;
  }

  @Override
  public List<Object> getPublishedTopics(String callerId, String subgraph) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> getSystemState(String callerId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> getUri(String callerId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> lookupNode(String callerId, String nodeName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> lookupService(String callerId, String service) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> unregisterService(String callerId, String service, String serviceApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi) {
    throw new UnsupportedOperationException();
  }

}
