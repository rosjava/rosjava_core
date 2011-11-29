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

/**
 * Listen to creation of new publisher and subscriber instances.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface TopicListener {

  /**
   * A publisher has been added.
   * 
   * @param publisher
   *          The publisher which has been added.
   */
  void publisherAdded(DefaultPublisher<?> publisher);

  /**
   * A subscriber has been added.
   * 
   * @param subscriber
   *          The subscriber which has been added.
   */
  void subscriberAdded(DefaultSubscriber<?> subscriber);

}
