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

import org.ros.node.ParameterListener;

import org.ros.internal.node.client.ParameterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.xmlrpc.ParameterServer;
import org.ros.namespace.NameResolver;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the ROS {@link ParameterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterTree implements org.ros.node.ParameterTree {

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
  public boolean has(String name) {
    String resolvedName = resolver.resolve(name);
    return parameterClient.hasParam(resolvedName).getResult();
  }

  @Override
  public void delete(String name) {
    String resolvedName = resolver.resolve(name);
    parameterClient.deleteParam(resolvedName);
  }

  @Override
  public String search(String name) {
    String resolvedName = resolver.resolve(name);
    Response<String> response = parameterClient.searchParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public List<String> getNames() {
    return parameterClient.getParamNames().getResult();
  }

  @Override
  public void addParameterListener(String name, ParameterListener listener) {
    parameterManager.addListener(name, listener);
    parameterClient.subscribeParam(name);
  }

  @Override
  public void removeParameterListener(String name, ParameterListener listener) {
    parameterManager.removeListener(name, listener);
  }

  @Override
  public void set(String name, Boolean value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Integer value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Double value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, String value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, List<?> value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Map<?, ?> value) {
    String resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  private Object get(String name) {
    String resolvedName = resolver.resolve(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  private Object get(String name, Object defaultValue) {
    String resolvedName = resolver.resolve(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return defaultValue;
    }
  }

  @Override
  public boolean getBoolean(String name) {
    return (Boolean) get(name);
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) {
    return (Boolean) get(name, defaultValue);
  }

  @Override
  public int getInteger(String name) {
    return (Integer) get(name);
  }

  @Override
  public int getInteger(String name, int defaultValue) {
    return (Integer) get(name, defaultValue);
  }

  @Override
  public double getDouble(String name) {
    return (Double) get(name);
  }

  @Override
  public double getDouble(String name, double defaultValue) {
    return (Double) get(name, defaultValue);
  }

  @Override
  public String getString(String name) {
    return (String) get(name);
  }

  @Override
  public String getString(String name, String defaultValue) {
    return (String) get(name, defaultValue);
  }

  @Override
  public List<?> getList(String name) {
    return Arrays.asList((Object[]) get(name));
  }

  @Override
  public List<?> getList(String name, List<?> defaultValue) {
    Object possibleList = get(name, defaultValue);
    if (possibleList instanceof List<?>) {
      return (List<?>) possibleList;
    }
    return Arrays.asList((Object[]) possibleList);
  }

  @Override
  public Map<?, ?> getMap(String name) {
    return (Map<?, ?>) get(name);
  }

  @Override
  public Map<?, ?> getMap(String name, Map<?, ?> defaultValue) {
    return (Map<?, ?>) get(name, defaultValue);
  }

}
