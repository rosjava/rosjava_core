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

import com.google.common.annotations.VisibleForTesting;
import org.ros.Parameters;
import org.ros.concurrent.CancellableLoop;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.message.Message;
import org.ros.internal.message.service.ServiceDescription;
import org.ros.internal.message.topic.TopicDescription;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.Registrar;
import org.ros.internal.node.parameter.DefaultParameterTree;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.NodeIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceDeclaration;
import org.ros.internal.node.service.ServiceFactory;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.PublisherFactory;
import org.ros.internal.node.topic.SubscriberFactory;
import org.ros.internal.node.topic.TopicDeclaration;
import org.ros.internal.node.topic.TopicParticipantManager;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.message.*;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;
import org.ros.node.*;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseBuilder;
import org.ros.node.service.ServiceServer;
import org.ros.node.topic.*;
import org.ros.time.ClockTopicTimeProvider;
import org.ros.time.TimeProvider;
import rosgraph_msgs.Log;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The default implementation of a {@link Node}.
 *
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 * @author Spyros Koukas
 */
public class DefaultNode implements ConnectedNode {

    /**
     * The maximum delay before shutdown will begin even if all
     * {@link NodeListener}s have not yet returned from their
     * {@link NodeListener#onShutdown(Node)} callback.
     */
    private static final int MAX_SHUTDOWN_DELAY_DURATION = 5;
    private static final TimeUnit MAX_SHUTDOWN_DELAY_UNITS = TimeUnit.SECONDS;

    private final NodeConfiguration nodeConfiguration;
    private final ListenerGroup<NodeListener> nodeListeners;
    private final ScheduledExecutorService scheduledExecutorService;
    private final URI masterUri;
    private final MasterClient masterClient;
    private final TopicParticipantManager topicParticipantManager;
    private final ServiceManager serviceManager;
    private final ParameterManager parameterManager;
    private final GraphName nodeName;
    private final NodeNameResolver resolver;
    private final SlaveServer slaveServer;
    private final ParameterTree parameterTree;
    private final PublisherFactory publisherFactory;
    private final SubscriberFactory subscriberFactory;
    private final ServiceFactory serviceFactory;
    private final Registrar registrar;

    private RosoutLogger rosoutLogger;
    private TimeProvider timeProvider;

    /**
     * {@link DefaultNode}s should only be constructed using the
     * {@link DefaultNodeFactory}.
     *
     * @param nodeConfiguration the {@link NodeConfiguration} for this {@link Node}
     * @param nodeListeners     a {@link Collection} of {@link NodeListener}s that will be added
     *                          to this {@link Node} before it starts
     */
    public DefaultNode(NodeConfiguration nodeConfiguration, Collection<NodeListener> nodeListeners,
                       ScheduledExecutorService scheduledExecutorService) {
        this.nodeConfiguration = NodeConfiguration.copyOf(nodeConfiguration);
        this.nodeListeners = new ListenerGroup<NodeListener>(scheduledExecutorService);
        this.nodeListeners.addAll(nodeListeners);
        this.scheduledExecutorService = scheduledExecutorService;
        this.masterUri = nodeConfiguration.getMasterUri();
        this.masterClient = new MasterClient(masterUri);
        this.topicParticipantManager = new TopicParticipantManager();
        this.serviceManager = new ServiceManager();
        this.parameterManager = new ParameterManager(scheduledExecutorService);

        final GraphName basename = nodeConfiguration.getNodeName();
        final NameResolver parentResolver = nodeConfiguration.getParentResolver();
        this.nodeName = parentResolver.getNamespace().join(basename);
        this.resolver = new NodeNameResolver(this.nodeName, parentResolver);
        this.slaveServer =
                new SlaveServer(this.nodeName, nodeConfiguration.getTcpRosBindAddress(),
                        nodeConfiguration.getTcpRosAdvertiseAddress(),
                        nodeConfiguration.getXmlRpcBindAddress(),
                        nodeConfiguration.getXmlRpcAdvertiseAddress(), this.masterClient, this.topicParticipantManager,
                        this.serviceManager, this.parameterManager, scheduledExecutorService, this);
        this.slaveServer.start();

        final NodeIdentifier nodeIdentifier = this.slaveServer.toNodeIdentifier();

        this.parameterTree =
                DefaultParameterTree.newFromNodeIdentifier(nodeIdentifier, this.masterClient.getRemoteUri(),
                        this.resolver, this.parameterManager);

        this.publisherFactory =
                new PublisherFactory(nodeIdentifier, this.topicParticipantManager,
                        nodeConfiguration.getTopicMessageFactory(), scheduledExecutorService);
        this.subscriberFactory =
                new SubscriberFactory(nodeIdentifier, this.topicParticipantManager, scheduledExecutorService);
        this.serviceFactory =
                new ServiceFactory(this.nodeName, this.slaveServer, this.serviceManager, scheduledExecutorService);

        this.registrar = new Registrar(this.masterClient, scheduledExecutorService);
        this.topicParticipantManager.setListener(this.registrar);
        this.serviceManager.setListener(this.registrar);

        scheduledExecutorService.execute(this::start);
    }

