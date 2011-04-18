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
import com.google.common.collect.Lists;

import org.ros.internal.message.MessageDefinition;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicDefinition {

  private final GraphName name;
  private final MessageDefinition messageDefinition;

  public static TopicDefinition createFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.TOPIC));
    return new TopicDefinition(new GraphName(header.get(ConnectionHeaderFields.TOPIC)),
        MessageDefinition.createFromHeader(header));
  }

  public TopicDefinition(GraphName name, MessageDefinition messageDefinition) {
    this.name = name;
    this.messageDefinition = messageDefinition;
  }

  public GraphName getName() {
    return name;
  }

  public String getMessageType() {
    return messageDefinition.getType();
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.TOPIC, name.toString())
        .putAll(messageDefinition.toHeader())
        .build();
  }

  public List<String> toList() {
    return Lists.newArrayList(name.toString(), getMessageType());
  }

  @Override
  public String toString() {
    return "TopicDefinition<" + name + ", " + messageDefinition.toString() + ">";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((messageDefinition == null) ? 0 : messageDefinition.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TopicDefinition other = (TopicDefinition) obj;
    if (messageDefinition == null) {
      if (other.messageDefinition != null) return false;
    } else if (!messageDefinition.equals(other.messageDefinition)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }

}
