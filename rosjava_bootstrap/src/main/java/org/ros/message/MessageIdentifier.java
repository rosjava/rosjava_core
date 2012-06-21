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

package org.ros.message;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.ros.exception.RosRuntimeException;

import java.util.concurrent.ExecutionException;

/**
 * Uniquely identifies a message.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageIdentifier {

  private static final LoadingCache<String, MessageIdentifier> cache = CacheBuilder.newBuilder()
      .build(new CacheLoader<String, MessageIdentifier>() {
        @Override
        public MessageIdentifier load(String type) throws Exception {
          return new MessageIdentifier(type);
        }
      });

  private final String type;
  private final String pkg;
  private final String name;

  @VisibleForTesting
  public static void invalidateAll() {
    cache.invalidateAll();
  }

  public static MessageIdentifier of(String pkg, String name) {
    return of(pkg + "/" + name);
  }

  public static MessageIdentifier of(String type) {
    try {
      return cache.get(type);
    } catch (ExecutionException e) {
      throw new RosRuntimeException(e);
    }
  }

  public MessageIdentifier(String type) {
    Preconditions.checkNotNull(type);
    Preconditions.checkArgument(type.contains("/"), "Type must be fully qualified: " + type);
    this.type = type;
    String[] packageAndName = type.split("/", 2);
    pkg = packageAndName[0];
    name = packageAndName[1];
  }

  public String getType() {
    return type;
  }

  public String getPackage() {
    return pkg;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("MessageIdentifier<%s>", type);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MessageIdentifier other = (MessageIdentifier) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }
}
