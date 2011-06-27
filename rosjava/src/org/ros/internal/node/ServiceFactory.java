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

package org.ros.internal.node;

import com.google.common.base.Preconditions;

import org.ros.MessageDeserializer;
import org.ros.internal.message.ServiceMessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceFactory {

  private final GraphName nodeName;
  private final SlaveServer slaveServer;
  private final ServiceManager serviceManager;

  public ServiceFactory(GraphName nodeName, SlaveServer slaveServer, ServiceManager serviceManager) {
    this.nodeName = nodeName;
    this.slaveServer = slaveServer;
    this.serviceManager = serviceManager;
  }

  /**
   * Gets or creates a {@link ServiceServer} instance. {@link ServiceServer}s
   * are cached and reused per service. When a new {@link ServiceServer} is
   * generated, it is registered with the {@link MasterServer}.
   * 
   * @param serviceDefinition the {@link ServiceMessageDefinition} that is being served
   * @param responseBuilder the {@link ServiceResponseBuilder} that is used to
   *        build responses
   * @return a {@link ServiceServer} instance
   * @throws Exception
   */
  public <RequestType, ResponseType> ServiceServer createServiceServer(
      ServiceDefinition serviceDefinition,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) throws Exception {
    ServiceServer serviceServer;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServiceServer(name)) {
        serviceServer = serviceManager.getServiceServer(name);
      } else {
        serviceServer =
            new ServiceServer(serviceDefinition, responseBuilder,
                slaveServer.getTcpRosAdvertiseAddress());
        createdNewService = true;
      }
    }

    if (createdNewService) {
      slaveServer.addService(serviceServer);
    }
    return serviceServer;
  }

  /**
   * Gets or creates a {@link ServiceClient} instance. {@link ServiceClient}s
   * are cached and reused per service. When a new {@link ServiceClient} is
   * created, it is connected to the {@link ServiceServer}.
   * 
   * @param <ResponseMessageType>
   * @param serviceDefinition the {@link ServiceIdentifier} of the server
   * @return a {@link ServiceClient} instance
   */
  @SuppressWarnings("unchecked")
  public <ResponseMessageType> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceDefinition serviceDefinition, MessageDeserializer<ResponseMessageType> deserializer) {
    Preconditions.checkNotNull(serviceDefinition.getUri());
    ServiceClient<ResponseMessageType> serviceClient;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServiceClient(name)) {
        serviceClient = (ServiceClient<ResponseMessageType>) serviceManager.getServiceClient(name);
      } else {
        serviceClient = ServiceClient.create(nodeName, serviceDefinition, deserializer);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      serviceClient.connect(serviceDefinition.getUri());
    }
    return serviceClient;
  }
}
