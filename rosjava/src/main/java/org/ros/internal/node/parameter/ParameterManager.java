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

import com.google.common.collect.Maps;

import org.ros.concurrent.ListenerCollection;
import org.ros.concurrent.ListenerCollection.SignalRunnable;
import org.ros.namespace.GraphName;
import org.ros.node.parameter.ParameterListener;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ParameterManager {

  private final ExecutorService executorService;
  private final Map<GraphName, ListenerCollection<ParameterListener>> listeners;

  public ParameterManager(ExecutorService executorService) {
    this.executorService = executorService;
    listeners = Maps.newHashMap();
  }

  public void addListener(GraphName parameterName, ParameterListener listener) {
    synchronized (listeners) {
      if (!listeners.containsKey(parameterName)) {
        listeners.put(parameterName, new ListenerCollection<ParameterListener>(executorService));
      }
      listeners.get(parameterName).add(listener);
    }
  }

  public void removeListener(GraphName parameterName, ParameterListener listener) {
    synchronized (listeners) {
      if (!listeners.containsKey(parameterName)) {
        return;
      }
      ListenerCollection<ParameterListener> listenerCollection = listeners.get(parameterName);
      listenerCollection.remove(listener);
      if (listenerCollection.size() == 0) {
        listeners.remove(parameterName);
      }
    }
  }

  /**
   * @param parameterName
   * @param value
   * @return the number of listeners called with the new value
   */
  public int updateParameter(GraphName parameterName, final Object value) {
    int numberOfListeners = 0;
    synchronized (listeners) {
      if (listeners.containsKey(parameterName)) {
        ListenerCollection<ParameterListener> listenerCollection = listeners.get(parameterName);
        numberOfListeners = listenerCollection.size();
        listenerCollection.signal(new SignalRunnable<ParameterListener>() {
          @Override
          public void run(ParameterListener listener) {
            listener.onNewValue(value);
          }
        });
      }
    }
    return numberOfListeners;
  }
}
