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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An interruptable loop that can be run by an {@link ExecutorService}.
 *
 * @author khughes@google.com (Keith M. Hughes)
 */
public abstract class CancellableLoop implements Runnable {
  private static final Object NOT_STARTED = null;

  private static final Object FINISHED = CancellableLoop.class;

  /**
   * State of this loop. Possible values are:
   * <ul>
   * <li>{@link #NOT_STARTED} - corresponds to the 'initial' state, before the loop starts
   * <li>{@link Thread} - 'running' state
   * <li>{@link #FINISHED} - 'finished' state (this can be an arbitrary object)
   * </ul>
   */
  private final AtomicReference<Object> state = new AtomicReference<Object>();

  @Override
  public void run() {
    Thread currentThread = Thread.currentThread();
    if (!state.compareAndSet(NOT_STARTED, currentThread)) {
      throw new IllegalStateException("CancellableLoops cannot be restarted.");
    }

    try {
      setup();
      while (!currentThread.isInterrupted()) {
        loop();
      }
    } catch (InterruptedException e) {
      handleInterruptedException(e);
    } finally {
      state.set(FINISHED);
    }
  }

  /**
   * The setup block for the loop. This will be called exactly once before
   * the first call to {@link #loop()}.
   */
  protected void setup() {
  }

  /**
   * The body of the loop. This will run continuously until the
   * {@link CancellableLoop} has been interrupted externally or by calling
   * {@link #cancel()}.
   */
  protected abstract void loop() throws InterruptedException;

  /**
   * An {@link InterruptedException} was thrown.
   */
  protected void handleInterruptedException(InterruptedException e) {
  }

  /**
   * Interrupts the loop.
   */
  public void cancel() {
    for (; ; ) {
      Object currentState = state.get();
      if (currentState == NOT_STARTED) {
        if (state.compareAndSet(NOT_STARTED, FINISHED)) {
          // cancelled before starting
          return;
        } else {
          // started before we cancelled, try again
          continue;
        }
      // either finished, or we cancel it
      } else if (currentState != FINISHED && state.compareAndSet(currentState, FINISHED)) {
        // first to interrupt
        Thread runningThread = (Thread) currentState;
        runningThread.interrupt();
      }
      return;
    }
  }

  /**
   * @return {@code true} if the loop is running
   */
  public boolean isRunning() {
    Object currentState = state.get();
    return currentState != NOT_STARTED && currentState != FINISHED;
  }
}
