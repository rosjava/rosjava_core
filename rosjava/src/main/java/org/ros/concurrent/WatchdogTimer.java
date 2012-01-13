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

import java.util.Timer;
import java.util.TimerTask;

// TODO(damonkohler): Change this to use a ScheduledExecutorService.
/**
 * A {@link WatchdogTimer} expects to receive a {@link #pulse()} at least once
 * every {@link #period} milliseconds. If a {@link #pulse()} is not received, it
 * will execute the provided {@link Runnable}
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class WatchdogTimer {

  private final long period;
  private final Timer timer;
  private final TimerTask task;

  private boolean pulsed;

  public WatchdogTimer(long period, final Runnable runnable) {
    this.period = period;
    pulsed = false;
    timer = new Timer();
    task = new TimerTask() {
      @Override
      public void run() {
        if (!pulsed) {
          runnable.run();
        }
        pulsed = false;
      }
    };
  }

  public void start() {
    timer.scheduleAtFixedRate(task, period, period);
  }

  public void pulse() {
    pulsed = true;
  }

  public void cancel() {
    task.cancel();
    timer.cancel();
  }
}
