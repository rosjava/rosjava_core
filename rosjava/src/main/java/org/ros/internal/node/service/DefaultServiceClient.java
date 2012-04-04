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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpClientConnection;
import org.ros.internal.transport.tcp.TcpClientConnectionManager;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultServiceClient<T, S> implements ServiceClient<T, S> {

  private final ServiceDeclaration serviceDeclaration;
  private final TcpClientConnectionManager tcpClientConnectionManager;
  private final MessageSerializer<T> serializer;
  private final MessageDeserializer<S> deserializer;
  private final Queue<ServiceResponseListener<S>> responseListeners;
  private final ImmutableMap<String, String> header;
  private final ScheduledExecutorService executorService;

  private TcpClientConnection tcpClientConnection;

  public static <S, T> DefaultServiceClient<S, T> newDefault(GraphName nodeName,
      ServiceDeclaration serviceDeclaration, MessageSerializer<S> serializer,
      MessageDeserializer<T> deserializer, ScheduledExecutorService executorService) {
    return new DefaultServiceClient<S, T>(nodeName, serviceDeclaration, serializer, deserializer,
        executorService);
  }

  private DefaultServiceClient(GraphName nodeName, ServiceDeclaration serviceDeclaration,
      MessageSerializer<T> serializer, MessageDeserializer<S> deserializer,
      ScheduledExecutorService executorService) {
    this.serviceDeclaration = serviceDeclaration;
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.executorService = executorService;
    responseListeners = Lists.newLinkedList();
    header =
        ImmutableMap.<String, String>builder()
            .put(ConnectionHeaderFields.CALLER_ID, nodeName.toString())
            // TODO(damonkohler): Support non-persistent connections.
            .put(ConnectionHeaderFields.PERSISTENT, "1").putAll(serviceDeclaration.toHeader())
            .build();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
  }

  @Override
  public void connect(URI uri) {
    Preconditions.checkNotNull(uri, "URI must be specified.");
    Preconditions.checkArgument(uri.getScheme().equals("rosrpc"), "Invalid service URI.");
    Preconditions.checkState(tcpClientConnection == null, "Already connected once.");
    InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    ServiceClientHandshakeHandler<T, S> handler =
        new ServiceClientHandshakeHandler<T, S>(header, responseListeners, deserializer,
            executorService);
    tcpClientConnection =
        tcpClientConnectionManager.connect(toString(), address, handler,
            "ServiceClientHandshakeHandler");
    // TODO(damonkohler): Remove this once blocking on handshakes is supported.
    // See issue 75.
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RosRuntimeException(e);
    }
  }

  @Override
  public void shutdown() {
    Preconditions.checkNotNull(tcpClientConnection, "Not connected.");
    tcpClientConnectionManager.shutdown();
  }

  @Override
  public void call(T request, ServiceResponseListener<S> listener) {
    ChannelBuffer wrappedBuffer = ChannelBuffers.wrappedBuffer(serializer.serialize(request));
    responseListeners.add(listener);
    tcpClientConnection.write(wrappedBuffer).awaitUninterruptibly();
  }

  @Override
  public GraphName getName() {
    return serviceDeclaration.getName();
  }

  @Override
  public String toString() {
    return "ServiceClient<" + serviceDeclaration + ">";
  }
}