    private final void start() {
        // The Registrar must be started first so that master registration is
        // possible during startup.
        this.registrar.start(slaveServer.toNodeIdentifier());

        // Wait for the logger to register with the master. This ensures the master is running before
        // requesting the use_sim_time parameter.
        final CountDownLatch rosoutLatch = new CountDownLatch(1);


        final DefaultPublisherListener<Log> defaultPublisherListener = new DefaultPublisherListener<>() {
            @Override
            public void onMasterRegistrationSuccess(Publisher<Log> registrant) {
                rosoutLatch.countDown();
            }
        };
        final Consumer<Publisher<Log>> consumer = publisher -> publisher.addListener(defaultPublisherListener);
        this.rosoutLogger = new RosoutLogger(this, consumer);
        try {
            rosoutLatch.await();

//          TODO: SpyrosKoukas  this.rosoutLogger.getPublisher().removeListener()
        } catch (InterruptedException e) {
            this.signalOnError(e);
            this.shutdown();
            return;
        }

        boolean useSimTime = false;
        try {
            useSimTime =
                    parameterTree.has(Parameters.USE_SIM_TIME)
                            && parameterTree.getBoolean(Parameters.USE_SIM_TIME);
        } catch (final Exception e) {
            signalOnError(e);
            shutdown();
            return;
        }

        final CountDownLatch timeLatch = new CountDownLatch(1);
        if (useSimTime) {
            final ClockTopicTimeProvider clockTopicTimeProvider = new ClockTopicTimeProvider(this);
            clockTopicTimeProvider.getSubscriber().addSubscriberListener(
                    new DefaultSubscriberListener<rosgraph_msgs.Clock>() {
                        @Override
                        public void onMasterRegistrationSuccess(Subscriber<rosgraph_msgs.Clock> subscriber) {
                            timeLatch.countDown();
                        }
                    });
            this.timeProvider = clockTopicTimeProvider;
        } else {
            this.timeProvider = this.nodeConfiguration.getTimeProvider();
            timeLatch.countDown();
        }

        try {
            timeLatch.await();
        } catch (InterruptedException e) {
            signalOnError(e);
            shutdown();
            return;
        }

        signalOnStart();
    }

    @VisibleForTesting
    Registrar getRegistrar() {
        return this.registrar;
    }

