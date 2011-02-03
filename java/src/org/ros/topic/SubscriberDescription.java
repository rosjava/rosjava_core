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

import java.util.Map;

import org.ros.communication.MessageDescription;
import org.ros.transport.HeaderFields;

public class SubscriberDescription {

  private final String name;
  private final TopicDescription topicDescription;
  private final MessageDescription messageDescription;

  public static SubscriberDescription CreateFromHeader(Map<String, String> header) {
    String callerId = header.get(HeaderFields.CALLER_ID);
    return new SubscriberDescription(callerId, TopicDescription.CreateFromHeader(header),
        MessageDescription.CreateFromHeader(header));
  }

  public SubscriberDescription(String name, TopicDescription topicDescription,
      MessageDescription messageDescription) {
    this.name = name;
    this.topicDescription = topicDescription;
    this.messageDescription = messageDescription;
  }

  public String getName() {
    return name;
  }

  public TopicDescription getTopicDescription() {
    return topicDescription;
  }

  public MessageDescription getMessageDescription() {
    return messageDescription;
  }

}
