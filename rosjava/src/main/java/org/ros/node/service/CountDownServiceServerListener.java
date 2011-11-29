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

package org.ros.node.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ServiceServerListener} which uses {@link CountDownLatch} to track
 * message invocations.
 * 
 * @author Keith M. Hughes
 */
public class CountDownServiceServerListener implements ServiceServerListener {
  private CountDownLatch registrationLatch;
  private CountDownLatch shutdownLatch;

  /**
   * Listener with counts of 1.
   */
  public CountDownServiceServerListener() {
    this(1, 1);
  }

  /**
   * @param registerationCount
   *          the number of counts to wait for for a registration
   * @param shutdownCount
   *          the number of counts to wait for for a shutdown
   */
  public CountDownServiceServerListener(int registerationCount, int shutdownCount) {
    registrationLatch = new CountDownLatch(registerationCount);
    shutdownLatch = new CountDownLatch(shutdownCount);
  }

  @Override
  public void onServiceServerRegistration(ServiceServer<?, ?> server) {
    registrationLatch.countDown();
  }

  @Override
  public void onServiceServerShutdown(ServiceServer<?, ?> server) {
    shutdownLatch.countDown();
  }
  /**
   * Await for the requested number of registrations.
   * 
   * @throws InterruptedException
   */
  public void awaitRegistration() throws InterruptedException {
    registrationLatch.await();
  }

  /**
   * Await for the requested number of registrations for the given time period.
   * 
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the {@code timeout} argument
   * 
   * @return {@code true} if the registration happened within the time period
   *         {@code false} otherwise.
   * 
   * @throws InterruptedException
   */
  public boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException {
    return registrationLatch.await(timeout, unit);
  }

  /**
   * Await for the requested number of shutdowns.
   * 
   * @throws InterruptedException
   */
  public void awaitShutdown() throws InterruptedException {
    shutdownLatch.await();
  }

  /**
   * Await for the requested number of shutdowns for the given time period.
   * 
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the {@code timeout} argument
   * 
   * @return {@code true} if the shutdowns happened within the time period
   *         {@code false} otherwise.
   * 
   * @throws InterruptedException
   */
  public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
    return shutdownLatch.await(timeout, unit);
  }

}
