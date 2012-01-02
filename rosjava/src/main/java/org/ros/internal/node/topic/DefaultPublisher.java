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
import org.ros.node.topic.Subscriber;

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
  private final OutgoingMessageQueue<MessageType> outgoingMessageQueue;

  /**
   * All {@link PublisherListener} instances added to the publisher.
   */
  private final List<PublisherListener> publisherListeners;

  /**
   * The {@link ExecutorService} to be used for all thread creation.
   */
  private final ExecutorService executorService;

  public DefaultPublisher(TopicDefinition topicDefinition,
      MessageSerializer<MessageType> serializer, ExecutorService executorService) {
    super(topicDefinition);
    this.executorService = executorService;
    publisherListeners = new CopyOnWriteArrayList<PublisherListener>();
    outgoingMessageQueue = new OutgoingMessageQueue<MessageType>(serializer, executorService);
  }

  @Override
  public void setLatchMode(boolean enabled) {
    outgoingMessageQueue.setLatchMode(enabled);
  }

  @Override
  public void shutdown() {
    outgoingMessageQueue.shutdown();
    signalShutdown();
  }

  public PublisherIdentifier toIdentifier(SlaveIdentifier slaveIdentifier) {
    return new PublisherIdentifier(slaveIdentifier, getTopicDefinition().toIdentifier());
  }

  public PublisherDefinition toDefinition(SlaveIdentifier slaveIdentifier) {
    return PublisherDefinition.create(slaveIdentifier, getTopicDefinition());
  }

  @Override
  public boolean hasSubscribers() {
    return outgoingMessageQueue.getNumberOfChannels() > 0;
  }

  @Override
  public int getNumberOfSubscribers() {
    return outgoingMessageQueue.getNumberOfChannels();
  }

  // TODO(damonkohler): Recycle Message objects to avoid GC.
  @Override
  public void publish(MessageType message) {
    if (DEBUG) {
      log.info("Publishing message: " + message);
    }
    outgoingMessageQueue.put(message);
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
   * Add a {@link Subscriber} connection to this {@link Publisher}.
   * 
   * @param channel
   *          the communication {@link Channel} to the {@link Subscriber}
   */
  public void addSubscriberChannel(Channel channel) {
    if (DEBUG) {
      log.info("Adding channel: " + channel);
    }
    outgoingMessageQueue.addChannel(channel);
    signalOnNewSubscriber();
  }

  @Override
  public void addPublisherListener(PublisherListener listener) {
    publisherListeners.add(listener);
  }

  @Override
  public void removePublisherListener(PublisherListener listener) {
    publisherListeners.remove(listener);
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has been
   * successfully registered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationSuccess() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterRegistrationSuccess(publisher);
        }
      });
    }
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has been
   * successfully registered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationFailure() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterRegistrationFailure(publisher);
        }
      });
    }
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has been
   * successfully unregistered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationSuccess() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterUnregistrationSuccess(publisher);
        }
      });
    }
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has been
   * successfully unregistered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationFailure() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterUnregistrationFailure(publisher);
        }
      });
    }
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has a new
   * {@link Subscriber}.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  private void signalOnNewSubscriber() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onNewSubscriber(publisher);
        }
      });
    }
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has been
   * shutdown.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  private void signalShutdown() {
    final Publisher<MessageType> publisher = this;
    for (final PublisherListener listener : publisherListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onShutdown(publisher);
        }
      });
    }
  }

  @Override
  public String toString() {
    return "Publisher<" + getTopicDefinition() + ">";
  }

  @Override
  public void setQueueLimit(int limit) {
    outgoingMessageQueue.setLimit(limit);
  }

  @Override
  public int getQueueLimit() {
    return outgoingMessageQueue.getLimit();
  }
}
