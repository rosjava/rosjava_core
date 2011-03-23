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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.message.Message;

import java.net.URI;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceServer<RequestMessageType extends Message> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final AdvertiseAddress advertiseAddress;
  private final Class<RequestMessageType> requestMessageClass;
  private final ServiceResponseBuilder<RequestMessageType> responseBuilder;
  private final ServiceDefinition definition;
  private final Map<String, String> header;

  public final class RequestHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      RequestMessageType request = requestMessageClass.newInstance();
      ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
      request.deserialize(buffer.toByteBuffer());
      Message responseMessage = responseBuilder.build(request);
      // TODO(damonkohler): Support sequence number.
      ChannelBuffer responseBuffer =
          ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, responseMessage.serialize(0));
      ServiceServerResponse response = new ServiceServerResponse();
      // TODO(damonkohler): Support changing error code.
      response.setErrorCode(1);
      response.setMessageLength(responseBuffer.readableBytes());
      response.setMessage(responseBuffer);
      ctx.getChannel().write(response);
    }
  }

  public ServiceServer(ServiceDefinition definition, Class<RequestMessageType> requestMessageClass,
      ServiceResponseBuilder<RequestMessageType> responseBuilder, AdvertiseAddress advertiseAddress) {
    this.requestMessageClass = requestMessageClass;
    this.responseBuilder = responseBuilder;
    this.definition = definition;
    this.advertiseAddress = advertiseAddress;
    header =
        ImmutableMap.<String, String>builder()
            .put(ConnectionHeaderFields.SERVICE, definition.getName().toString())
            .putAll(definition.toHeader()).build();
  }

  public ChannelBuffer finishHandshake(Map<String, String> incomingHeader) {
    if (DEBUG) {
      log.info("Outgoing handshake header: " + header);
    }
    if (incomingHeader.containsKey(ConnectionHeaderFields.PROBE)) {
      // TODO(damonkohler): This is kind of a lousy way to pass back the
      // information that this is a probe.
      return null;
    } else {
      Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
          header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
      return ConnectionHeader.encode(header);
    }
  }

  public URI getUri() {
    return advertiseAddress.toUri("rosrpc");
  }

  public GraphName getName() {
    return definition.getName();
  }

  public ChannelHandler createRequestHandler() {
    return new RequestHandler();
  }

  public boolean checkMessageClass(Class<RequestMessageType> expectedRequestMessageClass) {
    return expectedRequestMessageClass == requestMessageClass;
  }
  
}
