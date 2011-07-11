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
import org.ros.internal.message.new_style.ServiceMessageDefinition;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceServer<RequestType, ResponseType> implements
    org.ros.node.service.ServiceServer<RequestType, ResponseType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final ServiceDefinition definition;
  private final MessageDeserializer<RequestType> deserializer;
  private final MessageSerializer<ResponseType> serializer;
  private final AdvertiseAddress advertiseAddress;
  private final ServiceResponseBuilder<RequestType, ResponseType> responseBuilder;
  private final CountDownLatch registrationLatch;

  public ServiceServer(ServiceDefinition definition, MessageDeserializer<RequestType> deserializer,
      MessageSerializer<ResponseType> serializer,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder,
      AdvertiseAddress advertiseAddress) {
    this.definition = definition;
    this.deserializer = deserializer;
    this.serializer = serializer;
    this.responseBuilder = responseBuilder;
    this.advertiseAddress = advertiseAddress;
    registrationLatch = new CountDownLatch(1);
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
   *         {@link ServiceServer}'s {@link URI}
   */
  public ServiceDefinition getDefinition() {
    ServiceIdentifier identifier = new ServiceIdentifier(definition.getName(), getUri());
    return new ServiceDefinition(identifier, new ServiceMessageDefinition(definition.getType(),
        definition.getMd5Checksum()));
  }

  public ChannelHandler createRequestHandler() {
    return new ServiceRequestHandler<RequestType, ResponseType>(deserializer, serializer,
        responseBuilder);
  }

  public void signalRegistrationDone() {
    registrationLatch.countDown();
  }

  @Override
  public boolean isRegistered() {
    return registrationLatch.getCount() == 0;
  }

  @Override
  public void awaitRegistration() throws InterruptedException {
    registrationLatch.await();
  }

  @Override
  public boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException {
    return registrationLatch.await(timeout, unit);
  }

}
