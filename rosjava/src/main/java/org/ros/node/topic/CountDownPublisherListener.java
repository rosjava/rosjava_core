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
 * instances for all signals.
 * 
 * @author khughes@google.com (Keith M. Hughes)
 */
public class CountDownPublisherListener<T> extends CountDownRegistrantListener<Publisher<T>>
    implements PublisherListener<T> {

  private CountDownLatch shutdownLatch;
  private CountDownLatch newSubscriberLatch;

  public static <T> CountDownPublisherListener<T> create() {
    return newFromCounts(1, 1, 1, 1, 1, 1);
  }

  /**
   * @param masterRegistrationSuccessCount
   *          the number of counts to wait for for a successful master
   *          registration
   * @param masterRegistrationFailureCount
   *          the number of counts to wait for for a failing master registration
   * @param shutdownCount
   *          the number of counts to wait for for a shutdown
   * @param newSubscriberCount
   *          the number of counts to wait for for a new subscriber
   */
  public static <T> CountDownPublisherListener<T> newFromCounts(int masterRegistrationSuccessCount,
      int masterRegistrationFailureCount, int masterUnregistrationSuccessCount,
      int masterUnregistrationFailureCount, int shutdownCount, int newSubscriberCount) {
    return new CountDownPublisherListener<T>(new CountDownLatch(masterRegistrationSuccessCount),
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
   * @param newSubscriberLatch
   *          the latch to use for a remote connection
   */
  private CountDownPublisherListener(CountDownLatch masterRegistrationSuccessLatch,
      CountDownLatch masterRegistrationFailureLatch,
      CountDownLatch masterUnregistrationSuccessLatch,
      CountDownLatch masterUnregistrationFailureLatch, CountDownLatch shutdownLatch,
      CountDownLatch newSubscriberLatch) {
    super(masterRegistrationSuccessLatch, masterRegistrationFailureLatch,
        masterUnregistrationSuccessLatch, masterUnregistrationFailureLatch);
    this.shutdownLatch = shutdownLatch;
    this.newSubscriberLatch = newSubscriberLatch;
  }

  @Override
  public void onNewSubscriber(Publisher<T> publisher) {
    newSubscriberLatch.countDown();
  }

  @Override
  public void onShutdown(Publisher<T> publisher) {
    shutdownLatch.countDown();
  }

  /**
   * Wait for the requested number of shutdowns.
   * 
   * @throws InterruptedException
   */
  public void awaitNewSubscriber() throws InterruptedException {
    newSubscriberLatch.await();
  }

  /**
   * Wait for the requested number of remote connections for the given time
   * period.
   * 
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the {@link TimeUnit} of the {@code timeout} argument
   * @return {@code true} if the remote connections happened within the time
   *         period {@code false} otherwise.
   * @throws InterruptedException
   */
  public boolean awaitNewSubscriber(long timeout, TimeUnit unit) throws InterruptedException {
    return newSubscriberLatch.await(timeout, unit);
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
   *          the {@link TimeUnit} of the {@code timeout} argument
   * @return {@code true} if the shutdowns happened within the time period,
   *         {@code false} otherwise
   * @throws InterruptedException
   */
  public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
    return shutdownLatch.await(timeout, unit);
  }
}
