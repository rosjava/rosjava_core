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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ros.time.RemoteUptimeClock;
import org.ros.time.RemoteUptimeClock.NanoTimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.time.TimeProvider;

import java.util.concurrent.Callable;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RemoteUptimeClockTest {

  private static final long UPTIME_LATENCY_NS = (long) 1e6;
  private static final double SENSITIVITY = 0.1;

  private TimeProvider epochTimeProvider;
  private Time epochTime;
  private NanoTimeProvider nanoTimeProvider;
  private long[] nanoTime;
  private double drift;
  private long[] uptime;
  private RemoteUptimeClock remoteUptimeClock;
  private Callable<Long> uptimeCallable;

  @Before
  public void setup() {
    epochTime = new Time(0, 100);
    epochTimeProvider = new TimeProvider() {
      @Override
      public Time getCurrentTime() {
        return new Time(epochTime);
      }
    };
    nanoTime = new long[] { 0 };
    nanoTimeProvider = new NanoTimeProvider() {
      @Override
      public long nanoTime() {
        return nanoTime[0];
      }
    };
    drift = 1;
    uptime = new long[] { 0 };
    uptimeCallable = new Callable<Long>() {
      public Long call() throws Exception {
        long previousUptime = uptime[0];
        moveTimeForward(UPTIME_LATENCY_NS);
        return (long) uptime[0] - (uptime[0] - previousUptime) / 2;
      }
    };
    remoteUptimeClock =
        new RemoteUptimeClock(epochTimeProvider, nanoTimeProvider, uptimeCallable, SENSITIVITY);
  }

  private void assertTimeEquals(Time expected, Time actual, Duration delta) {
    int nanosecondsDelta = Math.abs(expected.nsecs - actual.nsecs);
    assertTrue(String.format("%s != %s (delta: %s ns)", expected, actual, nanosecondsDelta),
        nanosecondsDelta <= delta.nsecs);
    int secondsDelta = Math.abs(expected.secs - actual.secs);
    assertTrue(String.format("%s != %s (delta: %s)", expected, actual, secondsDelta),
        secondsDelta <= delta.secs);
  }

  private void assertEpochTimeEquals(long uptime, int seconds, int nanoseconds) {
    Time epochTime = remoteUptimeClock.toEpochTime(uptime);
    try {
      assertTimeEquals(new Time(seconds, nanoseconds), epochTime, new Duration(0, 5));
    } catch (AssertionError e) {
      System.err.println(String.format("Uptime: %d", uptime));
      throw e;
    }
  }

  private void assertDriftEquals(double expectedDrift) {
    assertEquals(expectedDrift, remoteUptimeClock.getDrift(), 1e-4);
    for (int i = 0; i < 1000; i++) {
      try {
        assertEpochTimeEquals(uptime[0] - i, 0, (int) (100 + (uptime[0] - i) * expectedDrift));
        assertEpochTimeEquals(uptime[0] + i, 0, (int) (100 + (uptime[0] + i) * expectedDrift));
      } catch (AssertionError e) {
        System.err.println(String.format("Expected drift: %f, Calculated drift: %f", expectedDrift,
            remoteUptimeClock.getDrift()));
        throw e;
      }
    }
  }

  private void moveTimeForward(long nanoseconds) {
    nanoTime[0] += nanoseconds;
    uptime[0] = (long) (nanoTime[0] / drift);
    epochTime.nsecs += nanoseconds;
    epochTime.normalize();
  }

  @Test
  public void testCalibrate() {
    for (int i = 1; i < 10; i++) {
      setup();
      drift = i;
      remoteUptimeClock.calibrate(10);
      assertEquals(i, remoteUptimeClock.getDrift(), 1e-4);
    }
  }

  @Test
  public void testDrift() {
    for (int i = 1; i < 10; i++) {
      setup();
      drift = i;
      remoteUptimeClock.calibrate(10);
      for (int unused = 0; unused < 10; unused++) {
        remoteUptimeClock.update();
        assertDriftEquals(i);
      }
    }
  }
}
