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

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.topic.TopicManager;
import org.ros.internal.transport.ConnectionTrackingHandler;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpServerPipelineFactory implements ChannelPipelineFactory {

  public static final String LENGTH_FIELD_BASED_FRAME_DECODER = "LengthFieldBasedFrameDecoder";
  public static final String LENGTH_FIELD_PREPENDER = "LengthFieldPrepender";
  public static final String HANDSHAKE_HANDLER = "HandshakeHandler";
  public static final String CONNECTION_TRACKING_HANDLER = "ConnectionTrackingHandler";

  private final ConnectionTrackingHandler connectionTrackingHandler;
  private final TopicManager topicManager;
  private final ServiceManager serviceManager;

  public TcpServerPipelineFactory(ChannelGroup channelGroup, TopicManager topicManager,
      ServiceManager serviceManager) {
    this.connectionTrackingHandler = new ConnectionTrackingHandler(channelGroup);
    this.topicManager = topicManager;
    this.serviceManager = serviceManager;
  }

  @Override
  public ChannelPipeline getPipeline() {
    ChannelPipeline pipeline = pipeline();
    pipeline.addLast(LENGTH_FIELD_PREPENDER, new LengthFieldPrepender(4));
    pipeline.addLast(LENGTH_FIELD_BASED_FRAME_DECODER, new LengthFieldBasedFrameDecoder(
        Integer.MAX_VALUE, 0, 4, 0, 4));
    pipeline.addLast(CONNECTION_TRACKING_HANDLER, connectionTrackingHandler);
    pipeline.addLast(HANDSHAKE_HANDLER, new TcpServerHandshakeHandler(topicManager, serviceManager));
    return pipeline;
  }

}
