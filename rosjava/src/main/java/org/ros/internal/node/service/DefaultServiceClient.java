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
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.message.Message;
import org.ros.internal.message.MessageBufferPool;
import org.ros.internal.transport.ClientHandshakeListener;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpClient;
import org.ros.internal.transport.tcp.TcpClientManager;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a {@link ServiceClient}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
final class DefaultServiceClient<T extends Message, S extends Message> implements ServiceClient<T, S> {
  private final ServiceDeclaration serviceDeclaration;
  private final MessageSerializer<T> serializer;
  private final MessageFactory messageFactory;
  private final MessageBufferPool messageBufferPool;
  private final ConcurrentLinkedQueue<ServiceResponseListener<S>> responseListeners;
  private final ConnectionHeader connectionHeader;
  private final TcpClientManager tcpClientManager;
  private final HandshakeLatch handshakeLatch;
  private TcpClient tcpClient;
  private final class HandshakeLatch implements ClientHandshakeListener {

  private CountDownLatch latch;
  private boolean success;
  private String errorMessage;

    @Override
    public final void onSuccess(final ConnectionHeader outgoingConnectionHeader,
        final ConnectionHeader incomingConnectionHeader) {
      this.success = true;
      this.latch.countDown();
    }

    @Override
    public final void onFailure(final ConnectionHeader outgoingConnectionHeader, final String errorMessage) {
      this.errorMessage = errorMessage;
      this.success = false;
      this.latch.countDown();
    }

    final boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
      this.latch.await(timeout, unit);
      return success;
    }

    final String getErrorMessage() {
      return this.errorMessage;
    }

    final void reset() {
      this.latch = new CountDownLatch(1);
      this.success = false;
      this.errorMessage = null;
    }
  }



   final static <S extends Message, T extends Message> DefaultServiceClient<S, T> newDefault(final GraphName nodeName,
      final ServiceDeclaration serviceDeclaration, final MessageSerializer<S> serializer,
      final MessageDeserializer<T> deserializer, final MessageFactory messageFactory,
      final ScheduledExecutorService executorService) {
    return new DefaultServiceClient<S, T>(nodeName, serviceDeclaration, serializer, deserializer,
        messageFactory, executorService);
  }

  private DefaultServiceClient(final GraphName nodeName,
      final ServiceDeclaration serviceDeclaration, final MessageSerializer<T> serializer,
      final MessageDeserializer<S> deserializer, final MessageFactory messageFactory,
      final ScheduledExecutorService executorService) {
    this.serviceDeclaration = serviceDeclaration;
    this.serializer = serializer;
    this.messageFactory = messageFactory;
    this.messageBufferPool = new MessageBufferPool();
    this.responseListeners = new ConcurrentLinkedQueue<>();
    this.connectionHeader = new ConnectionHeader();
    this.connectionHeader.addField(ConnectionHeaderFields.CALLER_ID, nodeName.toString());
    // TODO(damonkohler): Support non-persistent connections.
    this.connectionHeader.addField(ConnectionHeaderFields.PERSISTENT, "1");
    this.connectionHeader.merge(serviceDeclaration.toConnectionHeader());
    this.tcpClientManager = new TcpClientManager(executorService);
    final ServiceClientHandshakeHandler<T, S> serviceClientHandshakeHandler =
        new ServiceClientHandshakeHandler<T, S>(connectionHeader, responseListeners, deserializer,
            executorService);
    this.handshakeLatch = new HandshakeLatch();
    serviceClientHandshakeHandler.addListener(handshakeLatch);
    this.tcpClientManager.addNamedChannelHandler(serviceClientHandshakeHandler);
  }

  @Override
  public final void connect(final URI uri) {
    Preconditions.checkNotNull(uri, "URI must be specified.");
    Preconditions.checkArgument(uri.getScheme().equals("rosrpc"), "Invalid service URI.");
    Preconditions.checkState(tcpClient == null, "Already connected.");
    final InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    this.handshakeLatch.reset();
    this.tcpClient = this.tcpClientManager.connect(toString(), address);
    try {
      if (!this.handshakeLatch.await(1, TimeUnit.SECONDS)) {
        throw new RosRuntimeException(this.handshakeLatch.getErrorMessage());
      }
    } catch (final InterruptedException e) {
      throw new RosRuntimeException("Handshake timed out.");
    }
  }

  @Override
  public final void shutdown() {
    this.tcpClientManager.shutdown();
  }

  @Override
  synchronized public final void call(final T request, final ServiceResponseListener<S> listener) {
    final ChannelBuffer buffer = this.messageBufferPool.acquire();
    this.serializer.serialize(request, buffer);
    this.responseListeners.add(listener);
    this.tcpClient.write(buffer).awaitUninterruptibly();
    this.messageBufferPool.release(buffer);
  }

  @Override
  public final GraphName getName() {
    return this.serviceDeclaration.getName();
  }

  @Override
  public final String toString() {
    return "ServiceClient<" + this.serviceDeclaration + ">";
  }

  @Override
  public final T newMessage() {
    return this.messageFactory.newFromType(this.serviceDeclaration.getType());
  }

  @Override
  public final boolean isConnected() {
    return this.tcpClient.getChannel().isConnected();
  }
}
