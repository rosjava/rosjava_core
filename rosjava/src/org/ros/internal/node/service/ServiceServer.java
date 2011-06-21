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
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.net.URI;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceServer {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final AdvertiseAddress advertiseAddress;
  private final ServiceResponseBuilder<?, ?> responseBuilder;
  private final ServiceDefinition definition;

  public ServiceServer(ServiceDefinition definition, ServiceResponseBuilder<?, ?> responseBuilder,
      AdvertiseAddress advertiseAddress) {
    this.definition = definition;
    this.responseBuilder = responseBuilder;
    this.advertiseAddress = advertiseAddress;
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

  public URI getUri() {
    return advertiseAddress.toUri("rosrpc");
  }

  public GraphName getName() {
    return definition.getName();
  }

  /**
   * @return a new {@link ServiceDefinition} with this {@link ServiceServer}'s
   *         {@link URI}
   */
  public ServiceDefinition getDefinition() {
    ServiceIdentifier identifier = new ServiceIdentifier(definition.getName(), getUri());
    return new ServiceDefinition(identifier, definition.getType(), definition.getMd5Checksum());
  }

  public ChannelHandler createRequestHandler() {
    return new ServiceRequestHandler(responseBuilder);
  }

}
