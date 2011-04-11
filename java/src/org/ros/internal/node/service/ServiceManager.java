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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceManager {

  private final Map<String, ServiceServer> serviceServers;
  private final Map<String, ServiceClient<?>> serviceClients;

  public ServiceManager() {
    serviceServers = Maps.newConcurrentMap();
    serviceClients = Maps.newConcurrentMap();
  }

  public boolean hasServiceServer(String serviceName) {
    return serviceServers.containsKey(serviceName);
  }

  public void putServiceServer(String serviceName, ServiceServer serviceServer) {
    serviceServers.put(serviceName, serviceServer);
  }

  public ServiceServer getServiceServer(String serviceName) {
    return serviceServers.get(serviceName);
  }

  public boolean hasServiceClient(String serviceName) {
    return serviceClients.containsKey(serviceName);
  }

  public void putServiceClient(String serviceName, ServiceClient<?> serviceClient) {
    serviceClients.put(serviceName, serviceClient);
  }

  public ServiceClient<?> getServiceClient(String serviceName) {
    return serviceClients.get(serviceName);
  }

}
