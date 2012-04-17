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

import org.ros.node.service.ServiceResponseBuilder;

import org.ros.internal.message.service.ServiceDescription;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.server.master.MasterServer;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A factory for {@link ServiceServer}s and {@link ServiceClient}s.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceFactory {

  private final GraphName nodeName;
  private final SlaveServer slaveServer;
  private final ServiceManager serviceManager;
  private final ScheduledExecutorService executorService;

  public ServiceFactory(GraphName nodeName, SlaveServer slaveServer, ServiceManager serviceManager,
      ScheduledExecutorService executorService) {
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
   * @param serviceDeclaration
   *          the {@link ServiceDescription} that is being served
   * @param responseBuilder
   *          the {@link ServiceResponseBuilder} that is used to build responses
   * @param deserializer
   *          a {@link MessageDeserializer} to be used for incoming messages
   * @param serializer
   *          a {@link MessageSerializer} to be used for outgoing messages
   * @param messageFactory
   *          TODO
   * @return a {@link DefaultServiceServer} instance
   */
  @SuppressWarnings("unchecked")
  public <T, S> DefaultServiceServer<T, S> newServer(ServiceDeclaration serviceDeclaration,
      ServiceResponseBuilder<T, S> responseBuilder, MessageDeserializer<T> deserializer,
      MessageSerializer<S> serializer, MessageFactory messageFactory) {
    DefaultServiceServer<T, S> serviceServer;
    String name = serviceDeclaration.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServer(name)) {
        serviceServer = (DefaultServiceServer<T, S>) serviceManager.getServer(name);
      } else {
        serviceServer =
            new DefaultServiceServer<T, S>(serviceDeclaration, responseBuilder,
                slaveServer.getTcpRosAdvertiseAddress(), deserializer, serializer, messageFactory,
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
   * @param messageFactory
   *          TODO
   * 
   * @return a {@link DefaultServiceClient} instance
   */
  @SuppressWarnings("unchecked")
  public <T, S> DefaultServiceClient<T, S> newClient(ServiceDeclaration serviceDeclaration,
      MessageSerializer<T> serializer, MessageDeserializer<S> deserializer,
      MessageFactory messageFactory) {
    Preconditions.checkNotNull(serviceDeclaration.getUri());
    DefaultServiceClient<T, S> serviceClient;
    String name = serviceDeclaration.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasClient(name)) {
        serviceClient = (DefaultServiceClient<T, S>) serviceManager.getClient(name);
      } else {
        serviceClient =
            DefaultServiceClient.newDefault(nodeName, serviceDeclaration, serializer, deserializer,
                messageFactory, executorService);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      serviceClient.connect(serviceDeclaration.getUri());
    }
    return serviceClient;
  }
}
