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

import com.google.common.collect.ImmutableMap;

import org.ros.node.server.SlaveIdentifier;
import org.ros.transport.ConnectionHeaderFields;

import java.net.URL;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberIdentifier {

  private final SlaveIdentifier slaveIdentifier;
  private final TopicDefinition topicDefinition;

  public static SubscriberIdentifier createFromHeader(Map<String, String> header) {
    // TODO(damonkohler): Update SlaveIdentifier to handle the case where the
    // URL is not set.
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier(header.get(ConnectionHeaderFields.CALLER_ID),
        null);
    return new SubscriberIdentifier(slaveIdentifier, TopicDefinition.createFromHeader(header));
  }

  public SubscriberIdentifier(SlaveIdentifier slaveIdentifier, TopicDefinition topicDefinition) {
    this.slaveIdentifier = slaveIdentifier;
    this.topicDefinition = topicDefinition;
  }

  public SlaveIdentifier getSlaveIdentifier() {
    return slaveIdentifier;
  }

  public String getNodeName() {
    return slaveIdentifier.getName();
  }

  public URL getSlaveUrl() {
    return slaveIdentifier.getUrl();
  }

  public String getTopicName() {
    return topicDefinition.getName();
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>()
        .putAll(slaveIdentifier.toHeader())
        .putAll(topicDefinition.toHeader())
        .build();
  }

}
