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

package org.ros.internal.message.new_style;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class DefaultedClassMap<T> {

  private final Map<String, Class<? extends T>> map;
  private final Class<? extends T> defaultValue;
  
  public static <T> DefaultedClassMap<T> create(Class<? extends T> defaultValue) {
    return new DefaultedClassMap<T>(defaultValue);
  }

  public DefaultedClassMap(Class<? extends T> defaultValue) {
    this.defaultValue = defaultValue;
    map = Maps.newConcurrentMap();
  }

  public void put(String key, Class<? extends T> value) {
    map.put(key, value);
  }

  public Class<? extends T> get(String key) {
    if (!map.containsKey(key)) {
      return defaultValue;
    }
    return map.get(key);
  }

}
