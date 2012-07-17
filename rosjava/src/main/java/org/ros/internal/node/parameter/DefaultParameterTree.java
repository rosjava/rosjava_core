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
import org.ros.internal.node.server.NodeIdentifier;
import org.ros.internal.node.server.ParameterServer;
import org.ros.namespace.GraphName;
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
  private final ParameterManager parameterManager;
  private final NameResolver resolver;

  public static DefaultParameterTree newFromNodeIdentifier(NodeIdentifier nodeIdentifier,
      URI masterUri, NameResolver resolver, ParameterManager parameterManager) {
    ParameterClient client = new ParameterClient(nodeIdentifier, masterUri);
    return new DefaultParameterTree(client, parameterManager, resolver);
  }

  private DefaultParameterTree(ParameterClient parameterClient, ParameterManager parameterManager,
      NameResolver resolver) {
    this.parameterClient = parameterClient;
    this.parameterManager = parameterManager;
    this.resolver = resolver;
  }

  @Override
  public boolean has(GraphName name) {
    GraphName resolvedName = resolver.resolve(name);
    return parameterClient.hasParam(resolvedName).getResult();
  }

  @Override
  public boolean has(String name) {
    return has(new GraphName(name));
  }

  @Override
  public void delete(GraphName name) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.deleteParam(resolvedName);
  }

  @Override
  public void delete(String name) {
    delete(new GraphName(name));
  }

  @Override
  public GraphName search(GraphName name) {
    GraphName resolvedName = resolver.resolve(name);
    Response<GraphName> response = parameterClient.searchParam(resolvedName);
    if (response.getStatusCode() == StatusCode.SUCCESS) {
      return response.getResult();
    } else {
      return null;
    }
  }

  @Override
  public GraphName search(String name) {
    return search(new GraphName(name));
  }

  @Override
  public List<GraphName> getNames() {
    return parameterClient.getParamNames().getResult();
  }

  @Override
  public void addParameterListener(GraphName name, ParameterListener listener) {
    parameterManager.addListener(name, listener);
    parameterClient.subscribeParam(name);
  }

  @Override
  public void addParameterListener(String name, ParameterListener listener) {
    addParameterListener(new GraphName(name), listener);
  }

  @Override
  public void set(GraphName name, boolean value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, boolean value) {
    set(new GraphName(name), value);
  }

  @Override
  public void set(GraphName name, int value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, int value) {
    set(new GraphName(name), value);
  }

  @Override
  public void set(GraphName name, double value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, double value) {
    set(new GraphName(name), value);
  }

  @Override
  public void set(GraphName name, String value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, String value) {
    set(new GraphName(name), value);
  }

  @Override
  public void set(GraphName name, List<?> value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, List<?> value) {
    set(new GraphName(name), value);
  }

  @Override
  public void set(GraphName name, Map<?, ?> value) {
    GraphName resolvedName = resolver.resolve(name);
    parameterClient.setParam(resolvedName, value);
  }

  @Override
  public void set(String name, Map<?, ?> value) {
    set(new GraphName(name), value);
  }

  private <T> T get(GraphName name, Class<T> type) {
    GraphName resolvedName = resolver.resolve(name);
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
  private <T> T get(GraphName name, T defaultValue) {
    Preconditions.checkNotNull(defaultValue);
    GraphName resolvedName = resolver.resolve(name);
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
  public boolean getBoolean(GraphName name) {
    return get(name, Boolean.class);
  }

  @Override
  public boolean getBoolean(String name) {
    return getBoolean(new GraphName(name));
  }

  @Override
  public boolean getBoolean(GraphName name, boolean defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) {
    return getBoolean(new GraphName(name), defaultValue);
  }

  @Override
  public int getInteger(GraphName name) {
    return get(name, Integer.class);
  }

  @Override
  public int getInteger(String name) {
    return getInteger(new GraphName(name));
  }

  @Override
  public int getInteger(GraphName name, int defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public int getInteger(String name, int defaultValue) {
    return getInteger(new GraphName(name), defaultValue);
  }

  @Override
  public double getDouble(GraphName name) {
    return get(name, Double.class);
  }

  @Override
  public double getDouble(String name) {
    return getDouble(new GraphName(name));
  }

  @Override
  public double getDouble(GraphName name, double defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public double getDouble(String name, double defaultValue) {
    return getDouble(new GraphName(name), defaultValue);
  }

  @Override
  public String getString(GraphName name) {
    return get(name, String.class);
  }

  @Override
  public String getString(String name) {
    return get(new GraphName(name), String.class);
  }

  @Override
  public String getString(GraphName name, String defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public String getString(String name, String defaultValue) {
    return getString(new GraphName(name), defaultValue);
  }

  @Override
  public List<?> getList(GraphName name) {
    return Arrays.asList(get(name, Object[].class));
  }

  @Override
  public List<?> getList(String name) {
    return getList(new GraphName(name));
  }

  @Override
  public List<?> getList(GraphName name, List<?> defaultValue) {
    return Arrays.asList(get(name, defaultValue.toArray()));
  }

  @Override
  public List<?> getList(String name, List<?> defaultValue) {
    return getList(new GraphName(name), defaultValue);
  }

  @Override
  public Map<?, ?> getMap(GraphName name) {
    return get(name, Map.class);
  }

  @Override
  public Map<?, ?> getMap(String name) {
    return getMap(new GraphName(name));
  }

  @Override
  public Map<?, ?> getMap(GraphName name, Map<?, ?> defaultValue) {
    return get(name, defaultValue);
  }

  @Override
  public Map<?, ?> getMap(String name, Map<?, ?> defaultValue) {
    return getMap(new GraphName(name), defaultValue);
  }
}
