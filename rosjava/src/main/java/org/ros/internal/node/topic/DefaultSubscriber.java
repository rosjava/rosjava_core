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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.ros.concurrent.ListenerCollection;
import org.ros.concurrent.ListenerCollection.SignalRunnable;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.IncomingMessageQueue;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpClientConnectionManager;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link Subscriber}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultSubscriber<MessageType> extends DefaultTopic implements Subscriber<MessageType> {

  /**
   * The maximum delay before shutdown will begin even if all
   * {@link SubscriberListener}s have not yet returned from their
   * {@link SubscriberListener#onShutdown(Subscriber)} callback.
   */
  private static final int DEFAULT_SHUTDOWN_TIMEOUT = 5;
  private static final TimeUnit DEFAULT_SHUTDOWN_TIMEOUT_UNITS = TimeUnit.SECONDS;

  private final ExecutorService executorService;
  private final ImmutableMap<String, String> header;
  private final IncomingMessageQueue<MessageType> incomingMessageQueue;
  private final Set<PublisherIdentifier> knownPublishers;
  private final SlaveIdentifier slaveIdentifier;
  private final TcpClientConnectionManager tcpClientConnectionManager;

  /**
   * All {@link SubscriberListener} instances added to the subscriber.
   */
  private final ListenerCollection<SubscriberListener> subscriberListeners;

  public static <S> DefaultSubscriber<S> create(SlaveIdentifier slaveIdentifier,
      TopicDefinition description, ExecutorService executorService,
      MessageDeserializer<S> deserializer) {
    return new DefaultSubscriber<S>(slaveIdentifier, description, deserializer, executorService);
  }

  private DefaultSubscriber(SlaveIdentifier slaveIdentifier, TopicDefinition topicDefinition,
      MessageDeserializer<MessageType> deserializer, ExecutorService executorService) {
    super(topicDefinition);
    this.executorService = executorService;
    this.subscriberListeners = new ListenerCollection<SubscriberListener>(executorService);
    this.incomingMessageQueue =
        new IncomingMessageQueue<MessageType>(deserializer, executorService);
    this.slaveIdentifier = slaveIdentifier;
    header =
        ImmutableMap.<String, String>builder().putAll(slaveIdentifier.toHeader())
            .putAll(topicDefinition.toHeader()).build();
    knownPublishers = Sets.newHashSet();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
  }

  public Collection<String> getSupportedProtocols() {
    return ProtocolNames.SUPPORTED;
  }

  @Override
  public boolean getLatchMode() {
    return incomingMessageQueue.getLatchMode();
  }

  @Override
  public void addMessageListener(MessageListener<MessageType> listener) {
    incomingMessageQueue.addListener(listener);
  }

  @Override
  public void removeMessageListener(MessageListener<MessageType> listener) {
    incomingMessageQueue.removeListener(listener);
  }

  @VisibleForTesting
  public synchronized void addPublisher(PublisherIdentifier publisherIdentifier,
      InetSocketAddress address) {
    // TODO(damonkohler): If the connection is dropped, knownPublishers should
    // be updated.
    if (knownPublishers.contains(publisherIdentifier)) {
      return;
    }
    tcpClientConnectionManager.connect(toString(), address,
        new SubscriberHandshakeHandler<MessageType>(header, incomingMessageQueue),
        "SubscriberHandshakeHandler");
    knownPublishers.add(publisherIdentifier);
    signalOnNewPublisher();
  }

  /**
   * Updates the list of {@link DefaultPublisher}s for the topic that this
   * {@link DefaultSubscriber} is interested in.
   * 
   * @param publishers
   *          {@link List} of {@link PublisherIdentifier}s for the subscribed
   *          topic
   */
  public void updatePublishers(Collection<PublisherIdentifier> publishers) {
    for (final PublisherIdentifier publisher : publishers) {
      executorService.execute(new UpdatePublisherRunnable<MessageType>(this, this.slaveIdentifier,
          publisher));
    }
  }

  @Override
  public void shutdown(long timeout, TimeUnit unit) {
    signalShutdown();
    incomingMessageQueue.shutdown();
    tcpClientConnectionManager.shutdown();
  }

  @Override
  public void shutdown() {
    shutdown(DEFAULT_SHUTDOWN_TIMEOUT, DEFAULT_SHUTDOWN_TIMEOUT_UNITS);
  }

  @Override
  public void addSubscriberListener(SubscriberListener listener) {
    subscriberListeners.add(listener);
  }

  @Override
  public void removeSubscriberListener(SubscriberListener listener) {
    subscriberListeners.add(listener);
  }

  /**
   * Signal all {@link SubscriberListener}s that the {@link Subscriber} has
   * successfully registered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationSuccess() {
    final Subscriber<MessageType> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
      @Override
      public void run(SubscriberListener listener) {
        listener.onMasterRegistrationSuccess(subscriber);
      }
    });
  }

  /**
   * Signal all {@link SubscriberListener}s that the {@link Subscriber} has
   * failed to register with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterRegistrationFailure() {
    final Subscriber<MessageType> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
      @Override
      public void run(SubscriberListener listener) {
        listener.onMasterRegistrationFailure(subscriber);
      }
    });
  }

  /**
   * Signal all {@link SubscriberListener}s that the {@link Subscriber} has
   * successfully unregistered with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationSuccess() {
    final Subscriber<MessageType> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
      @Override
      public void run(SubscriberListener listener) {
        listener.onMasterUnregistrationSuccess(subscriber);
      }
    });
  }

  /**
   * Signal all {@link SubscriberListener}s that the {@link Subscriber} has
   * failed to unregister with the master.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  @Override
  public void signalOnMasterUnregistrationFailure() {
    final Subscriber<MessageType> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
      @Override
      public void run(SubscriberListener listener) {
        listener.onMasterUnregistrationFailure(subscriber);
      }
    });
  }

  /**
   * Signal all {@link SubscriberListener}s that a new {@link Publisher} has
   * connected.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  public void signalOnNewPublisher() {
    final Subscriber<MessageType> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
      @Override
      public void run(SubscriberListener listener) {
        listener.onNewPublisher(subscriber);
      }
    });
  }

  /**
   * Signal all {@link SubscriberListener}s that the {@link Subscriber} has shut
   * down.
   * 
   * <p>
   * Each listener is called in a separate thread.
   */
  private void signalShutdown() {
    final Subscriber<MessageType> subscriber = this;
    try {
      subscriberListeners.signal(new SignalRunnable<SubscriberListener>() {
        @Override
        public void run(SubscriberListener listener) {
          listener.onShutdown(subscriber);
        }
      }, DEFAULT_SHUTDOWN_TIMEOUT, DEFAULT_SHUTDOWN_TIMEOUT_UNITS);
    } catch (InterruptedException e) {
      // Ignored since we do not guarantee that all listeners will finish before
      // shutdown begins.
    }
  }

  @Override
  public String toString() {
    return "Subscriber<" + getTopicDefinition() + ">";
  }

  @Override
  public void setQueueLimit(int limit) {
    incomingMessageQueue.setLimit(limit);
  }

  @Override
  public int getQueueLimit() {
    return incomingMessageQueue.getLimit();
  }
}
