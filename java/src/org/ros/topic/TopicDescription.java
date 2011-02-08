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

package org.ros.topic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.ros.message.MessageDescription;
import org.ros.transport.ConnectionHeaderFields;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicDescription {

  private final String name;
  private final MessageDescription messageDescription;

  public static TopicDescription createFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.TOPIC));
    return new TopicDescription(header.get(ConnectionHeaderFields.TOPIC),
        MessageDescription.createFromHeader(header));
  }

  public TopicDescription(String name, MessageDescription messageDescription) {
    this.name = name;
    this.messageDescription = messageDescription;
  }

  public String getName() {
    return name;
  }

  public String getMessageType() {
    return messageDescription.getType();
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.TOPIC, name)
        .putAll(messageDescription.toHeader())
        .build();
  }

  public List<String> toList() {
    return Lists.newArrayList(name, getMessageType());
  }

  @Override
  public String toString() {
    return "TopicDescription<" + name + ", " + messageDescription.toString() + ">";
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
    result = prime * result + ((messageDescription == null) ? 0 : messageDescription.hashCode());
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
    TopicDescription other = (TopicDescription) obj;
    if (messageDescription == null) {
      if (other.messageDescription != null) return false;
    } else if (!messageDescription.equals(other.messageDescription)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }

}
