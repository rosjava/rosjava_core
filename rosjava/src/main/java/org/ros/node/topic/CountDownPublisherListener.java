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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link PublisherListener} which uses separate {@link CountDownLatch}
 * instances for all messages.
 * 
 * @author Keith M. Hughes
 */
public class CountDownPublisherListener implements PublisherListener {
  private CountDownLatch registrationLatch;
  private CountDownLatch shutdownLatch;
  private CountDownLatch remoteConnectionLatch;

  /**
   * Listener with counts of 1.
   */
  public CountDownPublisherListener() {
    this(1, 1, 1);
  }

  /**
   * @param registerationCount
   *          the number of counts to wait for for a registration
   * @param shutdownCount
   *          the number of counts to wait for for a shutdown
   * @param remoteConnectionCount
   *          the number of counts to wait for for a remore connection
   */
  public CountDownPublisherListener(int registerationCount, int shutdownCount,
      int remoteConnectionCount) {
    this(new CountDownLatch(registerationCount), new CountDownLatch(shutdownCount),
        new CountDownLatch(remoteConnectionCount));
  }

  /**
   * @param registerationLatch
   *          the latch to use for a registration
   * @param shutdownLatch
   *          the latch to use for a shutdown
   * @param remoteConnectionLatch
   *          the latch to use for a remote connection
   */
  public CountDownPublisherListener(CountDownLatch registerationLatch,
      CountDownLatch shutdownLatch, CountDownLatch remoteConnectionLatch) {
    this.registrationLatch = registerationLatch;
    this.shutdownLatch = shutdownLatch;
    this.remoteConnectionLatch = remoteConnectionLatch;
  }

  @Override
  public void onPublisherMasterRegistration(Publisher<?> publisher) {
    registrationLatch.countDown();
  }

  @Override
  public void onPublisherRemoteConnection(Publisher<?> publisher) {
    remoteConnectionLatch.countDown();
  }

  @Override
  public void onPublisherShutdown(Publisher<?> publisher) {
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
  public void awaitRemoteConnection() throws InterruptedException {
    remoteConnectionLatch.await();
  }

  /**
   * Await for the requested number of remote connections for the given time
   * period.
   * 
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the {@code timeout} argument
   * 
   * @return {@code true} if the remote connections happened within the time
   *         period {@code false} otherwise.
   * 
   * @throws InterruptedException
   */
  public boolean awaitRemoteConnection(long timeout, TimeUnit unit) throws InterruptedException {
    return remoteConnectionLatch.await(timeout, unit);
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
