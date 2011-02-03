package org.ros.node.xmlrpc;

import java.util.List;

public class ParameterServerImpl implements ParameterServer {

  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#deleteParam(java.lang.String, java.lang.String)
   */
  public List<Object> deleteParam(String callerId, String key) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#setParam(java.lang.String, java.lang.String, java.lang.String)
   */
  public List<Object> setParam(String callerId, String key, String value) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#getParam(java.lang.String, java.lang.String)
   */
  public List<Object> getParam(String callerId, String key) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#searchParam(java.lang.String, java.lang.String)
   */
  public List<Object> searchParam(String callerId, String key) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#subscribeParam(java.lang.String, java.lang.String, java.lang.String)
   */
  public List<Object> subscribeParam(String callerId, String callerApi, String key) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#unsubscribeParam(java.lang.String, java.lang.String, java.lang.String)
   */
  public List<Object> unsubscribeParam(String callerId, String callerApi, String key) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#hasParam(java.lang.String, java.lang.String)
   */
  public List<Object> hasParam(String callerId, String key) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.ros.topic.xmlrpc.ParameterServer#getParamNames(java.lang.String)
   */
  public List<Object> getParamNames(String callerId) {
    return null;
  }
}
