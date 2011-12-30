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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpClientPipelineFactory;
import org.ros.message.MessageDeserializer;
import org.ros.node.service.ServiceResponseListener;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <RequestType>
 * @param <ResponseType>
 */
class ServiceClientHandshakeHandler<RequestType, ResponseType> extends SimpleChannelHandler {

  private final ImmutableMap<String, String> header;
  private final Queue<ServiceResponseListener<ResponseType>> responseListeners;
  private final MessageDeserializer<ResponseType> deserializer;
  private final ExecutorService executorService;

  public ServiceClientHandshakeHandler(ImmutableMap<String, String> header,
      Queue<ServiceResponseListener<ResponseType>> responseListeners,
      MessageDeserializer<ResponseType> deserializer, ExecutorService executorService) {
    this.header = header;
    this.responseListeners = responseListeners;
    this.deserializer = deserializer;
    this.executorService = executorService;
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    e.getChannel().write(ConnectionHeader.encode(header));
    super.channelConnected(ctx, e);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
    // TODO(damonkohler): Handle handshake errors.
    handshake(incomingBuffer);
    ChannelPipeline pipeline = e.getChannel().getPipeline();
    pipeline.remove(TcpClientPipelineFactory.LENGTH_FIELD_BASED_FRAME_DECODER);
    pipeline.remove(this);
    pipeline.addLast("ResponseDecoder", new ServiceResponseDecoder<ResponseType>());
    pipeline.addLast("ResponseHandler", new ServiceResponseHandler<ResponseType>(responseListeners,
        deserializer, executorService));
    super.messageReceived(ctx, e);
  }

  private void handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = ConnectionHeader.decode(buffer);
    if (DefaultServiceClient.DEBUG) {
      DefaultServiceClient.log.info("Incoming handshake header: " + incomingHeader);
      DefaultServiceClient.log.info("Expected handshake header: " + header);
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
  }
}