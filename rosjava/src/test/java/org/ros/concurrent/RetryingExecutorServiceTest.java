/*
 * Copyright (C) 2012 Google Inc.
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

package org.ros.concurrent;

import static org.mockito.Mockito.*;


import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rodrigoq@google.com (Rodrigo Queiro)
 */
public class RetryingExecutorServiceTest {

  private ScheduledExecutorService executorService;

  @Before
  public void before() {
    executorService = Executors.newScheduledThreadPool(4);
  }

  @Test
  public void testNoRetry_calledOnce() throws Exception {
    RetryingExecutorService service = new RetryingExecutorService(executorService);
    Callable<Boolean> callable = mock(Callable.class);
    when(callable.call()).thenReturn(false);
    service.submit(callable);
    service.shutdown(10, TimeUnit.SECONDS);
    verify(callable, times(1)).call();
  }

  @Test
  public void testOneRetry_calledTwice() throws Exception {
    RetryingExecutorService service = new RetryingExecutorService(executorService);
    service.setRetryDelay(0, TimeUnit.SECONDS);
    Callable<Boolean> callable = mock(Callable.class);
    when(callable.call()).thenReturn(true).thenReturn(false);
    service.submit(callable);

    // Call verify() with a timeout before calling shutdown, as shutdown() will prevent further
    // retries.
    verify(callable, timeout(10000).times(2)).call();
    service.shutdown(10, TimeUnit.SECONDS);
  }
}
