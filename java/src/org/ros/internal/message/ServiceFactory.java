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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceFactory {

  private final ServiceLoader serviceLoader;
  private final MessageFactory messageFactory;
  private final DefaultedClassMap<Service.Request> requestMessageClassRegistry;
  private final DefaultedClassMap<Service.Response> responseMessageClassRegistry;
  private final Map<String, String> requestDefinitions;
  private final Map<String, String> responseDefinitions;

  public ServiceFactory(ServiceLoader serviceLoader, MessageFactory messageFactory) {
    this.serviceLoader = serviceLoader;
    this.messageFactory = messageFactory;
    requestMessageClassRegistry = new DefaultedClassMap<Service.Request>(Service.Request.class);
    responseMessageClassRegistry = new DefaultedClassMap<Service.Response>(Service.Response.class);
    requestDefinitions = Maps.newConcurrentMap();
    responseDefinitions = Maps.newConcurrentMap();
  }

  public Service createService(String serviceName) {
    if (!requestDefinitions.containsKey(serviceName)) {
      Preconditions.checkState(!responseDefinitions.containsKey(serviceName));
      addServiceDefinition(serviceName);
    }
    Service.Request request =
        messageFactory.createMessage(serviceName, requestDefinitions.get(serviceName),
            requestMessageClassRegistry.get(serviceName));
    Service.Response response =
        messageFactory.createMessage(serviceName, responseDefinitions.get(serviceName),
            responseMessageClassRegistry.get(serviceName));
    return new Service(request, response);
  }

  private void addServiceDefinition(String serviceName) {
    String serviceDefinition = serviceLoader.getServiceDefinition(serviceName);
    BufferedReader reader = new BufferedReader(new StringReader(serviceDefinition));
    StringBuilder request = new StringBuilder();
    StringBuilder response = new StringBuilder();
    StringBuilder current = request;
    String line;
    try {
      line = reader.readLine();
      while (line != null) {
        if (line.trim().equals("---")) {
          Preconditions.checkState(current == request);
          current = response;
        } else {
          current.append(line);
        }
        line = reader.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    requestDefinitions.put(serviceName, request.toString());
    responseDefinitions.put(serviceName, response.toString());
  }

}
