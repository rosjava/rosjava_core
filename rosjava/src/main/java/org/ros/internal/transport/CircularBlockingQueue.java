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

package org.ros.internal.transport;

import com.google.common.base.Preconditions;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link BlockingQueue} that removes the old elements when the number of
 * elements exceeds the limit.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CircularBlockingQueue<T> extends LinkedBlockingQueue<T> {

  private final int capacity;

  /**
   * The number of elements allowed in the queue at one time. Unlike
   * {@link #capacity}, this can be changed at runtime and is only best effort.
   */
  private int limit;

  /**
   * @param capacity
   *          the maximum number of elements allowed in the queue
   */
  public CircularBlockingQueue(int capacity) {
    super(capacity);
    this.capacity = capacity;
    this.limit = capacity;
  }

  /**
   * Remove elements until the size of the queue is lower than the limit.
   */
  private void shrink() {
    while (size() > limit) {
      remove();
    }
  }

  /**
   * Sets a soft limit on the number of elements allowed in the queue.
   * 
   * @param limit
   *          the number of elements allowed in the queue
   */
  public void setLimit(int limit) {
    Preconditions.checkArgument(limit <= capacity);
    this.limit = limit;
    shrink();
  }

  /**
   * @return the number of elements allowed in the queue
   */
  public int getLimit() {
    return limit;
  }

  @Override
  public void put(T entry) {
    shrink();
    try {
      super.put(entry);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
