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

package org.ros.internal.node.client;

import java.util.concurrent.TimeUnit;

import org.ros.internal.node.server.MasterServer;

/**
 * Represents something that can be registered with the {@link MasterServer} by
 * the {@link Registrar}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface Registrant {

  /**
   * @return {@code true} if the {@link Registrant} is registered with the ROS
   *         master, {@code false} otherwise
   */
  boolean isRegistered();

  /**
   * Wait for the {@link Registrant} to be registered with the ROS master.
   * 
   * <p>
   * This call blocks.
   * 
   * @throws InterruptedException
   */
  void awaitRegistration() throws InterruptedException;

  /**
   * Wait for the {@link Registrant} to be registered with the ROS master.
   * 
   * @param timeout
   *          how long to wait for registration
   * @param unit
   *          the units for how long to wait
   * @return {@code true} if the {@link Registrant} successfully registered with
   *         the ROS master, {@code false} otherwise
   * @throws InterruptedException
   */
  boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException;

}
