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

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the listener type
 */
public class EventDispatcher<T> extends CancellableLoop {

  private final T listener;
  private final CircularBlockingQueue<Event<T>> events;

  public EventDispatcher(T listener, int queueCapacity) {
    this.listener = listener;
    events = new CircularBlockingQueue<Event<T>>(queueCapacity);
  }

  @SuppressWarnings("unchecked")
  public ListenableFuture<Void> signal(final SignalRunnable<T> signalRunnable) {
    Event<T> event = new Event<T>(signalRunnable);
    ListenableFuture<List<Void>> futures = Futures.allAsList(events.add(event), event.getFuture());
    return Futures.transform(futures, new Function<List<Void>, Void>() {
      @Override
      public Void apply(List<Void> input) {
        return null;
      }
    });
  }

  @Override
  public void loop() throws InterruptedException {
    Event<T> event = events.take();
    event.run(listener);
  }
}