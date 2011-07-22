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

package org.ros.internal.node.topic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.OutgoingMessageQueue;
import org.ros.message.MessageSerializer;
import org.ros.node.topic.Publisher;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 */
public class DefaultPublisher<MessageType> extends DefaultTopic implements Publisher<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  private final List<SubscriberDefinition> subscribers;
  private final OutgoingMessageQueue<MessageType> out;

  public DefaultPublisher(TopicDefinition topicDefinition, MessageSerializer<MessageType> serializer) {
    super(topicDefinition);
    subscribers = Lists.newArrayList();
    out = new OutgoingMessageQueue<MessageType>(serializer);
    out.start();
  }

  @Override
  public void setLatchMode(boolean enabled) {
    out.setLatchMode(enabled);
  }

  @Override
  public void shutdown() {
    out.shutdown();
  }

  public PublisherDefinition toPublisherIdentifier(SlaveIdentifier description) {
    return PublisherDefinition.createPublisherDefinition(description, getTopicDefinition());
  }

  @Override
  public boolean hasSubscribers() {
    return !subscribers.isEmpty();
  }

  @Override
  public int getNumberOfSubscribers() {
    return subscribers.size();
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
  @Override
  public void publish(MessageType message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    out.put(message);
  }

  /**
   * Complete connection handshake on buffer. This generates the connection
   * header for this publisher to send and also updates the connection state of
   * this publisher.
   * 
   * @return encoded connection header from subscriber
   */
  public ChannelBuffer finishHandshake(Map<String, String> incomingHeader) {
    Map<String, String> header = getTopicDefinitionHeader();
    if (DEBUG) {
      log.info("Subscriber handshake header: " + incomingHeader);
      log.info("Publisher handshake header: " + header);
    }
    // TODO(damonkohler): Return error to the subscriber over the wire?
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        header.get(ConnectionHeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
    SubscriberDefinition subscriber = SubscriberDefinition.createFromHeader(incomingHeader);
    subscribers.add(subscriber);
    return ConnectionHeader.encode(header);
  }

  public void addChannel(Channel channel) {
    if (DEBUG) {
      log.info("Adding channel: " + channel);
    }
    out.addChannel(channel);
  }

  @Override
  public String toString() {
    return "Publisher<" + getTopicDefinition() + ">";
  }

}
