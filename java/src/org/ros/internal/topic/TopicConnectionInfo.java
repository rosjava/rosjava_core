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

package org.ros.internal.topic;

import org.ros.internal.transport.Connection;

/**
 * Data structure for storing identifiers for publisher and subscriber of a
 * connection.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TopicConnectionInfo {
  
  private final PublisherIdentifier publisherIdentifier;
  private final SubscriberIdentifier subscriberIdentifier;
  private Connection connection;

  public TopicConnectionInfo(PublisherIdentifier publisherIdentifier,
      SubscriberIdentifier subscriberIdentifier, Connection connection) {
    this.publisherIdentifier = publisherIdentifier;
    this.subscriberIdentifier = subscriberIdentifier;
    this.connection = connection;
  }

  /**
   * @return Identifier for publisher-side of topic connection.
   */
  public PublisherIdentifier getPublisherIdentifier() {
    return publisherIdentifier;
  }

  /**
   * @return Identifier for subscriber-side of topic connection.
   */
  public SubscriberIdentifier getSubscriberIdentifier() {
    return subscriberIdentifier;
  }
  
  public Connection getConnection() { 
    return connection;
  }
}
