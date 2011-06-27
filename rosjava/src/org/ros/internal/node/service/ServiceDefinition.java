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

import org.ros.internal.message.ServiceMessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.net.URI;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceDefinition {

  private final ServiceIdentifier identifier;
  private final ServiceMessageDefinition definition;

  public ServiceDefinition(ServiceIdentifier identifier, ServiceMessageDefinition definition) {
    this.identifier = identifier;
    this.definition = definition;
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.SERVICE, getName().toString())
        .putAll(definition.toHeader())
        .build();
  }

  public String getType() {
    return definition.getType();
  }

  public GraphName getName() {
    return identifier.getName();
  }

  @Override
  public String toString() {
    return "ServiceDefinition<" + getName().toString() + "," + definition.toString() + ">";
  }

  public String getMd5Checksum() {
    return definition.getMd5Checksum();
  }

  public URI getUri() {
    return identifier.getUri();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
    result = prime * result + ((definition == null) ? 0 : definition .hashCode());
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
    ServiceDefinition other = (ServiceDefinition) obj;
    if (identifier == null) {
      if (other.identifier != null)
        return false;
    } else if (!identifier.equals(other.identifier))
      return false;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    return true;
  }

}
