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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.ros.MessageDeserializer;
import org.ros.MessageListener;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.IncomingMessageQueue;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpClientPipelineFactory;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Subscriber<MessageType> extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Subscriber.class);

  private final Executor executor;
  private final ImmutableMap<String, String> header;
  private final CopyOnWriteArrayList<MessageListener<MessageType>> listeners;
  private final IncomingMessageQueue<MessageType> in;
  private final MessageReadingThread thread;
  private final ChannelFactory channelFactory;
  private final Set<PublisherDefinition> knownPublishers;
  private final SlaveIdentifier slaveIdentifier;
  private final ChannelGroup channelGroup;
  private final Collection<ClientBootstrap> bootstraps;
  
  private final class MessageReadingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          MessageType message = in.take();
          if (DEBUG) {
            log.info("Received message: " + message + " " + message.getClass().getCanonicalName());
          }
          for (MessageListener<MessageType> listener : listeners) {
            if (Thread.currentThread().isInterrupted()) {
              break;
            }
            // TODO(damonkohler): Recycle Message objects to avoid GC.
            listener.onNewMessage(message);
          }
        }
      } catch (InterruptedException e) {
        // Cancelable
        if (DEBUG) {
          log.info("Canceled.");
        }
      }
    }

    public void cancel() {
      interrupt();
    }
  }

  public static <S> Subscriber<S> create(SlaveIdentifier slaveIdentifier,
      TopicDefinition description, Class<S> messageClass, Executor executor,
      MessageDeserializer<S> deserializer) {
    return new Subscriber<S>(slaveIdentifier, description, deserializer, executor);
  }

  private Subscriber(SlaveIdentifier slaveIdentifier, TopicDefinition topicDefinition,
      MessageDeserializer<MessageType> deserializer, Executor executor) {
    super(topicDefinition);
    this.executor = executor;
    this.listeners = new CopyOnWriteArrayList<MessageListener<MessageType>>();
    this.in = new IncomingMessageQueue<MessageType>(deserializer);
    this.slaveIdentifier = slaveIdentifier;
    header =
        ImmutableMap.<String, String>builder().putAll(slaveIdentifier.toHeader())
            .putAll(topicDefinition.toHeader()).build();
    knownPublishers = Sets.newHashSet();
    channelFactory =
        new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    channelGroup = new DefaultChannelGroup();
    bootstraps = Lists.newArrayList();
    thread = new MessageReadingThread();
    thread.start();
  }

  public Collection<String> getSupportedProtocols() {
    return ProtocolNames.SUPPORTED;
  }

  public void addMessageListener(MessageListener<MessageType> listener) {
    listeners.add(listener);
  }

  public void removeMessageListener(MessageListener<MessageType> listener) {
    listeners.remove(listener);
    // TODO(kwc): Contracts on who does setup/teardown of resources is really
    // unclear right now. Also, we need to do much more cleanup than this, such
    // as unregistering with the master. Similarly, there needs to be logic in
    // addMessageCallbackListener to start the thread back up. Also, should we
    // be using listeners as a proxy for the number of Subscriber handles, or
    // should we track those explicitly?
    if (listeners.size() == 0) {
      thread.interrupt();
    }
  }

  public synchronized void addPublisher(PublisherDefinition publisherDefinition,
      InetSocketAddress address) {
    TcpClientPipelineFactory factory = new TcpClientPipelineFactory(channelGroup) {
      @Override
      public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = super.getPipeline();
        pipeline.addLast("SubscriberHandshakeHandler", new SubscriberHandshakeHandler<MessageType>(
            header, in));
        return pipeline;
      }
    };
    ClientBootstrap bootstrap = createClientBootstrap(factory);
    // TODO(damonkohler): Add timeouts.
    ChannelFuture future = bootstrap.connect(address).awaitUninterruptibly();
    if (!future.isSuccess()) {
      throw new RuntimeException(future.getCause());
    }
    Channel channel = future.getChannel();
    future = channel.write(ConnectionHeader.encode(header)).awaitUninterruptibly();
    if (!future.isSuccess()) {
      throw new RuntimeException(future.getCause());
    }
    if (DEBUG) {
      log.info("Connected to: " + channel.getRemoteAddress());
    }
    knownPublishers.add(publisherDefinition);
  }

  private ClientBootstrap createClientBootstrap(TcpClientPipelineFactory factory) {
    ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(factory);
    bootstrap.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
    bootstraps.add(bootstrap);
    return bootstrap;
  }

  /**
   * Updates the list of {@link Publisher}s for the topic that this
   * {@link Subscriber} is interested in.
   * 
   * @param publishers {@link List} of {@link Publisher}s for the subscribed
   *        topic
   */
  public synchronized void updatePublishers(Collection<PublisherDefinition> publishers) {
    // Find new connections.
    ArrayList<PublisherDefinition> newPublishers = new ArrayList<PublisherDefinition>();
    for (PublisherDefinition publisher : publishers) {
      if (!knownPublishers.contains(publisher)) {
        newPublishers.add(publisher);
      }
    }
    for (final PublisherDefinition publisher : newPublishers) {
      executor.execute(new UpdatePublisherRunnable<MessageType>(this, this.slaveIdentifier,
          publisher));
    }
  }

  public void shutdown() {
    thread.cancel();
    channelGroup.close().awaitUninterruptibly();
    channelFactory.releaseExternalResources();
    for (ClientBootstrap bootstrap : bootstraps) {
      bootstrap.releaseExternalResources();
    }
  }

  /**
   * @return this {@link Subscriber}'s connection header as an
   *         {@link ImmutableMap}
   */
  public ImmutableMap<String, String> getHeader() {
    return header;
  }

}
