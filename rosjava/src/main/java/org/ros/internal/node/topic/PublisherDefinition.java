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

import java.net.URI;

import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.namespace.GraphName;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherDefinition {

  private final PublisherIdentifier publisherIdentifier;
  private final TopicDefinition topicDefinition;

  public static PublisherDefinition newFromSlaveIdentifier(SlaveIdentifier slaveIdentifier,
      TopicDefinition topicDefinition) {
    return new PublisherDefinition(new PublisherIdentifier(slaveIdentifier,
        topicDefinition.toIdentifier()), topicDefinition);
  }

  public PublisherDefinition(PublisherIdentifier publisherIdentifier,
      TopicDefinition topicDefinition) {
    Preconditions.checkNotNull(publisherIdentifier);
    Preconditions.checkNotNull(topicDefinition);
    Preconditions.checkArgument(publisherIdentifier.getTopicIdentifier().equals(
        topicDefinition.toIdentifier()));
    this.publisherIdentifier = publisherIdentifier;
    this.topicDefinition = topicDefinition;
  }

  public SlaveIdentifier getSlaveIdentifier() {
    return publisherIdentifier.getSlaveIdentifier();
  }

  public GraphName getSlaveName() {
    return publisherIdentifier.getSlaveIdentifier().getName();
  }

  public URI getSlaveUri() {
    return publisherIdentifier.getSlaveUri();
  }

  public GraphName getTopicName() {
    return topicDefinition.getName();
  }

  public String getTopicMessageType() {
    return topicDefinition.getMessageType();
  }

  @Override
  public String toString() {
    return "PublisherDefinition<" + publisherIdentifier + ", " + topicDefinition + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((publisherIdentifier == null) ? 0 : publisherIdentifier.hashCode());
    result = prime * result + ((topicDefinition == null) ? 0 : topicDefinition.hashCode());
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
    PublisherDefinition other = (PublisherDefinition) obj;
    if (publisherIdentifier == null) {
      if (other.publisherIdentifier != null)
        return false;
    } else if (!publisherIdentifier.equals(other.publisherIdentifier))
      return false;
    if (topicDefinition == null) {
      if (other.topicDefinition != null)
        return false;
    } else if (!topicDefinition.equals(other.topicDefinition))
      return false;
    return true;
  }
}
