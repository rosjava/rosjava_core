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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.ros.concurrent.ListenerCollection;
import org.ros.concurrent.ListenerCollection.SignalRunnable;
import org.ros.internal.node.server.NodeIdentifier;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.OutgoingMessageQueue;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a {@link Publisher}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultPublisher<T> extends DefaultTopicParticipant implements Publisher<T> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  /**
   * The maximum delay before shutdown will begin even if all
   * {@link PublisherListener}s have not yet returned from their
   * {@link PublisherListener#onShutdown(Publisher)} callback.
   */
  private static final long DEFAULT_SHUTDOWN_TIMEOUT = 5;
  private static final TimeUnit DEFAULT_SHUTDOWN_TIMEOUT_UNITS = TimeUnit.SECONDS;

  /**
   * Queue of all messages being published by this {@link Publisher}
   * .org.ros.message.MessageFactory
   */
  private final OutgoingMessageQueue<T> outgoingMessageQueue;
  private final ListenerCollection<PublisherListener<T>> listeners;
  private final NodeIdentifier nodeIdentifier;
  private final MessageFactory messageFactory;

  public DefaultPublisher(NodeIdentifier nodeIdentifier, TopicDeclaration topicDeclaration,
      MessageSerializer<T> serializer, MessageFactory messageFactory,
      ScheduledExecutorService executorService) {
    super(topicDeclaration);
    this.nodeIdentifier = nodeIdentifier;
    this.messageFactory = messageFactory;
    outgoingMessageQueue = new OutgoingMessageQueue<T>(serializer, executorService);
    listeners = new ListenerCollection<PublisherListener<T>>(executorService);
    listeners.add(new DefaultPublisherListener<T>() {
      @Override
      public void onMasterRegistrationSuccess(Publisher<T> registrant) {
        log.info("Publisher registered: " + DefaultPublisher.this);
      }

      @Override
      public void onMasterRegistrationFailure(Publisher<T> registrant) {
        log.info("Publisher registration failed: " + DefaultPublisher.this);
      }

      @Override
      public void onMasterUnregistrationSuccess(Publisher<T> registrant) {
        log.info("Publisher unregistered: " + DefaultPublisher.this);
      }

      @Override
      public void onMasterUnregistrationFailure(Publisher<T> registrant) {
        log.info("Publisher unregistration failed: " + DefaultPublisher.this);
      }
    });
  }

  @Override
  public void setLatchMode(boolean enabled) {
    outgoingMessageQueue.setLatchMode(enabled);
  }

  @Override
  public boolean getLatchMode() {
    return outgoingMessageQueue.getLatchMode();
  }

  @Override
  public void shutdown(long timeout, TimeUnit unit) {
    signalOnShutdown(timeout, unit);
    outgoingMessageQueue.shutdown();
  }

  @Override
  public void shutdown() {
    shutdown(DEFAULT_SHUTDOWN_TIMEOUT, DEFAULT_SHUTDOWN_TIMEOUT_UNITS);
  }

  public PublisherIdentifier getIdentifier() {
    return new PublisherIdentifier(nodeIdentifier, getTopicDeclaration().getIdentifier());
  }

  public PublisherDeclaration getDeclaration() {
    return PublisherDeclaration.newFromNodeIdentifier(nodeIdentifier, getTopicDeclaration());
  }

  @Override
  public boolean hasSubscribers() {
    return outgoingMessageQueue.getNumberOfChannels() > 0;
  }

  @Override
  public int getNumberOfSubscribers() {
    return outgoingMessageQueue.getNumberOfChannels();
  }

  @Override
  public T newMessage() {
    return messageFactory.newFromType(getTopicDeclaration().getMessageType());
  }

  @Override
  public void publish(T message) {
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
    Map<String, String> topicDefinitionHeader = getTopicDeclarationHeader();
    if (DEBUG) {
      log.info("Subscriber handshake header: " + incomingHeader);
      log.info("Publisher handshake header: " + topicDefinitionHeader);
    }
    // TODO(damonkohler): Return errors to the subscriber over the wire.
    String incomingType = incomingHeader.get(ConnectionHeaderFields.TYPE);
    String expectedType = topicDefinitionHeader.get(ConnectionHeaderFields.TYPE);
    boolean messageTypeMatches =
        incomingType.equals(expectedType)
            || incomingType.equals(Subscriber.TOPIC_MESSAGE_TYPE_WILDCARD);
    Preconditions.checkState(messageTypeMatches, "Unexpected message type " + incomingType + " != "
        + expectedType);
    String incomingChecksum = incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM);
    String expectedChecksum = topicDefinitionHeader.get(ConnectionHeaderFields.MD5_CHECKSUM);
    boolean checksumMatches =
        incomingChecksum.equals(expectedChecksum)
            || incomingChecksum.equals(Subscriber.TOPIC_MESSAGE_TYPE_WILDCARD);
    Preconditions.checkState(checksumMatches, "Unexpected message MD5 " + incomingChecksum + " != "
        + expectedChecksum);
    Map<String, String> header = Maps.newHashMap();
    header.putAll(getDeclaration().toConnectionHeader());
    // TODO(damonkohler): Force latch mode to be consistent throughout the life
    // of the publisher.
    header.put(ConnectionHeaderFields.LATCHING, getLatchMode() ? "1" : "0");
    return ConnectionHeader.encode(ImmutableMap.copyOf(header));
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
  public void addListener(PublisherListener<T> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(PublisherListener<T> listener) {
    listeners.remove(listener);
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has
   * successfully registered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationSuccess() {
    final Publisher<T> publisher = this;
    listeners.signal(new SignalRunnable<PublisherListener<T>>() {
      @Override
      public void run(PublisherListener<T> listener) {
        listener.onMasterRegistrationSuccess(publisher);
      }
    });
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has failed
   * to register with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationFailure() {
    final Publisher<T> publisher = this;
    listeners.signal(new SignalRunnable<PublisherListener<T>>() {
      @Override
      public void run(PublisherListener<T> listener) {
        listener.onMasterRegistrationFailure(publisher);
      }
    });
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has
   * successfully unregistered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationSuccess() {
    final Publisher<T> publisher = this;
    listeners.signal(new SignalRunnable<PublisherListener<T>>() {
      @Override
      public void run(PublisherListener<T> listener) {
        listener.onMasterUnregistrationSuccess(publisher);
      }
    });
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has failed
   * to unregister with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationFailure() {
    final Publisher<T> publisher = this;
    listeners.signal(new SignalRunnable<PublisherListener<T>>() {
      @Override
      public void run(PublisherListener<T> listener) {
        listener.onMasterUnregistrationFailure(publisher);
      }
    });
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} has a new
   * {@link Subscriber}.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  private void signalOnNewSubscriber() {
    final Publisher<T> publisher = this;
    listeners.signal(new SignalRunnable<PublisherListener<T>>() {
      @Override
      public void run(PublisherListener<T> listener) {
        listener.onNewSubscriber(publisher);
      }
    });
  }

  /**
   * Signal all {@link PublisherListener}s that the {@link Publisher} is being
   * shut down. Listeners should exit quickly since they may block shut down.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  private void signalOnShutdown(long timeout, TimeUnit unit) {
    final Publisher<T> publisher = this;
    try {
      listeners.signal(new SignalRunnable<PublisherListener<T>>() {
        @Override
        public void run(PublisherListener<T> listener) {
          listener.onShutdown(publisher);
        }
      }, timeout, unit);
    } catch (InterruptedException e) {
      // Ignored since we do not guarantee that all listeners will finish before
      // shutdown begins.
    }
  }

  @Override
  public String toString() {
    return "Publisher<" + getDeclaration() + ">";
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
