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
   * The {@link Publisher} has been registered with the master.
   * 
   * @param publisher
   *          the {@link Publisher} which has been registered
   */
  void onMasterRegistrationSuccess(Publisher<?> publisher);

  /**
   * The {@link Publisher} has failed to register with the master.
   * 
   * <p>
   * This may be called multiple times per {@link Publisher} since master
   * registration will be retried until success.
   * 
   * @param publisher
   *          the {@link Publisher} which has been registered
   */
  void onMasterRegistrationFailure(Publisher<?> publisher);

  /**
   * A {@link Subscriber} has connected to the {@link Publisher}.
   * 
   * @param publisher
   *          the {@link Publisher} which has received the new connection
   */
  void onNewSubscriber(Publisher<?> publisher);

  /**
   * The {@link Publisher} has been shut down.
   * 
   * @param publisher
   *          the {@link Publisher} which has been shut down
   */
  void onShutdown(Publisher<?> publisher);
}
