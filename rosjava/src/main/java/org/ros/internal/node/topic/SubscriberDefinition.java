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
import com.google.common.collect.Maps;

import org.ros.internal.node.server.NodeSlaveIdentifier;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberDefinition {

  private final SubscriberIdentifier subscriberIdentifier;
  private final TopicDefinition topicDefinition;

  /**
   * Creates a subscriber definition from the headers in a connection header.
   * 
   * @param header The header data.
   * 
   * @return The subscriber definition from the header data.
   */
  public static SubscriberDefinition newFromHeader(Map<String, String> header) {
    NodeSlaveIdentifier slaveIdentifier =
        new NodeSlaveIdentifier(new GraphName(header.get(ConnectionHeaderFields.CALLER_ID)), null);
    TopicDefinition topicDefinition = TopicDefinition.newFromHeader(header);
    return new SubscriberDefinition(new SubscriberIdentifier(slaveIdentifier,
        topicDefinition.toIdentifier()), topicDefinition);
  }

  public SubscriberDefinition(SubscriberIdentifier subscriberIdentifier,
      TopicDefinition topicDefinition) {
    this.subscriberIdentifier = subscriberIdentifier;
    this.topicDefinition = topicDefinition;
  }

  public NodeSlaveIdentifier getSlaveIdentifier() {
    return subscriberIdentifier.getSlaveIdentifier();
  }

  public URI getSlaveUri() {
    return subscriberIdentifier.getUri();
  }

  public GraphName getTopicName() {
    return topicDefinition.getName();
  }

  public Map<String, String> toHeader() {
    // NOTE(damonkohler): ImmutableMap.Builder does not allow duplicate fields
    // while building.
    Map<String, String> header = Maps.newHashMap();
    header.putAll(subscriberIdentifier.toHeader());
    header.putAll(topicDefinition.toHeader());
    return ImmutableMap.copyOf(header);
  }

  @Override
  public String toString() {
    return "SubscriberDefinition<" + subscriberIdentifier + ", " + topicDefinition + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((subscriberIdentifier == null) ? 0 : subscriberIdentifier.hashCode());
    result = prime * result + ((topicDefinition == null) ? 0 : topicDefinition.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SubscriberDefinition other = (SubscriberDefinition) obj;
    if (subscriberIdentifier == null) {
      if (other.subscriberIdentifier != null) return false;
    } else if (!subscriberIdentifier.equals(other.subscriberIdentifier)) return false;
    if (topicDefinition == null) {
      if (other.topicDefinition != null) return false;
    } else if (!topicDefinition.equals(other.topicDefinition)) return false;
    return true;
  }
}
