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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CircularBlockingQueueTest {

  private ExecutorService executorService;

  @Before
  public void before() {
    executorService = Executors.newCachedThreadPool();
  }

  @Test
  public void testAddAndTake() throws InterruptedException {
    CircularBlockingQueue<String> queue = new CircularBlockingQueue<String>(10);
    String expectedString1 = "Hello, world!";
    String expectedString2 = "Goodbye, world!";
    queue.add(expectedString1);
    queue.add(expectedString2);
    assertEquals(expectedString1, queue.take());
    assertEquals(expectedString2, queue.take());
  }

  @Test
  public void testOverwrite() throws InterruptedException {
    CircularBlockingQueue<String> queue = new CircularBlockingQueue<String>(2);
    String expectedString = "Hello, world!";
    queue.add("overwritten");
    queue.add(expectedString);
    queue.add("foo");
    assertEquals(expectedString, queue.take());
  }

  @Test
  public void testIterator() throws InterruptedException {
    CircularBlockingQueue<String> queue = new CircularBlockingQueue<String>(10);
    String expectedString1 = "Hello, world!";
    String expectedString2 = "Goodbye, world!";
    queue.add(expectedString1);
    queue.add(expectedString2);
    Iterator<String> iterator = queue.iterator();
    assertEquals(expectedString1, iterator.next());
    assertEquals(expectedString2, iterator.next());
    assertFalse(iterator.hasNext());
    queue.take();
    iterator = queue.iterator();
    assertEquals(expectedString2, iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testBlockingTake() throws InterruptedException {
    final CircularBlockingQueue<String> queue = new CircularBlockingQueue<String>(1);
    final String expectedString = "Hello, world!";
    final CountDownLatch latch = new CountDownLatch(1);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          assertEquals(expectedString, queue.take());
        } catch (InterruptedException e) {
          fail();
        }
        latch.countDown();
      }
    });
    // Sleep to ensure we're waiting on take().
    Thread.sleep(5);
    queue.add(expectedString);
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }
}
