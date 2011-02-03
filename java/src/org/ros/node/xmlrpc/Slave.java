package org.ros.node.xmlrpc;

import java.util.List;

public interface Slave extends Node {

  public List<Object> getBusStats(String callerId);

  public List<Object> getBusInfo(String callerId);

  public List<Object> getMasterUri(String callerId);

  public List<Object> shutdown(String callerId, String message);

  public List<Object> getPid(String callerId);

  public List<Object> getSubscriptions(String callerId);

  public List<Object> getPublications(String callerId);

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue);

  public List<Object> publisherUpdate(String callerId, String topic, Object[] publishers);

  public List<Object> requestTopic(String callerId, String topic, Object[] protocols);

}