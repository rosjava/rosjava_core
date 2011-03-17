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

package org.ros.internal.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.jboss.netty.channel.ChannelHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.topic.Publisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.message.Message;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public abstract class ServiceServer<RequestMessageType extends Message> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final Class<RequestMessageType> requestMessageClass;
  private final ServiceDefinition definition;
  private final Map<String, String> header;
  private final String name;
  private InetSocketAddress address;

  public final class RequestHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      RequestMessageType request = requestMessageClass.newInstance();
      ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
      request.deserialize(buffer.toByteBuffer());
      Message responseMessage = buildResponse(request);
      // TODO(damonkohler): Support sequence number.
      ChannelBuffer responseBuffer = ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN,
          responseMessage.serialize(0));
      ServiceServerResponse response = new ServiceServerResponse();
      // TODO(damonkohler): Support changing error code.
      response.setErrorCode(1);
      response.setMessageLength(responseBuffer.readableBytes());
      response.setMessage(responseBuffer);
      ctx.getChannel().write(response);
    }
  }

  public ServiceServer(Class<RequestMessageType> requestMessageClass, String name,
      ServiceDefinition definition) {
    this.requestMessageClass = requestMessageClass;
    this.name = name;
    this.definition = definition;
    header = ImmutableMap.<String, String>builder().put(ConnectionHeaderFields.SERVICE, name)
        .putAll(definition.toHeader()).build();
  }

  public abstract Message buildResponse(RequestMessageType requestMessage);

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

  public void setAddress(InetSocketAddress address) {
    this.address = address;
  }

  public URI getUri() throws URISyntaxException {
    Preconditions.checkState(address != null);
    return new URI("rosrpc://" + address.getHostName() + ":" + address.getPort());
  }

  public ServiceDefinition getServiceDefinition() {
    return definition;
  }

  public String getName() {
    return name;
  }

  public ChannelHandler createRequestHandler() {
    return new RequestHandler();
  }
}
