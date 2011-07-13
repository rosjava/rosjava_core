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

import com.google.common.base.Preconditions;

import org.ros.exception.ParameterClassCastException;
import org.ros.exception.ParameterNotFoundException;
import org.ros.internal.node.client.ParameterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.xmlrpc.ParameterServer;
import org.ros.namespace.NameResolver;
import org.ros.node.parameter.ParameterListener;
import org.ros.node.parameter.ParameterTree;

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
public class DefaultParameterTree implements ParameterTree {

  private final ParameterClient parameterClient;
  private final NameResolver resolver;
  private final ParameterManager parameterManager;

  public static DefaultParameterTree create(SlaveIdentifier slaveIdentifier, URI masterUri,
      NameResolver resolver, ParameterManager parameterManager) {
    ParameterClient client =
        new org.ros.internal.node.client.ParameterClient(slaveIdentifier, masterUri);
    return new DefaultParameterTree(client, parameterManager, resolver);
  }

  private DefaultParameterTree(ParameterClient parameterClient, ParameterManager parameterManager,
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

  private <T> T get(String name, Class<T> type) {
    String resolvedName = resolver.resolve(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    try {
      if (response.getStatusCode() == StatusCode.SUCCESS) {
        return type.cast(response.getResult());
      }
    } catch (ClassCastException e) {
      throw new ParameterClassCastException("Cannot cast parameter to: " + type.getName(), e);
    }
    throw new ParameterNotFoundException("Parameter does not exist: " + name);
  }

  @SuppressWarnings("unchecked")
  private <T> T get(String name, T defaultValue) {
    Preconditions.checkNotNull(defaultValue);
    String resolvedName = resolver.resolve(name);
    Response<Object> response = parameterClient.getParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      try {
        return (T) defaultValue.getClass().cast(response.getResult());
      } catch (ClassCastException e) {
        throw new ParameterClassCastException("Cannot cast parameter to: "
            + defaultValue.getClass().getName(), e);
      }
    } else {
      return defaultValue;
    }
  }

  @Override
  public boolean getBoolean(String name) {
    return get(name, Boolean.class);
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public int getInteger(String name) {
    return get(name, Integer.class);
  }

  @Override
  public int getInteger(String name, int defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public double getDouble(String name) {
    return get(name, Double.class);
  }

  @Override
  public double getDouble(String name, double defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public String getString(String name) {
    return get(name, String.class);
  }

  @Override
  public String getString(String name, String defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public List<?> getList(String name) {
    return Arrays.asList(get(name, Object[].class));
  }

  @Override
  public List<?> getList(String name, List<?> defaultValue) {
    return Arrays.asList(get(name, defaultValue.toArray()));
  }

  @Override
  public Map<?, ?> getMap(String name) {
    return get(name, Map.class);
  }

  @Override
  public Map<?, ?> getMap(String name, Map<?, ?> defaultValue) {
    return get(name, defaultValue);
  }

}
