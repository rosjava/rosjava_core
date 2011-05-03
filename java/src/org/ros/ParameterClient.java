/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.namespace.NameResolver;

/**
 * Get and set values on the ROS Parameter Server.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ParameterClient {
  
  private static final boolean DEBUG = true;
  private static final Log log = LogFactory.getLog(ParameterClient.class);
  
  private final org.ros.internal.node.client.ParameterClient parameterServer;
  private final String callerId;
  private final NameResolver resolver;

  private ParameterClient(String callerId,
      org.ros.internal.node.client.ParameterClient parameterServer, NameResolver resolver) {
    this.parameterServer = parameterServer;
    this.callerId = callerId;
    this.resolver = resolver;
  }

  public static ParameterClient create(String callerId, URI masterUri,
      NameResolver resolver) throws MalformedURLException {
    org.ros.internal.node.client.ParameterClient client =
        new org.ros.internal.node.client.ParameterClient(masterUri);
    return new ParameterClient(callerId, client, resolver);
  }

  public Object getParam(String parameterName) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    if (DEBUG) {
      log.info("getParam(" + parameterName + " -> [resolved] " + resolvedName + ")");
    }
    Response<Object> response = parameterServer.getParam(this.callerId, resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  public Object getParam(String parameterName, Object defaultValue) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    if (DEBUG) {
      log.info("getParamDefault(" + parameterName + " -> [resolved] " + resolvedName + ")");
    }
    Response<Object> response = parameterServer.getParam(this.callerId, resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return defaultValue;
    }
  }

  public boolean hasParam(String parameterName) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    return parameterServer.hasParam(this.callerId, resolvedName).getResult();
  }

  public void deleteParam(String parameterName) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    if (DEBUG) {
      log.info("deleteParam(" + parameterName + " -> [resolved] " + resolvedName + ")");
    }
    parameterServer.deleteParam(this.callerId, resolvedName);
  }

  public void setParam(String parameterName, Object parameterValue) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    if (DEBUG) {
      log.info("setParam(" + parameterName + " -> [resolved] " + resolvedName + ")");
    }
    parameterServer.setParam(this.callerId, resolvedName, parameterValue);
  }

  public String searchParam(String parameterName) throws RemoteException {
    String resolvedName = resolver.resolveName(parameterName);
    Response<String> response = parameterServer.searchParam(this.callerId, resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  public List<String> getParamNames() throws RemoteException {
    return parameterServer.getParamNames(this.callerId).getResult();
  }

}
