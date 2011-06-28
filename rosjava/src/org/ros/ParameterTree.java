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

import org.ros.internal.exception.RemoteException;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface ParameterTree {

  Object get(String name) throws RemoteException;

  Object get(String name, Object defaultValue) throws RemoteException;

  boolean has(String name) throws RemoteException;

  void delete(String name) throws RemoteException;

  void set(String name, Object value) throws RemoteException;

  String search(String name) throws RemoteException;

  List<String> getNames() throws RemoteException;
  
  void addParameterListener(String name, ParameterListener listener);

  void removeParameterListener(String name, ParameterListener listener);

}