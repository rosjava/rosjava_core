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
import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.service.ChannelBufferServiceServer;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages a collection of {@link org.ros.node.service.ChannelBufferServiceServer}s and {@link ServiceClient}s.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class ServiceManager {

    /**
     * A mapping from service name to the server for the service.
     */
    private final Map<GraphName, ChannelBufferServiceServer<? extends Message, ? extends Message>> serviceServers;

    /**
     * A mapping from service name to a client for the service.
     */
    private final Map<GraphName, ServiceClient<? extends Message, ? extends Message>> serviceClients;

    // TODO(damonkohler): Change to ListenerGroup.
    private ServiceManagerListener listener;

    public ServiceManager() {
        serviceServers = Maps.newConcurrentMap();
        serviceClients = Maps.newConcurrentMap();
    }

    public void setListener(final ServiceManagerListener listener) {
        this.listener = listener;
    }

    public boolean hasServer(GraphName name) {
        return this.serviceServers.containsKey(name);
    }

    public void addServer(final ChannelBufferServiceServer<? extends Message, ? extends Message> serviceServer) {
        serviceServers.put(serviceServer.getName(), serviceServer);
        if (listener != null) {
            listener.onServiceServerAdded(serviceServer);
        }
    }

    public void removeServer(ServiceServer<? extends Message, ? extends Message> serviceServer) {
        this.serviceServers.remove(serviceServer.getName());
        if (this.listener != null) {
            this.listener.onServiceServerRemoved(serviceServer);
        }
    }

    public final ChannelBufferServiceServer<? extends Message, ? extends Message> getServer(final GraphName name) {
        return serviceServers.get(name);
    }

    public final boolean hasClient(final GraphName name) {
        return this.serviceClients.containsKey(name);
    }

    public final void addClient(ServiceClient<? extends Message, ? extends Message> serviceClient) {
        this.serviceClients.put(serviceClient.getName(), serviceClient);
    }

    public void removeClient(ServiceClient<?, ?> serviceClient) {
        serviceClients.remove(serviceClient.getName());
    }

    public ServiceClient<?, ?> getClient(GraphName name) {
        return serviceClients.get(name);
    }

    public final List<ChannelBufferServiceServer<?, ?>> getServers() {
        return ImmutableList.copyOf(this.serviceServers.values());
    }

    public final List<ServiceClient<?, ?>> getClients() {
        return ImmutableList.copyOf(serviceClients.values());
    }

    public final Set<GraphName> getServerNames() {
        return Collections.unmodifiableSet(serviceServers.keySet());
    }

    public final Set<GraphName> getClientNames() {
        return Collections.unmodifiableSet(serviceClients.keySet());
    }
}
