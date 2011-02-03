package org.ros.node.xmlrpc;

import java.util.List;

public class ParameterServerImpl implements ParameterServer {

  @Override
  public List<Object> deleteParam(String callerId, String key) {
    return null;
  }

  @Override
  public List<Object> setParam(String callerId, String key, String value) {
    return null;
  }

  @Override
  public List<Object> getParam(String callerId, String key) {
    return null;
  }

  @Override
  public List<Object> searchParam(String callerId, String key) {
    return null;
  }
  
  @Override
  public List<Object> subscribeParam(String callerId, String callerApi, String key) {
    return null;
  }
  
  @Override
  public List<Object> unsubscribeParam(String callerId, String callerApi, String key) {
    return null;
  }
  
  @Override
  public List<Object> hasParam(String callerId, String key) {
    return null;
  }
  
  @Override
  public List<Object> getParamNames(String callerId) {
    return null;
  }
}
