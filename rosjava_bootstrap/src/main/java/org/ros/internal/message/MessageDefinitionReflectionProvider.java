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

import org.ros.exception.RosRuntimeException;
import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageIdentifier;

import java.util.Collection;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinitionReflectionProvider implements MessageDefinitionProvider {

  private static final String DEFINITION_FIELD = "_DEFINITION";

  @Override
  public String get(String messageType) {
    try {
      String className = messageType.replace("/", ".");
      Class<?> loadedClass = getClass().getClassLoader().loadClass(className);
      return (String) loadedClass.getDeclaredField(DEFINITION_FIELD).get(null);
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }

  @Override
  public boolean has(String messageType) {
    try {
      get(messageType);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public Collection<String> getPackages() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<MessageIdentifier> getMessageIdentifiersByPackage(String pkg) {
    throw new UnsupportedOperationException();
  }
}
