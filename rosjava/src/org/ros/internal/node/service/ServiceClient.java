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
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.ros.MessageDeserializer;
import org.ros.MessageSerializer;
import org.ros.ServiceResponseListener;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpClientPipelineFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceClient<RequestType, ResponseType> implements
    org.ros.ServiceClient<RequestType, ResponseType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(ServiceClient.class);

  private final MessageSerializer<RequestType> serializer;
  private final MessageDeserializer<ResponseType> deserializer;
  private final Queue<ServiceResponseListener<ResponseType>> responseListeners;
  private final Map<String, String> header;
  private final ChannelFactory channelFactory;
  private final ClientBootstrap bootstrap;
  private final ChannelGroup channelGroup;

  private Channel channel;

  private final class HandshakeHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
      // TODO(damonkohler): Handle handshake errors.
      handshake(incomingBuffer);
      ChannelPipeline pipeline = e.getChannel().getPipeline();
      pipeline.remove(TcpClientPipelineFactory.LENGTH_FIELD_BASED_FRAME_DECODER);
      pipeline.remove(this);
      pipeline.addLast("ResponseDecoder", new ServiceResponseDecoder<ResponseType>());
      pipeline.addLast("ResponseHandler", new ServiceResponseHandler<ResponseType>(
          responseListeners, deserializer));
    }
  }

  public static <S, T> ServiceClient<S, T> create(GraphName nodeName,
      ServiceDefinition serviceDefinition, MessageSerializer<S> serializer,
      MessageDeserializer<T> deserializer) {
    return new ServiceClient<S, T>(nodeName, serviceDefinition, serializer, deserializer);
  }

  private ServiceClient(GraphName nodeName, ServiceDefinition serviceDefinition,
      MessageSerializer<RequestType> serializer, MessageDeserializer<ResponseType> deserializer) {
    this.serializer = serializer;
    this.deserializer = deserializer;
    responseListeners = Lists.newLinkedList();
    header =
        ImmutableMap.<String, String>builder()
            .put(ConnectionHeaderFields.CALLER_ID, nodeName.toString())
            // TODO(damonkohler): Support non-persistent connections.
            .put(ConnectionHeaderFields.PERSISTENT, "1")
            .putAll(serviceDefinition.toHeader())
            .build();
    channelFactory =
        new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    bootstrap = new ClientBootstrap(channelFactory);
    channelGroup = new DefaultChannelGroup();
    TcpClientPipelineFactory factory = new TcpClientPipelineFactory(channelGroup) {
      @Override
      public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = super.getPipeline();
        pipeline.addLast("HandshakeHandler", new HandshakeHandler());
        return pipeline;
      }
    };
    bootstrap.setPipelineFactory(factory);
    bootstrap.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
  }

  @Override
  public void connect(URI uri) {
    Preconditions.checkArgument(uri.getScheme().equals("rosrpc"));
    InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    // TODO(damonkohler): Add timeouts.
    ChannelFuture future = bootstrap.connect(address).awaitUninterruptibly();
    if (future.isSuccess()) {
      channel = future.getChannel();
      if (DEBUG) {
        log.info("Connected to: " + channel.getRemoteAddress());
      }
    } else {
      throw new RuntimeException("ServiceClient.connect() failed to connect to host "
          + uri.getHost() + " on port " + uri.getPort(), future.getCause());
    }
    ChannelBuffer encodedHeader = ConnectionHeader.encode(header);
    channel.write(encodedHeader).awaitUninterruptibly();
  }

  @Override
  public void shutdown() {
    channelGroup.close().awaitUninterruptibly();
    channelFactory.releaseExternalResources();
    bootstrap.releaseExternalResources();
    channel = null;
  }

  private void handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = ConnectionHeader.decode(buffer);
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }

  @Override
  public void call(RequestType request, ServiceResponseListener<ResponseType> listener) {
    ChannelBuffer wrappedBuffer = ChannelBuffers.wrappedBuffer(serializer.serialize(request));
    responseListeners.add(listener);
    channel.write(wrappedBuffer);
  }

}
