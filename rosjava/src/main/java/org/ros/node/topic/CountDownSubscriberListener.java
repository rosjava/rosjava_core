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
 * A {@link SubscriberListener} which uses separate {@link CountDownLatch}
 * instances for all messages.
 * 
 * @author khughes@google.com (Keith M. Hughes)
 */
public class CountDownSubscriberListener<T> extends CountDownRegistrantListener<Subscriber<T>>
    implements SubscriberListener<T> {

  private CountDownLatch shutdownLatch;
  private CountDownLatch newPublisherLatch;

  /**
   * Construct a {@link CountDownSubscriberListener} with all counts set to 1.
   */
  public static <T> CountDownSubscriberListener<T> create() {
    return newFromCounts(1, 1, 1, 1, 1, 1);
  }

  /**
   * @param masterRegistrationSuccessCount
   *          the number of counts to wait for for a successful master
   *          registration
   * @param masterRegistrationFailureCount
   *          the number of counts to wait for for a failed master registration
   * @param shutdownCount
   *          the number of counts to wait for for a shutdown
   * @param newPublisherCount
   *          the number of counts to wait for for a new publisher
   */
  public static <T> CountDownSubscriberListener<T> newFromCounts(
      int masterRegistrationSuccessCount, int masterRegistrationFailureCount,
      int masterUnregistrationSuccessCount, int masterUnregistrationFailureCount,
      int shutdownCount, int newSubscriberCount) {
    return new CountDownSubscriberListener<T>(new CountDownLatch(masterRegistrationSuccessCount),
        new CountDownLatch(masterRegistrationFailureCount), new CountDownLatch(
            masterUnregistrationSuccessCount),
        new CountDownLatch(masterUnregistrationFailureCount), new CountDownLatch(shutdownCount),
        new CountDownLatch(newSubscriberCount));
  }

  /**
   * @param masterRegistrationSuccessLatch
   *          the latch to use for a registration
   * @param shutdownLatch
   *          the latch to use for a shutdown
   * @param newPublisherLatch
   *          the latch to use for a remote connection
   */
  private CountDownSubscriberListener(CountDownLatch masterRegistrationSuccessLatch,
      CountDownLatch masterRegistrationFailureLatch,
      CountDownLatch masterUnregistrationSuccessLatch,
      CountDownLatch masterUnregistrationFailureLatch, CountDownLatch shutdownLatch,
      CountDownLatch newPublisherLatch) {
    super(masterRegistrationSuccessLatch, masterRegistrationFailureLatch,
        masterUnregistrationSuccessLatch, masterUnregistrationFailureLatch);
    this.shutdownLatch = shutdownLatch;
    this.newPublisherLatch = newPublisherLatch;
  }

  @Override
  public void onNewPublisher(Subscriber<T> subscriber) {
    newPublisherLatch.countDown();
  }

  @Override
  public void onShutdown(Subscriber<T> subscriber) {
    shutdownLatch.countDown();
  }

  /**
   * Await for the requested number of remote connections.
   * 
   * @throws InterruptedException
   */
  public void awaitNewPublisher() throws InterruptedException {
    newPublisherLatch.await();
  }

  /**
   * Wait for the requested number of remote connections for the given time
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
  public boolean awaitNewPublisher(long timeout, TimeUnit unit) throws InterruptedException {
    return newPublisherLatch.await(timeout, unit);
  }

  /**
   * Wait for the requested number of shutdowns.
   * 
   * @throws InterruptedException
   */
  public void awaitShutdown() throws InterruptedException {
    shutdownLatch.await();
  }

  /**
   * Wait for the requested number of shutdowns for the given time period.
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
