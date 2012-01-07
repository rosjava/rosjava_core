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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultServiceClient<RequestType, ResponseType> implements
    ServiceClient<RequestType, ResponseType> {

  static final boolean DEBUG = false;
  static final Log log = LogFactory.getLog(DefaultServiceClient.class);

  private final ServiceDefinition serviceDefinition;
  private final TcpClientConnectionManager tcpClientConnectionManager;
  private final MessageSerializer<RequestType> serializer;
  private final MessageDeserializer<ResponseType> deserializer;
  private final Queue<ServiceResponseListener<ResponseType>> responseListeners;
  private final ImmutableMap<String, String> header;
  private final ExecutorService executorService;

  private TcpClientConnection tcpClientConnection;

  public static <S, T> DefaultServiceClient<S, T> newDefault(GraphName nodeName,
      ServiceDefinition serviceDefinition, MessageSerializer<S> serializer,
      MessageDeserializer<T> deserializer, ExecutorService executorService) {
    return new DefaultServiceClient<S, T>(nodeName, serviceDefinition, serializer, deserializer,
        executorService);
  }

  private DefaultServiceClient(GraphName nodeName, ServiceDefinition serviceDefinition,
      MessageSerializer<RequestType> serializer, MessageDeserializer<ResponseType> deserializer,
      ExecutorService executorService) {
    this.serviceDefinition = serviceDefinition;
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.executorService = executorService;
    responseListeners = Lists.newLinkedList();
    header =
        ImmutableMap.<String, String>builder()
            .put(ConnectionHeaderFields.CALLER_ID, nodeName.toString())
            // TODO(damonkohler): Support non-persistent connections.
            .put(ConnectionHeaderFields.PERSISTENT, "1")
            .putAll(serviceDefinition.toHeader())
            .build();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
  }

  @Override
  public void connect(URI uri) {
    Preconditions.checkNotNull(uri, "URI must be specified.");
    Preconditions.checkArgument(uri.getScheme().equals("rosrpc"), "Invalid service URI.");
    Preconditions.checkState(tcpClientConnection == null, "Already connected once.");
    InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    tcpClientConnection =
        tcpClientConnectionManager.connect(toString(), address,
            new ServiceClientHandshakeHandler<RequestType, ResponseType>(header, responseListeners,
                deserializer, executorService), "ServiceClientHandshakeHandler");
  }

  @Override
  public void shutdown() {
    Preconditions.checkNotNull(tcpClientConnection, "Not connected.");
    tcpClientConnectionManager.shutdown();
  }

  @Override
  public void call(RequestType request, ServiceResponseListener<ResponseType> listener) {
    ChannelBuffer wrappedBuffer = ChannelBuffers.wrappedBuffer(serializer.serialize(request));
    responseListeners.add(listener);
    tcpClientConnection.write(wrappedBuffer).awaitUninterruptibly();
  }
  
  @Override
  public GraphName getName() {
    return serviceDefinition.getName();
  }

  @Override
  public String toString() {
    return "ServiceClient<" + serviceDefinition + ">";
  }
}
