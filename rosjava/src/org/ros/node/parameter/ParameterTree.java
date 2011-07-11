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

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface ParameterTree {

  boolean getBoolean(String name);

  boolean getBoolean(String name, boolean defaultValue);

  int getInteger(String name);

  int getInteger(String name, int defaultValue);

  double getDouble(String name);

  double getDouble(String name, double defaultValue);

  String getString(String name);

  String getString(String name, String defaultValue);

  List<?> getList(String name);

  List<?> getList(String name, List<?> defaultValue);

  Map<?, ?> getMap(String name);

  Map<?, ?> getMap(String name, Map<?, ?> defaultValue);

  void set(String name, Boolean value);

  void set(String name, Integer value);

  void set(String name, Double value);

  void set(String name, String value);

  void set(String name, List<?> value);

  void set(String name, Map<?, ?> value);

  boolean has(String name);

  void delete(String name);

  String search(String name);

  List<String> getNames();

  void addParameterListener(String name, ParameterListener listener);

  void removeParameterListener(String name, ParameterListener listener);

}