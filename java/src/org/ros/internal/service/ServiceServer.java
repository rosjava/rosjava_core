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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.ros.internal.topic.Publisher;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.NettyConnectionHeader;
import org.ros.internal.transport.SimplePipelineFactory;
import org.ros.internal.transport.tcp.TcpServer;
import org.ros.message.Message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

  private static final int ESTIMATED_RESPONSE_SIZE = 8192;

  private final TcpServer server;
  private final Class<RequestMessageType> requestMessageClass;
  private final ServiceDefinition definition;
  private final Map<String, String> header;
  private final String name;

  private final class ResponseEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
        throws Exception {
      if (msg instanceof ServiceServerResponse) {
        ServiceServerResponse response = (ServiceServerResponse) msg;
        ChannelBuffer buffer =
            ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, ESTIMATED_RESPONSE_SIZE);
        buffer.writeByte(response.getErrorCode());
        buffer.writeInt(response.getMessageLength());
        buffer.writeBytes(response.getMessage());
        return buffer;
      } else {
        return msg;
      }
    }
  }

  private final class HandshakeHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();
      ChannelBuffer outgoingBuffer = handshake(incomingBuffer);
      if (outgoingBuffer == null) {
        // This is just a probe.
        e.getChannel().close();
      } else {
        e.getChannel().write(outgoingBuffer);
        ChannelPipeline pipeline = e.getChannel().getPipeline();
        pipeline.replace(SimplePipelineFactory.LENGTH_FIELD_PREPENDER, "Response Encoder",
            new ResponseEncoder());
        pipeline.replace(this, "Request Handler", new RequestHandler());
      }
    }
  }

  private final class RequestHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      RequestMessageType request = requestMessageClass.newInstance();
      ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
      request.deserialize(buffer.toByteBuffer());
      Message responseMessage = buildResponse(request);
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

  public ServiceServer(Class<RequestMessageType> requestMessageClass, String name,
      ServiceDefinition definition) {
    this.requestMessageClass = requestMessageClass;
    this.name = name;
    this.definition = definition;
    SimplePipelineFactory factory = new SimplePipelineFactory();
    factory.getPipeline().addLast("Handshake Handler", new HandshakeHandler());
    server = new TcpServer(factory);
    header =
        ImmutableMap.<String, String>builder().put(ConnectionHeaderFields.SERVICE, name)
            .putAll(definition.toHeader()).build();
  }

  public abstract Message buildResponse(RequestMessageType requestMessage);

  @VisibleForTesting
  ChannelBuffer handshake(ChannelBuffer buffer) {
    Map<String, String> incomingHeader = NettyConnectionHeader.decode(buffer);
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Outgoing handshake header: " + header);
    }
    if (incomingHeader.containsKey(ConnectionHeaderFields.PROBE)) {
      // TODO(damonkohler): This is kind of a lousy way to pass back the
      // information that this is a probe.
      return null;
    }
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    return NettyConnectionHeader.encode(header);
  }

  public void start(SocketAddress address) {
    server.start(address);
  }

  public void shutdown() {
    server.shutdown();
  }

  public InetSocketAddress getAddress() {
    return server.getAddress();
  }

  public URI getUri() throws URISyntaxException {
    return new URI("rosrpc://" + server.getAddress().getHostName() + ":"
        + server.getAddress().getPort());
  }

  public ServiceDefinition getServiceDefinition() {
    return definition;
  }

  public String getName() {
    return name;
  }

}