    private <T extends Message> org.ros.message.MessageSerializer<T> newMessageSerializer(String messageType) {
        return this.nodeConfiguration.getMessageSerializationFactory().newMessageSerializer(messageType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> MessageDeserializer<T> newMessageDeserializer(String messageType) {
        return this.nodeConfiguration.getMessageSerializationFactory().newMessageDeserializer(messageType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> MessageSerializer<T> newServiceResponseSerializer(String serviceType) {
        return this.nodeConfiguration.getMessageSerializationFactory()
                .newServiceResponseSerializer(serviceType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> MessageDeserializer<T> newServiceResponseDeserializer(String serviceType) {
        return  this.nodeConfiguration.getMessageSerializationFactory()
                .newServiceResponseDeserializer(serviceType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> MessageSerializer<T> newServiceRequestSerializer(String serviceType) {
        return this.nodeConfiguration.getMessageSerializationFactory()
                .newServiceRequestSerializer(serviceType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> MessageDeserializer<T> newServiceRequestDeserializer(String serviceType) {
        return this.nodeConfiguration.getMessageSerializationFactory()
                .newServiceRequestDeserializer(serviceType);
    }

    @Override
    public <T extends Message> Publisher<T> newPublisher(GraphName topicName, String messageType) {
        final GraphName resolvedTopicName = resolveName(topicName);
        final TopicDescription topicDescription =
                this.nodeConfiguration.getTopicDescriptionFactory().newFromType(messageType);
        final TopicDeclaration topicDeclaration =
                TopicDeclaration.newFromTopicName(resolvedTopicName, topicDescription, null);
        final org.ros.message.MessageSerializer<T> serializer = newMessageSerializer(messageType);
        return this.publisherFactory.newOrExisting(topicDeclaration, serializer);
    }

    @Override
    public <T extends Message> Publisher<T> newPublisher(String topicName, String messageType) {
        return newPublisher(GraphName.of(topicName), messageType);
    }

    @Override
    public <T extends Message> Subscriber<T> newSubscriber(GraphName topicName, String messageType) {
        return newSubscriber(topicName, messageType, null);
    }

    @Override
    public <T extends Message> Subscriber<T> newSubscriber(GraphName topicName, String messageType, TransportHints transportHints) {
        final GraphName resolvedTopicName = resolveName(topicName);
        final TopicDescription topicDescription =
                this.nodeConfiguration.getTopicDescriptionFactory().newFromType(messageType);
        final TopicDeclaration topicDeclaration =
                TopicDeclaration.newFromTopicName(resolvedTopicName, topicDescription, transportHints);
        final MessageDeserializer<T> deserializer = newMessageDeserializer(messageType);
        final Subscriber<T> subscriber = this.subscriberFactory.newOrExisting(topicDeclaration, deserializer);
        return subscriber;
    }

    @Override
    public <T extends Message> Subscriber<T> newSubscriber(String topicName, String messageType) {
        return newSubscriber(GraphName.of(topicName), messageType, null);
    }

    @Override
    public <T extends Message> Subscriber<T> newSubscriber(String topicName, String messageType, TransportHints transportHints) {
        return newSubscriber(GraphName.of(topicName), messageType, transportHints);
    }

    @Override
    public <T extends Message, S extends Message> ServiceServer<T, S> newServiceServer(GraphName serviceName, String serviceType,
                                                                       ServiceResponseBuilder<T, S> responseBuilder) {
        final GraphName resolvedServiceName = resolveName(serviceName);
        // TODO(damonkohler): It's rather non-obvious that the URI will be
        // created later on the fly.
        final ServiceIdentifier identifier = new ServiceIdentifier(resolvedServiceName, null);
        final ServiceDescription serviceDescription =
                this.nodeConfiguration.getServiceDescriptionFactory().newFromType(serviceType);
        final ServiceDeclaration definition = new ServiceDeclaration(identifier, serviceDescription);
        final MessageDeserializer<T> requestDeserializer = newServiceRequestDeserializer(serviceType);
        final MessageSerializer<S> responseSerializer = newServiceResponseSerializer(serviceType);
        return this.serviceFactory.newServer(definition, responseBuilder, requestDeserializer,
                responseSerializer, nodeConfiguration.getServiceResponseMessageFactory());
    }

    @Override
    public <T extends Message, S extends Message> ServiceServer<T, S> newServiceServer(String serviceName, String serviceType,
                                                       ServiceResponseBuilder<T, S> responseBuilder) {
        return newServiceServer(GraphName.of(serviceName), serviceType, responseBuilder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T  extends Message, S extends Message> ServiceServer<T, S> getServiceServer(GraphName serviceName) {
        return (ServiceServer<T, S>) serviceManager.getServer(serviceName);
    }

    @Override
    public <T extends Message, S extends Message> ServiceServer<T, S> getServiceServer(String serviceName) {
        return getServiceServer(GraphName.of(serviceName));
    }

    @Override
    public URI lookupServiceUri(GraphName serviceName) {
        final Response<URI> response =
                masterClient.lookupService(slaveServer.toNodeIdentifier().getName(),
                        resolveName(serviceName).toString());
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public URI lookupServiceUri(String serviceName) {
        return lookupServiceUri(GraphName.of(serviceName));
    }

    @Override
    public <T extends Message, S extends Message> ServiceClient<T, S> newServiceClient(GraphName serviceName, String serviceType)
            throws ServiceNotFoundException {
        final GraphName resolvedServiceName = resolveName(serviceName);
        final URI uri = lookupServiceUri(resolvedServiceName);
        if (uri == null) {
            throw new ServiceNotFoundException("No such service " + resolvedServiceName + " of type "
                    + serviceType);
        }
        final ServiceDescription serviceDescription =
                this.nodeConfiguration.getServiceDescriptionFactory().newFromType(serviceType);
        final ServiceIdentifier serviceIdentifier = new ServiceIdentifier(resolvedServiceName, uri);
        final ServiceDeclaration definition = new ServiceDeclaration(serviceIdentifier, serviceDescription);
        final MessageSerializer<T> requestSerializer = newServiceRequestSerializer(serviceType);
        final MessageDeserializer<S> responseDeserializer = newServiceResponseDeserializer(serviceType);
        return this.serviceFactory.newClient(definition, requestSerializer, responseDeserializer,
                nodeConfiguration.getServiceRequestMessageFactory());
    }

    @Override
    public <T extends Message, S extends Message> ServiceClient<T, S> newServiceClient(String serviceName, String serviceType)
            throws ServiceNotFoundException {
        return newServiceClient(GraphName.of(serviceName), serviceType);
    }

    @Override
    public Time getCurrentTime() {
        return timeProvider.getCurrentTime();
    }

    @Override
    public GraphName getName() {
        return nodeName;
    }

    @Override
    public RosLog getLog() {
        return rosoutLogger;
    }

    @Override
    public GraphName resolveName(GraphName name) {
        return resolver.resolve(name);
    }

    @Override
    public GraphName resolveName(String name) {
        return resolver.resolve(GraphName.of(name));
    }

    @Override
    public void shutdown() {
        this.signalOnShutdown();
        // NOTE(damonkohler): We don't want to raise potentially spurious
        // exceptions during shutdown that would interrupt the process. This is
        // simply best effort cleanup.
        final String exceptionWhileShuttingDownMsg = "Exception while shutting down, during best effort cleanup";
        for (final ServiceServer<?, ?> serviceServer : this.serviceManager.getServers()) {
            try {
                final Response<Integer> response =
                        this.masterClient.unregisterService(this.slaveServer.toNodeIdentifier(), serviceServer);
                if (this.rosoutLogger.isDebugEnabled()) {
                    if (response.getResult() == 0) {
                        final String msg = "Failed to unregister service: " + serviceServer.getName();
                        this.rosoutLogger.error(msg);
                    }
                }
            } catch (final XmlRpcTimeoutException e) {
                this.rosoutLogger.error(exceptionWhileShuttingDownMsg, e);
            } catch (final RemoteException e) {
                this.rosoutLogger.error(exceptionWhileShuttingDownMsg, e);
            }
        }
        for (final ServiceClient<?, ?> serviceClient : this.serviceManager.getClients()) {
            serviceClient.shutdown();
        }
        this.slaveServer.shutdown();
        this.topicParticipantManager.shutdown();
        this.registrar.shutdown();
        this.signalOnShutdownComplete();
    }

    @Override
    public URI getMasterUri() {
        return masterUri;
    }

    @Override
    public NodeNameResolver getResolver() {
        return resolver;
    }

    @Override
    public ParameterTree getParameterTree() {
        return parameterTree;
    }

    @Override
    public URI getUri() {
        return slaveServer.getUri();
    }

    @Override
    public MessageSerializationFactory getMessageSerializationFactory() {
        return this.nodeConfiguration.getMessageSerializationFactory();
    }

    @Override
    public MessageFactory getTopicMessageFactory() {
        return this.nodeConfiguration.getTopicMessageFactory();
    }

    @Override
    public MessageFactory getServiceRequestMessageFactory() {
        return this.nodeConfiguration.getServiceRequestMessageFactory();
    }

    @Override
    public MessageFactory getServiceResponseMessageFactory() {
        return this.nodeConfiguration.getServiceResponseMessageFactory();
    }

    @Override
    public void addListener(NodeListener listener) {
        this.nodeListeners.add(listener);
    }

    /**
     * SignalRunnable all {@link NodeListener}s that the {@link Node} has
     * experienced an error.
     * <p>
     * Each listener is called in a separate thread.
     */
    private void signalOnError(final Throwable throwable) {
        final Node node = this;
        nodeListeners.signal(new SignalRunnable<NodeListener>() {
            @Override
            public void run(NodeListener listener) {
                listener.onError(node, throwable);
            }
        });
    }

    @Override
    public void removeListeners() {
        nodeListeners.shutdown();
    }

    /**
     * SignalRunnable all {@link NodeListener}s that the {@link Node} has started.
     * <p>
     * Each listener is called in a separate thread.
     */
    private void signalOnStart() {
        final ConnectedNode connectedNode = this;
        nodeListeners.signal(new SignalRunnable<NodeListener>() {
            @Override
            public void run(NodeListener listener) {
                listener.onStart(connectedNode);
            }
        });
    }

    /**
     * SignalRunnable all {@link NodeListener}s that the {@link Node} has started
     * shutting down.
     * <p>
     * Each listener is called in a separate thread.
     */
    private void signalOnShutdown() {
        final Node node = this;
        try {
            nodeListeners.signal(new SignalRunnable<NodeListener>() {
                @Override
                public void run(NodeListener listener) {
                    listener.onShutdown(node);
                }
            }, MAX_SHUTDOWN_DELAY_DURATION, MAX_SHUTDOWN_DELAY_UNITS);
        } catch (InterruptedException e) {
            // Ignored since we do not guarantee that all listeners will finish
            // before
            // shutdown begins.
        }
    }

    /**
     * SignalRunnable all {@link NodeListener}s that the {@link Node} has shut
     * down.
     * <p>
     * Each listener is called in a separate thread.
     */
    private void signalOnShutdownComplete() {
        final Node node = this;
        nodeListeners.signal(new SignalRunnable<NodeListener>() {
            @Override
            public void run(NodeListener listener) {
                try {
                    listener.onShutdownComplete(node);
                } catch (Throwable e) {
                    System.out.println(listener);
                }
            }
        });
    }

    @VisibleForTesting
    InetSocketAddress getAddress() {
        return slaveServer.getAddress();
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Override
    public void executeCancellableLoop(final CancellableLoop cancellableLoop) {
        scheduledExecutorService.execute(cancellableLoop);
        addListener(new NodeListener() {
            @Override
            public void onStart(ConnectedNode connectedNode) {
            }

            @Override
            public void onShutdown(Node node) {
                cancellableLoop.cancel();
            }

            @Override
            public void onShutdownComplete(Node node) {
            }

            @Override
            public void onError(Node node, Throwable throwable) {
                cancellableLoop.cancel();
            }
        });
    }
}
