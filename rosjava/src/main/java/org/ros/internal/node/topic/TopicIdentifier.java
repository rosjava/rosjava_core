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

import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.namespace.GraphName;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicIdentifier {
  
  private final GraphName name;
  
  public static TopicIdentifier newFromString(String topicName) {
    return new TopicIdentifier(new GraphName(topicName));
  }

  public TopicIdentifier(GraphName name) {
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(name.isGlobal());
    this.name = name;
  }
  
  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.TOPIC, name.toString())
        .build();
  }

  public GraphName getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return "TopicIdentifier<" + name + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TopicIdentifier other = (TopicIdentifier) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
