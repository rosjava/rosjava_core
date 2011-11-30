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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.concurrent.CancellableLoop;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Default implementation of the {@link Subscriber}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultSubscriber<MessageType> extends DefaultTopic implements Subscriber<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultSubscriber.class);

  private final ExecutorService executorService;
  private final ImmutableMap<String, String> header;
  private final CopyOnWriteArrayList<MessageListener<MessageType>> messageListeners;
  private final IncomingMessageQueue<MessageType> incomingMessageQueue;
  private final MessageReader messageReader;
  private final Set<PublisherIdentifier> knownPublishers;
  private final SlaveIdentifier slaveIdentifier;
  private final TcpClientConnectionManager tcpClientConnectionManager;

  /**
   * All {@link SubscriberListener} instances added to the subscriber.
   */
  private final CopyOnWriteArrayList<SubscriberListener> subscriberListeners;

  private final class MessageReader extends CancellableLoop {
    @Override
    public void loop() throws InterruptedException {
      final MessageType message = incomingMessageQueue.take();
      if (DEBUG) {
        log.info("Received message: " + message + " " + message.getClass().getCanonicalName());
      }
      for (final MessageListener<MessageType> listener : messageListeners) {
        // TODO(damonkohler): Recycle Message objects to avoid GC.
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            listener.onNewMessage(message);
          }
        });
      }
    }
  }

  public static <S> DefaultSubscriber<S> create(SlaveIdentifier slaveIdentifier,
      TopicDefinition description, ExecutorService executorService,
      MessageDeserializer<S> deserializer) {
    return new DefaultSubscriber<S>(slaveIdentifier, description, deserializer, executorService);
  }

  private DefaultSubscriber(SlaveIdentifier slaveIdentifier, TopicDefinition topicDefinition,
      MessageDeserializer<MessageType> deserializer, ExecutorService executorService) {
    super(topicDefinition);
    this.executorService = executorService;
    this.messageListeners = new CopyOnWriteArrayList<MessageListener<MessageType>>();
    this.subscriberListeners = new CopyOnWriteArrayList<SubscriberListener>();
    this.incomingMessageQueue = new IncomingMessageQueue<MessageType>(deserializer);
    this.slaveIdentifier = slaveIdentifier;
    header =
        ImmutableMap.<String, String>builder()
            .putAll(slaveIdentifier.toHeader())
            .putAll(topicDefinition.toHeader())
            .build();
    knownPublishers = Sets.newHashSet();
    tcpClientConnectionManager = new TcpClientConnectionManager(executorService);
    messageReader = new MessageReader();
    executorService.execute(messageReader);
  }

  public Collection<String> getSupportedProtocols() {
    return ProtocolNames.SUPPORTED;
  }

  @Override
  public void addMessageListener(MessageListener<MessageType> listener) {
    messageListeners.add(listener);
  }

  @Override
  public void removeMessageListener(MessageListener<MessageType> listener) {
    messageListeners.remove(listener);
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
  public void shutdown() {
    messageReader.cancel();
    tcpClientConnectionManager.shutdown();
    signalShutdown();
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
    for (final SubscriberListener listener : subscriberListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterRegistrationSuccess(subscriber);
        }
      });
    }
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
    for (final SubscriberListener listener : subscriberListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onMasterRegistrationFailure(subscriber);
        }
      });
    }
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
    for (final SubscriberListener listener : subscriberListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onNewPublisher(subscriber);
        }
      });
    }
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
    for (final SubscriberListener listener : subscriberListeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          listener.onShutdown(subscriber);
        }
      });
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
