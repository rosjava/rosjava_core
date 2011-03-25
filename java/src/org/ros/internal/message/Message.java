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

import org.ros.message.Duration;
import org.ros.message.Time;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Message {

  Duration getDuration(String key);

  Time getTime(String key);

  boolean getBoolean(String key);

  double getDouble(String key);

  float getFloat(String key);

  long getLong(String key);

  int getInt(String key);

  String getString(String key);

  void set(String key, Duration value);

  void set(String key, Time value);

  void set(String key, boolean value);

  void set(String key, double value);

  void set(String key, float value);

  void set(String key, long value);

  void set(String key, int value);

  void set(String key, String value);

  void set(String key, Message value);

  <MessageType extends Message> MessageType getMessage(String key, Class<MessageType> messageClass);

}
