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
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.concurrent.ListenerCollection;
import org.ros.concurrent.ListenerCollection.SignalRunnable;
import org.ros.internal.node.server.NodeSlaveIdentifier;
import org.ros.internal.transport.IncomingMessageQueue;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpClientConnectionManager;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageListener;
import org.ros.node.topic.DefaultSubscriberListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a {@link Subscriber}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultSubscriber<T> extends DefaultTopic implements Subscriber<T> {

  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  /**
   * The maximum delay before shutdown will begin even if all
   * {@link SubscriberListener}s have not yet returned from their
   * {@link SubscriberListener#onShutdown(Subscriber)} callback.
   */
  private static final int DEFAULT_SHUTDOWN_TIMEOUT = 5;
  private static final TimeUnit DEFAULT_SHUTDOWN_TIMEOUT_UNITS = TimeUnit.SECONDS;

  private final NodeSlaveIdentifier slaveIdentifier;
  private final ScheduledExecutorService executorService;
  private final IncomingMessageQueue<T> incomingMessageQueue;
  private final Set<PublisherIdentifier> knownPublishers;
  private final TcpClientConnectionManager tcpClientConnectionManager;

  /**
   * Manages the {@link SubscriberListener}s for this {@link Subscriber}.
   */
  private final ListenerCollection<SubscriberListener<T>> subscriberListeners;

  public static <S> DefaultSubscriber<S> newDefault(NodeSlaveIdentifier slaveIdentifier,
      TopicDefinition description, ScheduledExecutorService executorService,
      MessageDeserializer<S> deserializer) {
    return new DefaultSubscriber<S>(slaveIdentifier, description, deserializer, executorService);
  }

  private DefaultSubscriber(NodeSlaveIdentifier slaveIdentifier, TopicDefinition topicDefinition,
      MessageDeserializer<T> deserializer, ScheduledExecutorService executorService) {
    super(topicDefinition);
    this.slaveIdentifier = slaveIdentifier;
    this.executorService = executorService;
    incomingMessageQueue = new IncomingMessageQueue<T>(deserializer, executorService);
    knownPublishers = Sets.newHashSet();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
    subscriberListeners = new ListenerCollection<SubscriberListener<T>>(executorService);
    subscriberListeners.add(new DefaultSubscriberListener<T>() {
      @Override
      public void onMasterRegistrationSuccess(Subscriber<T> registrant) {
        log.info("Subscriber registered: " + DefaultSubscriber.this);
      }

      @Override
      public void onMasterRegistrationFailure(Subscriber<T> registrant) {
        log.info("Subscriber registration failed: " + DefaultSubscriber.this);
      }

      @Override
      public void onMasterUnregistrationSuccess(Subscriber<T> registrant) {
        log.info("Subscriber unregistered: " + DefaultSubscriber.this);
      }

      @Override
      public void onMasterUnregistrationFailure(Subscriber<T> registrant) {
        log.info("Subscriber unregistration failed: " + DefaultSubscriber.this);
      }
    });
  }

  public SubscriberIdentifier toIdentifier() {
    return new SubscriberIdentifier(slaveIdentifier, getTopicDefinition().toIdentifier());
  }

  public SubscriberDefinition toDefinition() {
    return new SubscriberDefinition(toIdentifier(), getTopicDefinition());
  }

  public Collection<String> getSupportedProtocols() {
    return ProtocolNames.SUPPORTED;
  }

  @Override
  public boolean getLatchMode() {
    return incomingMessageQueue.getLatchMode();
  }

  @Override
  public void addMessageListener(MessageListener<T> listener) {
    incomingMessageQueue.addListener(listener);
  }

  @Override
  public void removeMessageListener(MessageListener<T> listener) {
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
    tcpClientConnectionManager.connect(toString(), address, new SubscriberHandshakeHandler<T>(
        toDefinition().toHeader(), incomingMessageQueue), "SubscriberHandshakeHandler");
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
      executorService
          .execute(new UpdatePublisherRunnable<T>(this, this.slaveIdentifier, publisher));
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
  public void addSubscriberListener(SubscriberListener<T> listener) {
    subscriberListeners.add(listener);
  }

  @Override
  public void removeSubscriberListener(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
      @Override
      public void run(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
      @Override
      public void run(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
      @Override
      public void run(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
      @Override
      public void run(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
      @Override
      public void run(SubscriberListener<T> listener) {
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
    final Subscriber<T> subscriber = this;
    try {
      subscriberListeners.signal(new SignalRunnable<SubscriberListener<T>>() {
        @Override
        public void run(SubscriberListener<T> listener) {
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
