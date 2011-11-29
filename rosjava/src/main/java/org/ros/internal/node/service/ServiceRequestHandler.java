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

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class ServiceRequestHandler<RequestType, ResponseType> extends SimpleChannelHandler {

  private final MessageDeserializer<RequestType> deserializer;
  private final MessageSerializer<ResponseType> serializer;
  private final ServiceResponseBuilder<RequestType, ResponseType> responseBuilder;

  ServiceRequestHandler(MessageDeserializer<RequestType> deserializer,
      MessageSerializer<ResponseType> serializer,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) {
    this.deserializer = deserializer;
    this.serializer = serializer;
    this.responseBuilder = responseBuilder;
  }

  private ByteBuffer handleRequest(ByteBuffer buffer) throws ServiceException {
    RequestType request = deserializer.deserialize(buffer);
    ResponseType response = responseBuilder.build(request);
    return serializer.serialize(response);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer requestBuffer = (ChannelBuffer) e.getMessage();
    ServiceServerResponse response = new ServiceServerResponse();
    ChannelBuffer responseBuffer;
    try {
      responseBuffer =
          ChannelBuffers.wrappedBuffer(handleRequest(requestBuffer.toByteBuffer()));
    } catch (ServiceException ex) {
      response.setErrorCode(0);
      response.setMessageLength(ex.getMessage().length());
      response.setMessage(ex.getMessageAsChannelBuffer());
      ctx.getChannel().write(response);
      return;
    }
    response.setErrorCode(1);
    response.setMessageLength(responseBuffer.readableBytes());
    response.setMessage(responseBuffer);
    ctx.getChannel().write(response);
    super.messageReceived(ctx, e);
  }

}
