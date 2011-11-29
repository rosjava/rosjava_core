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

package org.ros.node.topic;

/**
 * A lifecycle listener for {@link Publisher} instances.
 *
 * @author Keith M. Hughes
 */
public interface PublisherListener {
	
  /**
   * The publisher has been registered with the master.
   * 
   * @param publisher
   *          the publisher which has been registered
   */
  void onPublisherMasterRegistration(Publisher<?> publisher);
  
  /**
   * A remote connection has been made to the publisher.
   * 
   * @param publisher
   *          the publisher which has received the new connection
   */
  void onPublisherRemoteConnection(Publisher<?> publisher);
	
  /**
   * The publisher has been shut down.
   * 
   * @param publisher
   *          the publisher which has been shut down
   */
  void onPublisherShutdown(Publisher<?> publisher);

}
