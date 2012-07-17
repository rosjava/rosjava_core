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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *          the listener type
 */
public class Event<T> {

  private final SignalRunnable<T> signalRunnable;
  private final SettableFuture<Void> future;

  public Event(SignalRunnable<T> signalRunnable) {
    this.signalRunnable = signalRunnable;
    future = SettableFuture.create();
  }

  public void run(T listener) {
    signalRunnable.run(listener);
    future.set(null);
  }

  public ListenableFuture<Void> getFuture() {
    return future;
  }
}