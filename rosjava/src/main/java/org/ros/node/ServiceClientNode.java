/*
 * Copyright (C) 2012 Google Inc.
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

package org.ros.node;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link NodeMain} which provides a service client
 *
 * @author Spyros Koukas
 */
public class ServiceClientNode<T extends Message, S extends Message> extends AbstractNodeMain {
    private final GraphName graphName;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * Getter for serviceClient
     *
     * @return serviceClient
     **/
    public final ServiceClient<T, S> getServiceClient() {
        return serviceClient;
    }

    private ServiceClient<T, S> serviceClient;
    private final String serviceType;
    private final String serviceName;

    public ServiceClientNode(final String name, final String serviceName, final String serviceType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name));
        Preconditions.checkArgument(StringUtils.isNotBlank(serviceName));
        Preconditions.checkArgument(StringUtils.isNotBlank(serviceType));
        this.graphName = GraphName.of(name);
        this.serviceName = serviceName;
        this.serviceType = serviceType;
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        try {
            this.serviceClient = connectedNode.newServiceClient(serviceName, serviceType);
        } catch (final ServiceNotFoundException exception) {
            throw new RuntimeException(exception);
        }
        this.countDownLatch.countDown();
    }

    @Override
    public void onShutdown(Node node) {
        if (this.serviceClient != null) {
            try {
                this.serviceClient.shutdown();
            } catch (final Exception ignore) {
            }
            this.serviceClient = null;
        }
    }

    /**
     * Awaits for the Node to start.
     * The thread however can be interrupted or it can return if the Node has shutdown.
     *
     * @return true if the Node is started and the service client is connected
     *
     * @throws InterruptedException
     */
    public boolean awaitConnection() throws InterruptedException {
        this.countDownLatch.await();
        return this.serviceClient != null && this.serviceClient.isConnected();
    }

    /**
     * Awaits for the Node to start.
     * The thread however can be interrupted or it can return if the Node has shutdown.
     *
     * @param time
     * @param unit
     *
     * @return true if the Node is started and the service client is connected
     *
     * @throws InterruptedException
     */
    public boolean awaitConnection(final long time, final TimeUnit unit) throws InterruptedException {
        return this.countDownLatch.await(time, unit) &&
                this.serviceClient != null && this.serviceClient.isConnected();
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

    /**
     * Getter for serviceType
     *
     * @return serviceType
     **/
    public final String getServiceType() {
        return serviceType;
    }

    /**
     * Getter for serviceName
     *
     * @return serviceName
     **/
    public final String getServiceName() {
        return serviceName;
    }

    /**
     * @return the name of the {@link Node} that will be used if a name was not
     * specified in the {@link Node}'s associated
     * {@link NodeConfiguration}
     */
    @Override
    public GraphName getDefaultNodeName() {
        return graphName;
    }
}
