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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.service.ServiceResponseEncoder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.util.Map;

/**
 * A {@link ChannelHandler} which will process the TCP server handshake.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TcpServerHandshakeHandler extends SimpleChannelHandler {
  
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;

  public TcpServerHandshakeHandler(TopicManager topicManager, ServiceManager serviceManager) {
    this.topicManager = topicManager;
    this.serviceManager = serviceManager;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer incomingBuffer = (ChannelBuffer) e.getMessage();

    // TODO(kwc): move startHandshake elsewhere as both topic/services share
    // this.
    Map<String, String> incomingHeader = ConnectionHeader.decode(incomingBuffer);

    ChannelPipeline pipeline = e.getChannel().getPipeline();
    if (incomingHeader.containsKey(ConnectionHeaderFields.SERVICE)) {
      String serviceName = incomingHeader.get(ConnectionHeaderFields.SERVICE);
      Preconditions.checkState(serviceManager.hasServer(serviceName));
      ServiceServer<?, ?> serviceServer = serviceManager.getServer(serviceName);
      ChannelBuffer outgoingBuffer = serviceServer.finishHandshake(incomingHeader);
      if (outgoingBuffer == null) {
        // This is just a probe.
        e.getChannel().close();
      } else {
        e.getChannel().write(outgoingBuffer);
        pipeline.replace(TcpServerPipelineFactory.LENGTH_FIELD_PREPENDER, "ServiceResponseEncoder",
            new ServiceResponseEncoder());
        pipeline.replace(this, "ServiceRequestHandler", serviceServer.createRequestHandler());
      }
    } else {
      Preconditions.checkState(incomingHeader.containsKey(ConnectionHeaderFields.TOPIC));
      String topicName = incomingHeader.get(ConnectionHeaderFields.TOPIC);
      Preconditions.checkState(topicManager.hasPublisher(topicName));
      Publisher<?> publisher = topicManager.getPublisher(topicName);
      ChannelBuffer outgoingBuffer = publisher.finishHandshake(incomingHeader);
      Channel channel = ctx.getChannel();
      ChannelFuture future = channel.write(outgoingBuffer).await();
      if (!future.isSuccess()) {
        throw new RuntimeException(future.getCause());
      }
      publisher.addChannel(channel);
      
      // Once the handshake is complete, there will be nothing incoming on the
      // channel. Replace the handshake handler with a handler which will
      // drop everything.
      pipeline.replace(this, "DiscardHandler", new SimpleChannelHandler());
    }
  }
  
}
