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
import java.util.Map;
import java.util.Vector;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface ParameterTree {

  boolean getBoolean(String name) throws RemoteException;

  boolean getBoolean(String name, boolean defaultValue) throws RemoteException;

  char getChar(String name) throws RemoteException;

  char getChar(String name, char defaultValue) throws RemoteException;

  byte getByte(String name) throws RemoteException;

  byte getByte(String name, byte defaultValue) throws RemoteException;

  short getShort(String name) throws RemoteException;

  short getShort(String name, short defaultValue) throws RemoteException;

  int getInteger(String name) throws RemoteException;

  int getInteger(String name, int defaultValue) throws RemoteException;

  long getLong(String name) throws RemoteException;

  long getLong(String name, long defaultValue) throws RemoteException;

  float getFloat(String name) throws RemoteException;

  float getFloat(String name, float defaultValue) throws RemoteException;

  double getDouble(String name) throws RemoteException;

  double getDouble(String name, double defaultValue) throws RemoteException;

  String getString(String name) throws RemoteException;

  String getString(String name, String defaultValue) throws RemoteException;

  List<?> getList(String name) throws RemoteException;

  List<?> getList(String name, List<?> defaultValue) throws RemoteException;

  Vector<?> getVector(String name) throws RemoteException;

  Vector<?> getVector(String name, Vector<?> defaultValue) throws RemoteException;

  Map<?, ?> getMap(String name) throws RemoteException;

  Map<?, ?> getMap(String name, Map<?, ?> defaultValue) throws RemoteException;

  void set(String name, Boolean value) throws RemoteException;

  void set(String name, Character value) throws RemoteException;

  void set(String name, Byte value) throws RemoteException;

  void set(String name, Short value) throws RemoteException;

  void set(String name, Integer value) throws RemoteException;

  void set(String name, Long value) throws RemoteException;

  void set(String name, Float value) throws RemoteException;

  void set(String name, Double value) throws RemoteException;

  void set(String name, String value) throws RemoteException;

  void set(String name, List<?> value) throws RemoteException;

  void set(String name, Vector<?> value) throws RemoteException;

  void set(String name, Map<?, ?> value) throws RemoteException;

  boolean has(String name) throws RemoteException;

  void delete(String name) throws RemoteException;

  String search(String name) throws RemoteException;

  List<String> getNames() throws RemoteException;

  void addParameterListener(String name, ParameterListener listener);

  void removeParameterListener(String name, ParameterListener listener);

}