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

package org.ros.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link BlockingQueue} that removes the old elements when the number of
 * elements exceeds the limit.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CircularBlockingQueue<T> {

  private final LinkedBlockingQueue<ListenableEntry<T>> queue;

  /**
   * The number of elements allowed in the queue at one time. Unlike the
   * capacity specified by
   * {@link CircularBlockingQueue#CircularBlockingQueue(int)}, {@link #limit}
   * can be changed at runtime.
   */
  private int limit;

  /**
   * @param capacity
   *          the maximum number of elements allowed in the queue
   */
  public CircularBlockingQueue(int capacity) {
    queue = new LinkedBlockingQueue<ListenableEntry<T>>(capacity);
    this.limit = capacity;
  }

  /**
   * Constructs a new {@link CircularBlockingQueue} with the capacity set to
   * {@link Integer#MAX_VALUE}.
   */
  public CircularBlockingQueue() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Remove elements until the size of the queue is lower than the limit.
   */
  private void shrink() {
    while (queue.size() > limit) {
      ListenableEntry<T> listenableEntry = queue.remove();
      listenableEntry.getFuture().setException(new DroppedEntryException());
    }
  }

  /**
   * Adjusts the limit on the number of elements allowed in the queue.
   * 
   * @param limit
   *          the number of elements allowed in the queue
   */
  public void setLimit(int limit) {
    this.limit = limit;
    shrink();
  }

  /**
   * @return the number of elements allowed in the queue
   */
  public int getLimit() {
    return limit;
  }

  /**
   * @return the number of elements in the queue
   */
  public int getSize() {
    return queue.size();
  }

  /**
   * Adds the specified entry to the tail of the queue, waiting if necessary for
   * space to become available and then shrinking the queue if necessary to stay
   * within the queue's limit.
   * <p>
   * When the queue shrinks, older entries are removed first.
   * 
   * @param entry
   *          the entry to add
   * @return a {@link ListenableFuture} whose result will be set when the item
   *         is removed from the queue or whose exception will be set when the
   *         item is dropped from the queue
   * @throws InterruptedException
   */
  public ListenableFuture<Void> put(T entry) throws InterruptedException {
    ListenableEntry<T> listenableEntry = new ListenableEntry<T>(entry);
    synchronized (queue) {
      try {
        queue.put(listenableEntry);
      } finally {
        shrink();
      }
      queue.notify();
    }
    return listenableEntry.getFuture();
  }

  /**
   * @see LinkedBlockingQueue#take()
   */
  public T take() throws InterruptedException {
    ListenableEntry<T> listenableEntry;
    synchronized (queue) {
      while (true) {
        listenableEntry = queue.poll();
        if (listenableEntry != null) {
          break;
        }
        queue.wait();
      }
    }
    listenableEntry.getFuture().set(null);
    return listenableEntry.getEntry();
  }
}
