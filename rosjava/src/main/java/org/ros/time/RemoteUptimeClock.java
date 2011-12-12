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

package org.ros.time;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.message.Duration;
import org.ros.message.Time;

import java.util.concurrent.Callable;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RemoteUptimeClock {

  private static final Log log = LogFactory.getLog(RemoteUptimeClock.class);

  private final TimeProvider epochTimeProvider;
  private final NanoTimeProvider nanoTimeProvider;
  private final Callable<Long> callable;
  private final double sensitivity;

  private Time epochTime;
  private long uptime;
  private long nanoseconds;
  private double drift;

  private class UptimeCalculationResult {
    final Time newEpochTime;
    final long newNanoseconds;
    final long newUptime;

    public UptimeCalculationResult(Time newEpochTime, long newNanoseconds, long newUptime) {
      this.newEpochTime = newEpochTime;
      this.newNanoseconds = newNanoseconds;
      this.newUptime = newUptime;
    }
  }

  @VisibleForTesting
  interface NanoTimeProvider {
    long nanoTime();
  }

  /**
   * The provided {@link Callable} should return the current uptime of the
   * remote clock with minimal overhead since the run time of this call will be
   * used to further improve the estimation of uptime.
   * 
   * @param epochTimeProvider
   * @param callable
   *          returns the current uptime in arbitrary units
   * @param sensitivity
   * @return
   */
  public static RemoteUptimeClock create(TimeProvider epochTimeProvider, Callable<Long> callable,
      double sensitivity) {
    return new RemoteUptimeClock(epochTimeProvider, new NanoTimeProvider() {
      @Override
      public long nanoTime() {
        return System.nanoTime();
      }
    }, callable, sensitivity);
  }

  @VisibleForTesting
  RemoteUptimeClock(TimeProvider epochTimeProvider, NanoTimeProvider nanoTimeProvider,
      Callable<Long> callable, double sensitivity) {
    this.epochTimeProvider = epochTimeProvider;
    this.nanoTimeProvider = nanoTimeProvider;
    this.callable = callable;
    this.sensitivity = sensitivity;
    epochTime = null;
  }

  public void calibrate(int sampleSize) {
    uptime = 0;
    nanoseconds = 0;
    long nanosecondsDelta = 0;
    long uptimeDelta = 0;
    for (int i = 0; i < sampleSize; i++) {
      UptimeCalculationResult result = calculateNewUptime(callable);
      if (i > 0) {
        nanosecondsDelta += result.newNanoseconds - nanoseconds;
        uptimeDelta += result.newUptime - uptime;
      }
      uptime = result.newUptime;
      nanoseconds = result.newNanoseconds;
      epochTime = result.newEpochTime;
    }
    drift = calculateDrift(nanosecondsDelta, uptimeDelta);
  }

  private double calculateDrift(long nanosecondsDelta, long uptimeDelta) {
    Preconditions.checkState(uptimeDelta > 0);
    return (double) nanosecondsDelta / uptimeDelta;
  }

  /**
   * Update this {@link RemoteUptimeClock} with the latest uptime from the
   * remote clock.
   */
  public void update() {
    UptimeCalculationResult result = calculateNewUptime(callable);
    long newNanoseconds = result.newNanoseconds;
    long newUptime = result.newUptime;
    long nanosecondsDelta = newNanoseconds - nanoseconds;
    long uptimeDelta = newUptime - uptime;
    double newDrift = calculateDrift(nanosecondsDelta, uptimeDelta);
    long error = nanosecondsDelta - (long) (uptimeDelta * drift);
    log.info(String.format("Drift: %.4f, Error: %d ns", newDrift, error));
    drift = (1 - sensitivity) * drift + sensitivity * newDrift;
    uptime = newUptime;
    nanoseconds = newNanoseconds;
    epochTime = result.newEpochTime;
  }

  private UptimeCalculationResult calculateNewUptime(Callable<Long> callable) {
    Time newEpochTime = epochTimeProvider.getCurrentTime();
    long newNanoseconds = nanoTimeProvider.nanoTime();
    long newUptime;
    try {
      newUptime = callable.call();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
    long offset = (nanoTimeProvider.nanoTime() - newNanoseconds) / 2;
    newNanoseconds += offset;
    newEpochTime = newEpochTime.add(Duration.fromNano(offset));
    return new UptimeCalculationResult(newEpochTime, newNanoseconds, newUptime);
  }

  public Time toEpochTime(long uptime) {
    Preconditions.checkNotNull(epochTime, "No data available to estimate epoch time.");
    return epochTime.add(Duration.fromNano((long) ((uptime - this.uptime) * drift)));
  }

  @VisibleForTesting
  double getDrift() {
    return drift;
  }
}
