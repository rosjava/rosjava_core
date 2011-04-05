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

import org.ros.internal.namespace.GraphName;
import org.ros.message.Message;

import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Topic {

  private final TopicDefinition description;
  private final Class<?> messageClass;

  public Topic(TopicDefinition description, Class<?> messageClass) {
    this.description = description;
    this.messageClass = messageClass;
  }
  
  public TopicDefinition getTopicDefinition() {
    return description;
  }
 
  public List<String> getTopicDefinitionAsList() {
    return description.toList();
  }
  
  public GraphName getTopicName() {
    return description.getName();
  }
  
  public String getTopicMessageType() {
    return description.getMessageType();
  }
  
  public Map<String, String> getTopicDefinitionHeader() {
    return description.toHeader();
  }
  
  /**
   * @param messageClass
   * @return <code>true</code> if this {@link Subscriber} instance accepts the
   *         supplied {@link Message} class
   */
  public boolean checkMessageClass(Class<?> messageClass) {
    return this.messageClass == messageClass;
  }

}