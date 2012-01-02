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

import org.ros.namespace.GraphName;

import java.util.List;
import java.util.Map;

/**
 * Base definition of a {@link Topic}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public abstract class DefaultTopic implements Topic {

  private final TopicDefinition topicDefinition;

  public DefaultTopic(TopicDefinition topicDefinition) {
    this.topicDefinition = topicDefinition;
  }

  /**
   * @return the definition of the topic.
   */
  public TopicDefinition getTopicDefinition() {
    return topicDefinition;
  }

  public List<String> getTopicDefinitionAsList() {
    return topicDefinition.toList();
  }

  @Override
  public GraphName getTopicName() {
    return topicDefinition.getName();
  }

  @Override
  public String getTopicMessageType() {
    return topicDefinition.getMessageType();
  }

  /**
   * Get the definition header for the topic.
   * 
   * @return
   */
  public Map<String, String> getTopicDefinitionHeader() {
    return topicDefinition.toHeader();
  }

  /**
   * Signal that the {@link Topic} successfully registered with the master.
   */
  public abstract void signalOnMasterRegistrationSuccess();

  /**
   * Signal that the {@link Topic} failed to register with the master.
   */
  public abstract void signalOnMasterRegistrationFailure();

  /**
   * Signal that the {@link Topic} successfully unregistered with the master.
   */
  public abstract void signalOnMasterUnregistrationSuccess();

  /**
   * Signal that the {@link Topic} failed to unregister with the master.
   */
  public abstract void signalOnMasterUnregistrationFailure();
}
