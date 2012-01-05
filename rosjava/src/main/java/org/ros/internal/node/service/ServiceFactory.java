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

import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;
import org.ros.node.service.ServiceServerListener;

import java.util.concurrent.ExecutorService;

/**
 * A factory for {@link ServiceServer}s and {@link ServiceClient}s.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceFactory {

  private final GraphName nodeName;
  private final SlaveServer slaveServer;
  private final ServiceManager serviceManager;
  private final ExecutorService executorService;

  public ServiceFactory(GraphName nodeName, SlaveServer slaveServer, ServiceManager serviceManager,
      ExecutorService executorService) {
    this.nodeName = nodeName;
    this.slaveServer = slaveServer;
    this.serviceManager = serviceManager;
    this.executorService = executorService;
  }

  /**
   * Gets or creates a {@link DefaultServiceServer} instance.
   * {@link DefaultServiceServer}s are cached and reused per service. When a new
   * {@link DefaultServiceServer} is generated, it is registered with the
   * {@link MasterServer}.
   * 
   * @param serviceDefinition
   *          the {@link ServiceMessageDefinition} that is being served
   * @param responseBuilder
   *          the {@link ServiceResponseBuilder} that is used to build responses
   * @param serverListeners
   *          a collection of {@link ServiceServerListener} instances to be
   *          added to the server (can be {@code null}
   * 
   * @return a {@link DefaultServiceServer} instance
   */
  @SuppressWarnings("unchecked")
  public <T, S> DefaultServiceServer<T, S> newServer(
      ServiceDefinition serviceDefinition, MessageDeserializer<T> deserializer,
      MessageSerializer<S> serializer,
      ServiceResponseBuilder<T, S> responseBuilder) {
    DefaultServiceServer<T, S> serviceServer;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServer(name)) {
        serviceServer =
            (DefaultServiceServer<T, S>) serviceManager.getServer(name);
      } else {
        serviceServer =
            new DefaultServiceServer<T, S>(serviceDefinition, deserializer,
                serializer, responseBuilder, slaveServer.getTcpRosAdvertiseAddress(),
                executorService);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      slaveServer.addService(serviceServer);
    }
    return serviceServer;
  }

  /**
   * Gets or creates a {@link DefaultServiceClient} instance.
   * {@link DefaultServiceClient}s are cached and reused per service. When a new
   * {@link DefaultServiceClient} is created, it is connected to the
   * {@link DefaultServiceServer}.
   * 
   * @param <ResponseType>
   * @param serviceDefinition
   *          the {@link ServiceIdentifier} of the server
   * @return a {@link DefaultServiceClient} instance
   */
  @SuppressWarnings("unchecked")
  public <RequestType, ResponseType> DefaultServiceClient<RequestType, ResponseType> newClient(
      ServiceDefinition serviceDefinition, MessageSerializer<RequestType> serializer,
      MessageDeserializer<ResponseType> deserializer) {
    Preconditions.checkNotNull(serviceDefinition.getUri());
    DefaultServiceClient<RequestType, ResponseType> serviceClient;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasClient(name)) {
        serviceClient =
            (DefaultServiceClient<RequestType, ResponseType>) serviceManager.getClient(name);
      } else {
        serviceClient =
            DefaultServiceClient.newDefault(nodeName, serviceDefinition, serializer, deserializer,
                executorService);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      serviceClient.connect(serviceDefinition.getUri());
    }
    return serviceClient;
  }
}
