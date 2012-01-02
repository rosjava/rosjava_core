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

import com.google.common.collect.Maps;

import org.ros.concurrent.CancellableLoop;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RetryingCompletionService {

  private static final long DEFAULT_RETRY_DELAY = 5;
  private static final TimeUnit DEFAULT_RETRY_TIME_UNIT = TimeUnit.SECONDS;

  private final Map<Future<Boolean>, Callable<Boolean>> callables;
  private final CompletionService<Boolean> completionService;
  private final RetryLoop retryLoop;
  private final ScheduledExecutorService retryExecutor;

  private long retryDelay;
  private TimeUnit retryTimeUnit;

  private class RetryLoop extends CancellableLoop {
    @Override
    public void loop() throws InterruptedException {
      Future<Boolean> future = completionService.take();
      final Callable<Boolean> callable = callables.remove(future);
      boolean retry;
      try {
        retry = future.get();
      } catch (ExecutionException e) {
        retry = true;
      }
      if (retry) {
        retryExecutor.schedule(new Runnable() {
          @Override
          public void run() {
            submit(callable);
          }
        }, retryDelay, retryTimeUnit);
      } else {

      }
    }
  }

  public RetryingCompletionService(Executor executorService) {
    retryLoop = new RetryLoop();
    completionService = new ExecutorCompletionService<Boolean>(executorService);
    callables = Maps.newConcurrentMap();
    retryDelay = DEFAULT_RETRY_DELAY;
    retryTimeUnit = DEFAULT_RETRY_TIME_UNIT;
    // TODO(damonkohler): Unify this with the passed in ExecutorService.
    retryExecutor = Executors.newSingleThreadScheduledExecutor();
    executorService.execute(retryLoop);
  }

  /**
   * Submit a task to the {@link CompletionService}.
   * 
   * @param callable
   *          the {@link Callable} to execute
   */
  public synchronized void submit(Callable<Boolean> callable) {
    Future<Boolean> future = completionService.submit(callable);
    callables.put(future, callable);
  }

  public void setRetryDelay(long delay, TimeUnit unit) {
    retryDelay = delay;
    retryTimeUnit = unit;
  }

  public void shutdown() {
    // TODO(damonkohler): Need to allow shutdown with timeout and handle
    // shutting down with scheduled retries.
    while (callables.size() > 0) {
      Thread.yield();
    }
    retryExecutor.shutdownNow();
    retryLoop.cancel();
    synchronized (callables) {
      for (Future<Boolean> future : callables.keySet()) {
        future.cancel(true);
      }
    }
  }
}