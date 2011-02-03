package org.ros.node.xmlrpc;

import java.util.List;

public interface ParameterServer {

  public List<Object> deleteParam(String callerId, String key);

  public List<Object> setParam(String callerId, String key, String value);

  public List<Object> getParam(String callerId, String key);

  public List<Object> searchParam(String callerId, String key);

  public List<Object> subscribeParam(String callerId, String callerApi, String key);

  public List<Object> unsubscribeParam(String callerId, String callerApi, String key);

  public List<Object> hasParam(String callerId, String key);

  public List<Object> getParamNames(String callerId);

}