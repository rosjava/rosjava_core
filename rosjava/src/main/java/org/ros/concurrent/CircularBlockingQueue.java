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

import java.util.Queue;

/**
 * A {@link Queue} that removes the old elements when the number of elements
 * exceeds the limit and blocks on {@link #take()} when there are no elements
 * available.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CircularBlockingQueue<T> {

  private final T[] queue;
  private final Object mutex;

  /**
   * Points to the next entry that will be returned by {@link #take()} unless
   * {@link #isEmpty()}.
   */
  private int start;

  /**
   * The number of entries in the queue.
   */
  private int length;

  /**
   * The maximum number of entries in the queue.
   */
  private int limit;

  /**
   * @param capacity
   *          the maximum number of elements allowed in the queue
   */
  @SuppressWarnings("unchecked")
  public CircularBlockingQueue(int capacity) {
    queue = (T[]) new Object[capacity];
    mutex = new Object();
    start = 0;
    length = 0;
    limit = capacity;
  }

  /**
   * Adds the specified entry to the tail of the queue, overwriting older
   * entries if necessary.
   * 
   * @param entry
   *          the entry to add
   */
  public void add(T entry) {
    synchronized (mutex) {
      queue[(start + length) % limit] = entry;
      if (length == limit) {
        start = (start + 1) % limit;
      } else {
        length++;
      }
      mutex.notify();
    }
  }

  /**
   * Returns the oldest entry in the queue, blocking if necessary until an entry
   * is available.
   * 
   * @return the oldest entry
   * @throws InterruptedException
   */
  public T take() throws InterruptedException {
    T entry;
    synchronized (mutex) {
      while (true) {
        if (length > 0) {
          entry = queue[start];
          start = (start + 1) % limit;
          length--;
          break;
        }
        mutex.wait();
      }
    }
    return entry;
  }

  /**
   * @return {@code true} if the queue is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return length == 0;
  }
}
