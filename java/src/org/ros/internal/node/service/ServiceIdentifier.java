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

import com.google.common.collect.ImmutableMap;

import org.ros.internal.transport.ConnectionHeaderFields;


import java.net.URI;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceIdentifier {

  private final String name;
  private final URI uri;
  private final ServiceDefinition serviceDefinition;

  public ServiceIdentifier(String name, URI uri, ServiceDefinition serviceDefinition) {
    this.name = name;
    this.uri = uri;
    this.serviceDefinition = serviceDefinition;
  }

  public Map<String, String> toHeader() {
    return ImmutableMap.<String, String>builder()
        .put(ConnectionHeaderFields.SERVICE, name)
        .putAll(serviceDefinition.toHeader()).build();
  }

  public String getName() {
    return serviceDefinition.getType();
  }

  public URI getUri() {
    return uri;
  }

}
