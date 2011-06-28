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
import org.ros.MessageSerializer;
import org.ros.internal.exception.RemoteException;
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
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;

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
   * @param serviceDefinition
   *          the {@link ServiceMessageDefinition} that is being served
   * @param responseBuilder
   *          the {@link ServiceResponseBuilder} that is used to build responses
   * @return a {@link ServiceServer} instance
   * @throws RemoteException
   * @throws XmlRpcTimeoutException
   */
  @SuppressWarnings("unchecked")
  public <RequestType, ResponseType> ServiceServer<RequestType, ResponseType> createServer(
      ServiceDefinition serviceDefinition, MessageDeserializer<RequestType> deserializer,
      MessageSerializer<ResponseType> serializer,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    ServiceServer<RequestType, ResponseType> serviceServer;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasServer(name)) {
        serviceServer =
            (ServiceServer<RequestType, ResponseType>) serviceManager.getServer(name);
      } else {
        serviceServer =
            new ServiceServer<RequestType, ResponseType>(serviceDefinition, deserializer,
                serializer, responseBuilder, slaveServer.getTcpRosAdvertiseAddress());
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
   * @param <ResponseType>
   * @param serviceDefinition
   *          the {@link ServiceIdentifier} of the server
   * @return a {@link ServiceClient} instance
   */
  @SuppressWarnings("unchecked")
  public <RequestType, ResponseType> ServiceClient<RequestType, ResponseType> createClient(
      ServiceDefinition serviceDefinition, MessageSerializer<RequestType> serializer,
      MessageDeserializer<ResponseType> deserializer) {
    Preconditions.checkNotNull(serviceDefinition.getUri());
    ServiceClient<RequestType, ResponseType> serviceClient;
    String name = serviceDefinition.getName().toString();
    boolean createdNewService = false;

    synchronized (serviceManager) {
      if (serviceManager.hasClient(name)) {
        serviceClient =
            (ServiceClient<RequestType, ResponseType>) serviceManager.getClient(name);
      } else {
        serviceClient = ServiceClient.create(nodeName, serviceDefinition, serializer, deserializer);
        createdNewService = true;
      }
    }

    if (createdNewService) {
      serviceClient.connect(serviceDefinition.getUri());
    }
    return serviceClient;
  }
}
