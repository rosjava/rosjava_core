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

import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * Listen to creation of new publisher and subscriber instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface TopicListener {

  /**
   * Called when a new {@link Publisher} has been added.
   * 
   * @param publisher
   *          the {@link Publisher} which has been added
   */
  void publisherAdded(DefaultPublisher<?> publisher);

  void publisherRemoved(DefaultPublisher<?> publisher);

  /**
   * Called when a {@link Subscriber} has been added.
   * 
   * @param subscriber
   *          the {@link Subscriber} which has been added
   */
  void subscriberAdded(DefaultSubscriber<?> subscriber);

  void subscriberRemoved(DefaultSubscriber<?> subscriber);
}
