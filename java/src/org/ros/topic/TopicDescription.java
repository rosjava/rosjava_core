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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class TopicDescription {

  private final String name;
  private final MessageDescription messageDescription;

  public static TopicDescription CreateFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(HeaderFields.TOPIC));
    return new TopicDescription(header.get(HeaderFields.TOPIC), MessageDescription
        .CreateFromHeader(header));
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
  
  public String getMd5Checksum() {
    return messageDescription.getMd5Checksum();
  }
  
  public Map<String, String> toHeader() {
    Map<String, String> header = Maps.newHashMap();
    header.put(HeaderFields.TOPIC, name);
    header.putAll(messageDescription.toHeader());
    return header;
  }

}
