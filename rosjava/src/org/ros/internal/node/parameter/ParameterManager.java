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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.ros.node.parameter.ParameterListener;


import java.util.Collection;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterManager {

  private final Multimap<String, ParameterListener> listeners;
  
  public ParameterManager() {
    listeners = Multimaps.synchronizedMultimap(HashMultimap.<String, ParameterListener>create());
  }
  
  public void addListener(String key, ParameterListener listener) {
    listeners.put(key, listener);
  }
  
  public void removeListener(String key, ParameterListener listener) {
    listeners.remove(key, listener);
  }
  
  /**
   * @param key
   * @param value
   * @return the number of listeners called with the new value
   */
  public int updateParameter(String key, Object value) {
    int numberOfListeners = 0;
    synchronized(listeners) {
      Collection<ParameterListener> listenersForKey = listeners.get(key);
      numberOfListeners = listenersForKey.size();
      for (ParameterListener listener : listenersForKey) {
        listener.onNewValue(value);
      }
    }
    return numberOfListeners;
  }
  
}
