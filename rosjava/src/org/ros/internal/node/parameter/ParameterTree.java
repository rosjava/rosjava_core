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
import java.util.Map;
import java.util.Vector;

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
  public void set(String name, Boolean value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Integer value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Float value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value.doubleValue());
  }

  @Override
  public void set(String name, Double value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, String value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Character value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Byte value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Short value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Long value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value.intValue());
  }

  @Override
  public void set(String name, List<?> value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Vector<?> value) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Map<?, ?> value) throws RemoteException {
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

  public Object get(String name) throws RemoteException {
    String resolvedName = resolver.resolveName(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

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
  public boolean getBoolean(String name) throws RemoteException {
    return (Boolean) get(name);
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) throws RemoteException {
    return (Boolean) get(name, defaultValue);
  }

  @Override
  public char getChar(String name) throws RemoteException {
    return (Character) get(name);
  }

  @Override
  public char getChar(String name, char defaultValue) throws RemoteException {
    return (Character) get(name, defaultValue);
  }

  @Override
  public byte getByte(String name) throws RemoteException {
    return (Byte) get(name);
  }

  @Override
  public byte getByte(String name, byte defaultValue) throws RemoteException {
    return (Byte) get(name, defaultValue);
  }

  @Override
  public short getShort(String name) throws RemoteException {
    return (Short) get(name);
  }

  @Override
  public short getShort(String name, short defaultValue) throws RemoteException {
    return (Short) get(name, defaultValue);
  }

  @Override
  public int getInteger(String name) throws RemoteException {
    return (Integer) get(name);
  }

  @Override
  public int getInteger(String name, int defaultValue) throws RemoteException {
    return (Integer) get(name, defaultValue);
  }

  @Override
  public long getLong(String name) throws RemoteException {
    return ((Integer) get(name)).longValue();
  }

  @Override
  public long getLong(String name, long defaultValue) throws RemoteException {
    return ((Integer) get(name, defaultValue)).longValue();
  }

  @Override
  public float getFloat(String name) throws RemoteException {
    return ((Double) get(name)).floatValue();
  }

  @Override
  public float getFloat(String name, float defaultValue) throws RemoteException {
    return ((Double) get(name, defaultValue)).floatValue();
  }

  @Override
  public double getDouble(String name) throws RemoteException {
    return (Double) get(name);
  }

  @Override
  public double getDouble(String name, double defaultValue) throws RemoteException {
    return (Double) get(name, defaultValue);
  }

  @Override
  public String getString(String name) throws RemoteException {
    return (String) get(name);
  }

  @Override
  public String getString(String name, String defaultValue) throws RemoteException {
    return (String) get(name, defaultValue);
  }

  @Override
  public List<?> getList(String name) throws RemoteException {
    return (List<?>) get(name);
  }

  @Override
  public List<?> getList(String name, List<?> defaultValue) throws RemoteException {
    return (List<?>) get(name, defaultValue);
  }

  @Override
  public Vector<?> getVector(String name) throws RemoteException {
    return (Vector<?>) get(name);
  }

  @Override
  public Vector<?> getVector(String name, Vector<?> defaultValue) throws RemoteException {
    return (Vector<?>) get(name, defaultValue);
  }

  @Override
  public Map<?, ?> getMap(String name) throws RemoteException {
    return (Map<?, ?>) get(name);
  }

  @Override
  public Map<?, ?> getMap(String name, Map<?, ?> defaultValue) throws RemoteException {
    return (Map<?, ?>) get(name, defaultValue);
  }

}
