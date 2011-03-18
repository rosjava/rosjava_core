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

import org.jboss.netty.channel.Channel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.OutgoingMessageQueue;
import org.ros.message.Message;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 *
 * @param <MessageType>
 */
public class Publisher<MessageType extends Message> extends Topic<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final List<SubscriberIdentifier> subscribers;
  private final OutgoingMessageQueue out;

  public Publisher(TopicDefinition description, Class<MessageType> messageClass) {
    super(description, messageClass);
    subscribers = Lists.newArrayList();
    out = new OutgoingMessageQueue();
    out.start();
  }

  public void shutdown() {
    try {
      out.shutdown();
    } catch (InterruptedException e) {
      log.error("Failed to shutdown outgoing message queue.", e);
    }
  }

  public PublisherIdentifier toPublisherIdentifier(SlaveIdentifier description) {
    return new PublisherIdentifier(description, getTopicDefinition());
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
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
    SubscriberIdentifier subscriber = SubscriberIdentifier.createFromHeader(incomingHeader);
    subscribers.add(subscriber);
    return ConnectionHeader.encode(header);
  }

  public void addChannel(Channel channel) {
    if (DEBUG) {
      log.info("Adding channel: " + channel);
    }
    out.addChannel(channel);
  }

}
