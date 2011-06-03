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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.ros.MessageDeserializer;
import org.ros.ServiceResponseListener;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpClientPipelineFactory;
import org.ros.message.Message;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceClient<ResponseMessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(ServiceClient.class);

  final MessageDeserializer<ResponseMessageType> deserializer;
  final Queue<ServiceResponseListener<ResponseMessageType>> responseListeners;
  private final Map<String, String> header;
  private final ChannelFactory channelFactory;
  private final ClientBootstrap bootstrap;

  private Channel channel;

  enum DecodingState {
    ERROR_CODE, MESSAGE_LENGTH, MESSAGE
  }

  private final class HandshakeHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
      // TODO(damonkohler): Handle handshake errors.
      handshake(incomingBuffer);
      ChannelPipeline pipeline = e.getChannel().getPipeline();
      pipeline.remove(TcpClientPipelineFactory.LENGTH_FIELD_BASED_FRAME_DECODER);
      pipeline.remove(this);
      pipeline.addLast("ResponseDecoder", new ServiceResponseDecoder<ResponseMessageType>());
      pipeline.addLast("ResponseHandler", new ServiceResponseHandler<ResponseMessageType>(
          responseListeners, deserializer));
    }
  }

  public static <T> ServiceClient<T> create(GraphName nodeName,
      ServiceIdentifier serviceIdentifier, MessageDeserializer<T> deserializer) {
    return new ServiceClient<T>(nodeName, serviceIdentifier, deserializer);
  }

  private ServiceClient(GraphName nodeName, ServiceIdentifier serviceIdentifier,
      MessageDeserializer<ResponseMessageType> deserializer) {
    this.deserializer = deserializer;
    responseListeners = Lists.newLinkedList();
    header =
        ImmutableMap.<String, String>builder()
            .put(ConnectionHeaderFields.CALLER_ID, nodeName.toString())
            // TODO(damonkohler): Support non-persistent connections.
            .put(ConnectionHeaderFields.PERSISTENT, "1").putAll(serviceIdentifier.toHeader())
            .build();
    channelFactory =
        new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    bootstrap = new ClientBootstrap(channelFactory);
    TcpClientPipelineFactory factory = new TcpClientPipelineFactory() {
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

  public void shutdown() {
    channel.close().awaitUninterruptibly();
    channelFactory.releaseExternalResources();
    bootstrap.releaseExternalResources();
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

  /**
   * @param message
   */
  public void call(Message message, ServiceResponseListener<ResponseMessageType> listener) {
    // TODO(damonkohler): Make use of sequence number.
    ChannelBuffer buffer =
        ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, message.serialize(0));
    responseListeners.add(listener);
    channel.write(buffer);
  }

}
