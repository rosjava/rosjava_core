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

package org.ros.internal.node.service;

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.ros.address.AdvertiseAddress;
import org.ros.concurrent.ListenerCollection;
import org.ros.concurrent.ListenerCollection.SignalRunnable;
import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.service.ServiceServer;
import org.ros.node.service.ServiceServerListener;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Default implementation of a {@link ServiceServer}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultServiceServer<T, S> implements ServiceServer<T, S> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultPublisher.class);

  private final ServiceDefinition definition;
  private final MessageDeserializer<T> deserializer;
  private final MessageSerializer<S> serializer;
  private final AdvertiseAddress advertiseAddress;
  private final ServiceResponseBuilder<T, S> responseBuilder;
  private final ListenerCollection<ServiceServerListener<T, S>> listeners;

  public DefaultServiceServer(ServiceDefinition definition, MessageDeserializer<T> deserializer,
      MessageSerializer<S> serializer, ServiceResponseBuilder<T, S> responseBuilder,
      AdvertiseAddress advertiseAddress, ExecutorService executorService) {
    this.definition = definition;
    this.deserializer = deserializer;
    this.serializer = serializer;
    this.responseBuilder = responseBuilder;
    this.advertiseAddress = advertiseAddress;
    listeners = new ListenerCollection<ServiceServerListener<T, S>>(executorService);
  }

  public ChannelBuffer finishHandshake(Map<String, String> incomingHeader) {
    Map<String, String> header = getDefinition().toHeader();
    if (DEBUG) {
      log.info("Outgoing handshake header: " + header);
    }
    if (incomingHeader.containsKey(ConnectionHeaderFields.PROBE)) {
      // TODO(damonkohler): This is kind of a lousy way to pass back the
      // information that this is a probe.
      return null;
    } else {
      Preconditions.checkState(incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
          header.get(ConnectionHeaderFields.MD5_CHECKSUM)));
      return ConnectionHeader.encode(header);
    }
  }

  @Override
  public URI getUri() {
    return advertiseAddress.toUri("rosrpc");
  }

  @Override
  public GraphName getName() {
    return definition.getName();
  }

  /**
   * @return a new {@link ServiceMessageDefinition} with this
   *         {@link DefaultServiceServer}'s {@link URI}
   */
  ServiceDefinition getDefinition() {
    ServiceIdentifier identifier = new ServiceIdentifier(definition.getName(), getUri());
    return new ServiceDefinition(identifier, new ServiceMessageDefinition(definition.getType(),
        definition.getMd5Checksum()));
  }

  public ChannelHandler createRequestHandler() {
    return new ServiceRequestHandler<T, S>(deserializer, serializer, responseBuilder);
  }

  /**
   * Signal all registered {@link ServiceServerListener} instances that
   * registration is complete.
   */
  public void signalRegistrationDone() {
    final ServiceServer<T, S> serviceServer = this;
    listeners.signal(new SignalRunnable<ServiceServerListener<T, S>>() {
      @Override
      public void run(ServiceServerListener<T, S> listener) {
        listener.onServiceServerRegistration(serviceServer);
      }
    });
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addListener(ServiceServerListener<T, S> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(ServiceServerListener<T, S> listener) {
    listeners.remove(listener);
  }
}
