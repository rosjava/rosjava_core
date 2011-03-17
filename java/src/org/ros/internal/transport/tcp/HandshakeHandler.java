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
package org.ros.internal.transport.tcp;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.service.ServiceResponseEncoder;
import org.ros.internal.service.ServiceServer;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.TopicManager;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.SimplePipelineFactory;
import org.ros.message.Message;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class HandshakeHandler extends SimpleChannelHandler {
  
  private static final Log log = LogFactory.getLog(HandshakeHandler.class);
  
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;

  public HandshakeHandler(TopicManager topicManager, ServiceManager serviceManager) {
    this.topicManager = topicManager;
    this.serviceManager = serviceManager;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();

    // TODO(kwc): move startHandshake elsewhere as both topic/services share
    // this.
    Map<String, String> incomingHeader = ConnectionHeader.decode(incomingBuffer);

    if (incomingHeader.containsKey(ConnectionHeaderFields.SERVICE)) {
      String serviceName = incomingHeader.get(ConnectionHeaderFields.SERVICE);
      Preconditions.checkState(serviceManager.hasService(serviceName));
      ServiceServer<?> serviceServer = serviceManager.getService(serviceName);

      ChannelBuffer outgoingBuffer = serviceServer.finishHandshake(incomingHeader);
      if (outgoingBuffer == null) {
        // This is just a probe.
        e.getChannel().close();
      } else {
        e.getChannel().write(outgoingBuffer);
        ChannelPipeline pipeline = e.getChannel().getPipeline();
        pipeline.replace(SimplePipelineFactory.LENGTH_FIELD_PREPENDER, "Response Encoder",
            new ServiceResponseEncoder());
        pipeline.replace(this, "Request Handler", serviceServer.createRequestHandler());
      }
    } else {
      String topicName = incomingHeader.get(ConnectionHeaderFields.TOPIC);
      Preconditions.checkState(topicManager.hasPublisher(topicName));
      Publisher<? extends Message> publisher = topicManager.getPublisher(topicName);
      ChannelBuffer outgoingBuffer = publisher.finishHandshake(incomingHeader);
      Channel channel = ctx.getChannel();
      channel.write(outgoingBuffer).await();
      publisher.addChannel(channel);
    }

    // TODO(damonkohler): What happens if the client doesn't like the handshake?
    // TODO(damonkohler): Replace this handler with a discard handler in the
    // pipeline.
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    log.error("Incomming connection failed.", e.getCause());
    e.getChannel().close();
  }

}
