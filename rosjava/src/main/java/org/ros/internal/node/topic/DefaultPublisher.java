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

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 */
public class DefaultPublisher<MessageType> extends DefaultTopic implements Publisher<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  private final OutgoingMessageQueue<MessageType> out;

  public DefaultPublisher(TopicDefinition topicDefinition, MessageSerializer<MessageType> serializer) {
    super(topicDefinition);
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
    return PublisherDefinition.create(description, getTopicDefinition());
  }

  @Override
  public boolean hasSubscribers() {
    return out.getChannelGroupSize() > 0;
  }

  @Override
  public int getNumberOfSubscribers() {
    return out.getChannelGroupSize();
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
    String incomingType = incomingHeader.get(ConnectionHeaderFields.TYPE);
    String expectedType = header.get(ConnectionHeaderFields.TYPE);
    Preconditions.checkState(incomingType.equals(expectedType), "Unexpected message type "
        + incomingType + " != " + expectedType);
    String incomingChecksum = incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM);
    String expectedChecksum = header.get(ConnectionHeaderFields.MD5_CHECKSUM);
    Preconditions.checkState(incomingChecksum.equals(expectedChecksum), "Unexpected message MD5 "
        + incomingChecksum + " != " + expectedChecksum);
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
