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

package org.ros.node.parameter;

import org.ros.exception.ParameterClassCastException;
import org.ros.exception.ParameterNotFoundException;
import org.ros.internal.node.server.ParameterServer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides access to a {@link ParameterServer}.
 * 
 * <p>
 * A parameter server is a shared, multi-variate dictionary that is accessible
 * via network APIs. Nodes use this server to store and retrieve parameters at
 * runtime. As it is not designed for high-performance, it is best used for
 * static, non-binary data such as configuration parameters. It is meant to be
 * globally viewable so that tools can easily inspect the configuration state of
 * the system and modify if necessary.
 * 
 * @see http://www.ros.org/wiki/Parameter%20Server
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface ParameterTree {

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  boolean getBoolean(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  boolean getBoolean(String name, boolean defaultValue);

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  int getInteger(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  int getInteger(String name, int defaultValue);

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  double getDouble(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  double getDouble(String name, double defaultValue);

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  String getString(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  String getString(String name, String defaultValue);

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  List<?> getList(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  List<?> getList(String name, List<?> defaultValue);

  /**
   * @param name
   *          the parameter name
   * @return the parameter value
   * @throws ParameterNotFoundException
   *           if the parameter is not found
   * @throws ParameterClassCastException
   *           if the parameter is not the expected type
   */
  Map<?, ?> getMap(String name);

  /**
   * @param name
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value or the default value if the parameter does not
   *         exist
   * @throws ParameterClassCastException
   *           if the parameter exists and is not the expected type
   */
  Map<?, ?> getMap(String name, Map<?, ?> defaultValue);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, Boolean value);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, Integer value);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, Double value);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, String value);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, List<?> value);

  /**
   * @param name
   *          the parameter name
   * @param value
   *          the value that the parameter will be set to
   */
  void set(String name, Map<?, ?> value);

  /**
   * @param name
   *          the parameter name
   * @return {@code true} if a parameter with the given name exists,
   *         {@code false} otherwise
   */
  boolean has(String name);

  /**
   * Deletes a specified parameter.
   * 
   * @param name
   *          the parameter name
   */
  void delete(String name);

  /**
   * Search for parameter key on the Parameter Server. Search starts in caller's
   * namespace and proceeds upwards through parent namespaces until the
   * {@link ParameterServer} finds a matching key.
   * 
   * @param name
   *          the parameter name to search for
   * @return the name of the found parameter or {@code null} if no matching
   *         parameter was found
   */
  String search(String name);

  /**
   * @return all known parameter names
   */
  Collection<String> getNames();

  /**
   * Subscribes to changes to the specified parameter.
   * 
   * @param name
   *          the parameter name to subscribe to
   * @param listener
   *          a {@link ParameterListener} that will be called when the
   *          subscribed parameter changes
   */
  void addParameterListener(String name, ParameterListener listener);

  /**
   * Unsubscribes from changes to the specified parameter.
   * 
   * @param name
   * @param listener
   */
  void removeParameterListener(String name, ParameterListener listener);

}