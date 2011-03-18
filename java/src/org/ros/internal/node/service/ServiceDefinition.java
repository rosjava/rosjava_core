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

package org.ros.internal.node.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.ros.internal.transport.ConnectionHeaderFields;


import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceDefinition {

  private final String name;
  private final String type;
  private final String md5Checksum;
  
  public ServiceDefinition(String name, String type, String md5Checksum) {
    this.name = name;
    this.type = type;
    this.md5Checksum = md5Checksum;
  }
  
  public Map<String, String> toHeader() {
    Preconditions.checkNotNull(md5Checksum);
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.TYPE, type)
        .put(ConnectionHeaderFields.MD5_CHECKSUM, md5Checksum)
        .build();
  }

  public String getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
}
