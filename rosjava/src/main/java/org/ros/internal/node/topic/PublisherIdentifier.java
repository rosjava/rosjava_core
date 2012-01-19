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
import com.google.common.collect.Sets;

import org.ros.internal.node.server.NodeSlaveIdentifier;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * All information needed to identify a publisher.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherIdentifier {

  private final NodeSlaveIdentifier nodeSlaveIdentifier;
  private final TopicIdentifier topicIdentifier;

  public static Collection<PublisherIdentifier> newCollectionFromUris(
      Collection<URI> publisherUris, TopicDefinition topicDefinition) {
    Set<PublisherIdentifier> publishers = Sets.newHashSet();
    for (URI uri : publisherUris) {
      NodeSlaveIdentifier slaveIdentifier = new NodeSlaveIdentifier(null, uri);
      publishers.add(new PublisherIdentifier(slaveIdentifier, topicDefinition.toIdentifier()));
    }
    return publishers;
  }

  public static PublisherIdentifier newFromStrings(String nodeName, String uri, String topicName) {
    return new PublisherIdentifier(NodeSlaveIdentifier.newFromStrings(nodeName, uri),
        TopicIdentifier.newFromString(topicName));
  }

  public PublisherIdentifier(NodeSlaveIdentifier slaveIdentifier, TopicIdentifier topicIdentifier) {
    Preconditions.checkNotNull(slaveIdentifier);
    Preconditions.checkNotNull(topicIdentifier);
    this.nodeSlaveIdentifier = slaveIdentifier;
    this.topicIdentifier = topicIdentifier;
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>().putAll(nodeSlaveIdentifier.toHeader())
        .putAll(topicIdentifier.toHeader()).build();
  }

  public NodeSlaveIdentifier getNodeSlaveIdentifier() {
    return nodeSlaveIdentifier;
  }

  /**
   * Get the name of the node where the slave for this publisher lives.
   * 
   * @return
   */
  public GraphName getNodeSlaveName() {
    return nodeSlaveIdentifier.getNodeName();
  }

  /**
   * Get the URL for the slave server on the node which contains this publisher.
   * 
   * @return
   */
  public URI getNodeSlaveUri() {
    return nodeSlaveIdentifier.getUri();
  }

  /**
   * Get the {@link TopicIdentifier} for the publisher's topic.
   * 
   * @return
   */
  public TopicIdentifier getTopicIdentifier() {
    return topicIdentifier;
  }

  /**
   * Get the name of the topic for the publisher.
   * 
   * @return
   */
  public GraphName getTopicName() {
    return topicIdentifier.getName();
  }

  @Override
  public String toString() {
    return "PublisherIdentifier<" + nodeSlaveIdentifier + ", " + topicIdentifier + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + nodeSlaveIdentifier.hashCode();
    result = prime * result + topicIdentifier.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PublisherIdentifier other = (PublisherIdentifier) obj;
    if (!nodeSlaveIdentifier.equals(other.nodeSlaveIdentifier))
      return false;
    if (!topicIdentifier.equals(other.topicIdentifier))
      return false;
    return true;
  }
}
