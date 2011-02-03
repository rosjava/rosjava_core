package org.ros.node.xmlrpc;

import java.util.List;

public interface Master extends Node {

  public List<Object> registerService(String callerId, String service, String serviceApi,
      String callerApi);

  public List<Object> unregisterService(String callerId, String service, String serviceApi);

  public List<Object> registerSubscriber(String callerId, String topic, String topicType,
      String callerApi);

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi);

  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi);

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi);

  public List<Object> lookupNode(String callerId, String nodeName);

  public List<Object> getPublishedTopics(String callerId, String subgraph);

  public List<Object> getSystemState(String callerId);

  public List<Object> getUri(String callerId);

  public List<Object> lookupService(String callerId, String service);

}