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
import org.ros.node.topic.PublisherListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Default implementation of a {@link Publisher}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <MessageType>
 */
public class DefaultPublisher<MessageType> extends DefaultTopic implements Publisher<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  /**
   * Queue of all messages being published by this publisher.
   */
  private final OutgoingMessageQueue<MessageType> outgoingMessages;

  /**
   * All {@link PublisherListener} instances added to the publisher.
   */
  private final List<PublisherListener> publisherListeners =
      new CopyOnWriteArrayList<PublisherListener>();

  /**
   * Th {@link ExecutorService} to be used for all thread creation.
   */
  private final ExecutorService executorService;

  public DefaultPublisher(TopicDefinition topicDefinition,
      MessageSerializer<MessageType> serializer, ExecutorService executorService) {
    super(topicDefinition);
    this.executorService = executorService;

    // TODO(khughes): Use the handed in ExecutorService in the
    // OutgoingMessageQueue.
    outgoingMessages = new OutgoingMessageQueue<MessageType>(serializer);
    outgoingMessages.start();
  }

  @Override
  public void setLatchMode(boolean enabled) {
    outgoingMessages.setLatchMode(enabled);
  }

  @Override
  public void shutdown() {
    outgoingMessages.shutdown();

    signalShutdown();
  }

  public PublisherDefinition toPublisherIdentifier(SlaveIdentifier description) {
    return PublisherDefinition.create(description, getTopicDefinition());
  }

  @Override
  public boolean hasSubscribers() {
    return outgoingMessages.getNumberChannels() > 0;
  }

  @Override
  public int getNumberOfSubscribers() {
    return outgoingMessages.getNumberChannels();
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
  @Override
  public void publish(MessageType message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    outgoingMessages.put(message);
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
    Preconditions.checkState(incomingType.equals(expectedType) || incomingType.equals("*"),
        "Unexpected message type " + incomingType + " != " + expectedType);
    String incomingChecksum = incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM);
    String expectedChecksum = header.get(ConnectionHeaderFields.MD5_CHECKSUM);
    Preconditions.checkState(
        incomingChecksum.equals(expectedChecksum) || incomingChecksum.equals("*"),
        "Unexpected message MD5 " + incomingChecksum + " != " + expectedChecksum);
    return ConnectionHeader.encode(header);
  }

  /**
   * A remote {@link Subscriber} is being added to this publisher.
   * 
   * @param channel
   *          channel for the remote connection
   */
  public void addRemoteConnection(Channel channel) {
    if (DEBUG) {
      log.info("Adding channel: " + channel);
    }
    outgoingMessages.addChannel(channel);
    signalRemoteConnection();
  }

  @Override
  public void addPublisherListener(PublisherListener listener) {
    publisherListeners.add(listener);
  }

  @Override
  public void removePublisherListener(PublisherListener listener) {
    publisherListeners.add(listener);
  }

  /**
   * Notify listeners that the node has been registered.
   * 
   * <p>
   * Done in another thread.
   */
  public void signalRegistrationDone() {
    final Publisher<MessageType> publisher = this;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        for (PublisherListener listener : publisherListeners) {
          listener.onPublisherMasterRegistration(publisher);
        }
      }
    });
  }

  /**
   * Notify listeners that the node has been registered.
   * 
   * <p>
   * Done in another thread.
   */
  private void signalRemoteConnection() {
    final Publisher<MessageType> publisher = this;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        for (PublisherListener listener : publisherListeners) {
          listener.onPublisherRemoteConnection(publisher);
        }
      }
    });
  }

  /**
   * Notify listeners that the node has shutdown.
   * 
   * <p>
   * Done in another thread.
   */
  private void signalShutdown() {
    final Publisher<MessageType> publisher = this;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        for (PublisherListener listener : publisherListeners) {
          listener.onPublisherShutdown(publisher);
        }
      }
    });
  }

  @Override
  public String toString() {
    return "Publisher<" + getTopicDefinition() + ">";
  }

}
