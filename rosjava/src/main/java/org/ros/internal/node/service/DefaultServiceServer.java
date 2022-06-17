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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.serializer.XmlRpcConstants;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.ros.address.AdvertiseAddress;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.internal.message.Message;
import org.ros.internal.message.service.ServiceDescription;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Default implementation of a {@link ServiceServer}.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
final class DefaultServiceServer<T extends Message, S extends Message> implements ChannelBufferServiceServer<T, S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPublisher.class);


    private final ServiceDeclaration serviceDeclaration;
    private final ServiceResponseBuilder<T, S> serviceResponseBuilder;
    private final AdvertiseAddress advertiseAddress;
    private final MessageDeserializer<T> messageDeserializer;
    private final MessageSerializer<S> messageSerializer;
    private final MessageFactory messageFactory;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ListenerGroup<ServiceServerListener<T, S>> listenerGroup;

    public DefaultServiceServer(ServiceDeclaration serviceDeclaration,
                                ServiceResponseBuilder<T, S> serviceResponseBuilder, AdvertiseAddress advertiseAddress,
                                MessageDeserializer<T> messageDeserializer, MessageSerializer<S> messageSerializer,
                                MessageFactory messageFactory, ScheduledExecutorService scheduledExecutorService) {
        this.serviceDeclaration = serviceDeclaration;
        this.serviceResponseBuilder = serviceResponseBuilder;
        this.advertiseAddress = advertiseAddress;
        this.messageDeserializer = messageDeserializer;
        this.messageSerializer = messageSerializer;
        this.messageFactory = messageFactory;
        this.scheduledExecutorService = scheduledExecutorService;
        this.listenerGroup = new ListenerGroup<>(scheduledExecutorService);
        this.listenerGroup.add(new LoggerServiceListener());
    }

    private final class LoggerServiceListener extends DefaultServiceServerListener<T, S> {
        private LoggerServiceListener() {

        }

        @Override
        public void onMasterRegistrationSuccess(ServiceServer<T, S> registrant) {
            LOGGER.info("Service registered: " + DefaultServiceServer.this);
        }

        @Override
        public void onMasterRegistrationFailure(ServiceServer<T, S> registrant) {
            LOGGER.info("Service registration failed: " + DefaultServiceServer.this);
        }

        @Override
        public void onMasterUnregistrationSuccess(ServiceServer<T, S> registrant) {
            LOGGER.info("Service unregistered: " + DefaultServiceServer.this);
        }

        @Override
        public void onMasterUnregistrationFailure(ServiceServer<T, S> registrant) {
            LOGGER.info("Service unregistration failed: " + DefaultServiceServer.this);
        }
    }

    public final ChannelBuffer finishHandshake(ConnectionHeader incomingConnectionHeader) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Client handshake header: " + incomingConnectionHeader);
        }
        final ConnectionHeader connectionHeader = toDeclaration().toConnectionHeader();
        final String expectedChecksum = connectionHeader.getField(ConnectionHeaderFields.MD5_CHECKSUM);
        final String incomingChecksum =
                incomingConnectionHeader.getField(ConnectionHeaderFields.MD5_CHECKSUM);
        // TODO(damonkohler): Pull out header field comparison logic.
        Preconditions.checkState(incomingChecksum.equals(expectedChecksum)
                || incomingChecksum.equals("*"));
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Server handshake header: " + connectionHeader);
        }
        return connectionHeader.encode();
    }

    @Override
    public URI getUri() {
        return advertiseAddress.toUri(XmlRpcConstants.ROSRPC);
    }

    @Override
    public GraphName getName() {
        return serviceDeclaration.getName();
    }

    /**
     * @return a new {@link ServiceDeclaration} with this
     * {@link DefaultServiceServer}'s {@link URI}
     */
    ServiceDeclaration toDeclaration() {
        ServiceIdentifier identifier = new ServiceIdentifier(serviceDeclaration.getName(), getUri());
        return new ServiceDeclaration(identifier, new ServiceDescription(serviceDeclaration.getType(),
                serviceDeclaration.getDefinition(), serviceDeclaration.getMd5Checksum()));
    }

    public ChannelHandler newRequestHandler() {
        return new ServiceRequestHandler<T, S>(serviceDeclaration, serviceResponseBuilder,
                messageDeserializer, messageSerializer, messageFactory, scheduledExecutorService);
    }

    /**
     * Signal all {@link ServiceServerListener}s that the {@link ServiceServer}
     * has been successfully registered with the master.
     *
     * <p>
     * Each listener is called in a separate thread.
     */
    public void onMasterRegistrationSuccess() {
        final ServiceServer<T, S> serviceServer = this;
        listenerGroup.signal(new SignalRunnable<ServiceServerListener<T, S>>() {
            @Override
            public void run(ServiceServerListener<T, S> listener) {
                listener.onMasterRegistrationSuccess(serviceServer);
            }
        });
    }

    /**
     * Signal all {@link ServiceServerListener}s that the {@link ServiceServer}
     * has failed to register with the master.
     *
     * <p>
     * Each listener is called in a separate thread.
     */
    public void onMasterRegistrationFailure() {
        final ServiceServer<T, S> serviceServer = this;
        listenerGroup.signal(new SignalRunnable<ServiceServerListener<T, S>>() {
            @Override
            public void run(ServiceServerListener<T, S> listener) {
                listener.onMasterRegistrationFailure(serviceServer);
            }
        });
    }

    /**
     * Signal all {@link ServiceServerListener}s that the {@link ServiceServer}
     * has been successfully unregistered with the master.
     *
     * <p>
     * Each listener is called in a separate thread.
     */
    public void onMasterUnregistrationSuccess() {
        final ServiceServer<T, S> serviceServer = this;
        listenerGroup.signal(new SignalRunnable<ServiceServerListener<T, S>>() {
            @Override
            public void run(ServiceServerListener<T, S> listener) {
                listener.onMasterUnregistrationSuccess(serviceServer);
            }
        });
    }

    /**
     * Signal all {@link ServiceServerListener}s that the {@link ServiceServer}
     * has failed to unregister with the master.
     *
     * <p>
     * Each listener is called in a separate thread.
     */
    public void onMasterUnregistrationFailure() {
        final ServiceServer<T, S> serviceServer = this;
        listenerGroup.signal(new SignalRunnable<ServiceServerListener<T, S>>() {
            @Override
            public void run(ServiceServerListener<T, S> listener) {
                listener.onMasterUnregistrationFailure(serviceServer);
            }
        });
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(ServiceServerListener<T, S> listener) {
        listenerGroup.add(listener);
    }

    @Override
    public String toString() {
        return "ServiceServer<" + toDeclaration() + ">";
    }
}
