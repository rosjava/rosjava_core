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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.ros.node.service.ServiceClient;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceManager {

  private final Map<String, DefaultServiceServer<?, ?>> serviceServers;
  private final Map<String, ServiceClient<?, ?>> serviceClients;
  
  private ServiceListener listener;

  public ServiceManager() {
    serviceServers = Maps.newConcurrentMap();
    serviceClients = Maps.newConcurrentMap();
  }
  
  public void setListener(ServiceListener listener) {
    this.listener = listener;
  }

  public boolean hasServer(String serviceName) {
    return serviceServers.containsKey(serviceName);
  }

  public void putServer(String serviceName, DefaultServiceServer<?, ?> serviceServer) {
    serviceServers.put(serviceName, serviceServer);
    if (listener != null) {
      listener.serviceServerAdded(serviceServer);
    }
  }

  public DefaultServiceServer<?, ?> getServer(String serviceName) {
    return serviceServers.get(serviceName);
  }

  public boolean hasClient(String serviceName) {
    return serviceClients.containsKey(serviceName);
  }

  public void putClient(String serviceName, ServiceClient<?, ?> serviceClient) {
    serviceClients.put(serviceName, serviceClient);
  }

  public ServiceClient<?, ?> getClient(String serviceName) {
    return serviceClients.get(serviceName);
  }

  public List<DefaultServiceServer<?, ?>> getServers() {
    return ImmutableList.copyOf(serviceServers.values());
  }

  public List<ServiceClient<?, ?>> getClients() {
    return ImmutableList.copyOf(serviceClients.values());
  }

}
