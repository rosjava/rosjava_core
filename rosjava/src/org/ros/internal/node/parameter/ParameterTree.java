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

package org.ros.internal.node.parameter;

import org.ros.ParameterListener;
import org.ros.internal.exception.RemoteException;
import org.ros.internal.node.client.ParameterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.xmlrpc.ParameterServer;
import org.ros.namespace.NameResolver;

import java.net.URI;
import java.util.List;

/**
 * Provides access to the ROS {@link ParameterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterTree implements org.ros.ParameterTree {

  private final ParameterClient parameterClient;
  private final NameResolver resolver;
  private final ParameterManager parameterManager;

  public static ParameterTree create(SlaveIdentifier slaveIdentifier, URI masterUri,
      NameResolver resolver, ParameterManager parameterManager) {
    ParameterClient client =
        new org.ros.internal.node.client.ParameterClient(slaveIdentifier, masterUri);
    return new ParameterTree(client, parameterManager, resolver);
  }

  private ParameterTree(ParameterClient parameterClient, ParameterManager parameterManager,
      NameResolver resolver) {
    this.parameterClient = parameterClient;
    this.parameterManager = parameterManager;
    this.resolver = resolver;
  }

  @Override
  public Object get(String name) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public Object get(String name, Object defaultValue) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return defaultValue;
    }
  }

  @Override
  public boolean has(String name) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    return parameterClient.hasParam(resolvedName).getResult();
  }

  @Override
  public void delete(String name) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.deleteParam(resolvedName);
  }

  @Override
  public void set(String name, Object value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public String search(String name) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    Response<String> response = parameterClient.searchParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public List<String> getNames() throws RemoteException {
    return parameterClient.getParamNames().getResult();
  }

  @Override
  public void addParameterListener(String name, ParameterListener listener) {
    parameterManager.addListener(name, listener);
  }

  @Override
  public void removeParameterListener(String name, ParameterListener listener) {
    parameterManager.removeListener(name, listener);
  }

}
