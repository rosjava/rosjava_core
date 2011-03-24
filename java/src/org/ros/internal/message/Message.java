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

package org.ros.internal.message;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.ros.message.Duration;
import org.ros.message.Time;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Message implements FieldType {

  private final ImmutableMap<String, Object> constantFieldValues;
  private final ImmutableMap<String, Short> valueFieldTypes;
  private final Map<String, Object> valueFieldValues;

  public Message(ImmutableMap<String, Short> valueFieldTypes,
      ImmutableMap<String, Object> constantFieldValues) {
    this.constantFieldValues = constantFieldValues;
    this.valueFieldTypes = valueFieldTypes;
    valueFieldValues = Maps.newConcurrentMap();
  }

  public void set(String key, String value) {
    if (valueFieldTypes.get(key) != STRING) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, int value) {
    short type = valueFieldTypes.get(key);
    if (type != INT8 && type != UINT8 && type != INT16 && type != UINT16 && type != INT32
        && type != UINT32) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, long value) {
    short type = valueFieldTypes.get(key);
    if (type != INT64 && type != UINT64) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, float value) {
    if (valueFieldTypes.get(key) != FLOAT32) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, double value) {
    if (valueFieldTypes.get(key) != FLOAT64) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, boolean value) {
    if (valueFieldTypes.get(key) != BOOL) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, Time value) {
    if (valueFieldTypes.get(key) != TIME) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public void set(String key, Duration value) {
    if (valueFieldTypes.get(key) != DURATION) {
      throw new RuntimeException();
    }
    valueFieldValues.put(key, value);
  }

  public String getString(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof String) {
      return (String) constantFieldValues.get(key);
    }
    if (valueFieldValues.containsKey(key) && valueFieldTypes.get(key) == STRING) {
      return (String) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public int getInt(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Integer) {
      return (Integer) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key)
        && (type == INT8 || type == UINT8 || type == INT16 || type == UINT16 || type == INT32 || type == UINT32)) {
      return (Integer) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public long getLong(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Long) {
      return (Long) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && (type == INT64 || type == UINT64)) {
      return (Long) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public float getFloat(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Float) {
      return (Float) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && type == FLOAT32) {
      return (Float) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public double getDouble(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Double) {
      return (Double) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && type == FLOAT64) {
      return (Double) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public boolean getBoolean(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Boolean) {
      return (Boolean) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && type == BOOL) {
      return (Boolean) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public Time getTime(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Time) {
      return (Time) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && type == TIME) {
      return (Time) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

  public Duration getDuration(String key) {
    if (constantFieldValues.containsKey(key) && constantFieldValues.get(key) instanceof Duration) {
      return (Duration) constantFieldValues.get(key);
    }
    Short type = valueFieldTypes.get(key);
    if (valueFieldValues.containsKey(key) && type == DURATION) {
      return (Duration) valueFieldValues.get(key);
    }
    throw new RuntimeException();
  }

}
