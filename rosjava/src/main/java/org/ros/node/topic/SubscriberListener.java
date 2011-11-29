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
 * A lifecycle listener for {@link Subscriber} instances.
 * 
 * @author Keith M. Hughes
 */
public interface SubscriberListener {

  /**
   * The {@link Subscriber} has successfully registered with the master.
   * 
   * @param subscriber
   *          the {@link Subscriber} which has been registered.
   */
  void onMasterRegistrationSuccess(Subscriber<?> subscriber);

  /**
   * The {@link Subscriber} has failed to register with the master.
   * 
   * @param subscriber
   *          the {@link Subscriber} which has failed to register
   */
  void onMasterRegistrationFailure(Subscriber<?> subscriber);

  /**
   * A new {@link Publisher} has connected to the {@link Subscriber}.
   * 
   * @param subscriber
   *          the {@link Subscriber} that the {@link Publisher} connected to
   */
  void onNewPublisher(Subscriber<?> subscriber);

  /**
   * The {@link Subscriber} has been shut down.
   * 
   * @param subscriber
   *          the {@link Subscriber} that was shut down
   */
  void onShutdown(Subscriber<?> subscriber);
}
