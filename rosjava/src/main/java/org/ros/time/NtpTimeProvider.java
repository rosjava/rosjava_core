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

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.ros.message.Duration;
import org.ros.message.Time;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Provides NTP synchronized wallclock (actual) time.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NtpTimeProvider implements TimeProvider {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(NtpTimeProvider.class);

  private final InetAddress host;
  private final NTPUDPClient ntpClient;
  private final WallTimeProvider wallTimeProvider;

  private TimeInfo time;
  private Timer timer;

  /**
   * @param host
   *          the NTP host to use
   */
  public NtpTimeProvider(InetAddress host) {
    this.host = host;
    this.wallTimeProvider = new WallTimeProvider();
    ntpClient = new NTPUDPClient();
  }

  /**
   * Update the current time offset from the configured NTP host.
   * 
   * @throws IOException
   */
  public void updateTime() throws IOException {
    if (DEBUG) {
      log.info("Updating time offset from NTP server: " + host.getHostName());
    }
    try {
      time = ntpClient.getTime(host);
    } catch (IOException e) {
      log.error("Failed to read time from NTP server: " + host.getHostName(), e);
      throw e;
    }
    time.computeDetails();
    log.info(String.format("NTP time offset: %d ms", time.getOffset()));
  }

  /**
   * Starts periodically updating the current time offset periodically.
   * 
   * <p>
   * The first time update happens immediately.
   * 
   * <p>
   * Note that errors thrown while periodically updating time will be logged but
   * not rethrown.
   * 
   * @param period
   *          time between updates
   * @param unit
   *          unit of period
   */
  public void startPeriodicUpdates(long period, TimeUnit unit) {
    Preconditions.checkState(timer == null);
    timer = new Timer();
    long periodInMilliseconds = TimeUnit.MILLISECONDS.convert(period, unit);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try {
          updateTime();
        } catch (IOException e) {
          log.error("Periodic NTP update failed.", e);
        }
      }
    }, 0, periodInMilliseconds);
  }

  /**
   * Stops periodically updating the current time offset.
   */
  public void stopPeriodicUpdates() {
    Preconditions.checkNotNull(timer);
    timer.cancel();
    timer = null;
  }

  @Override
  public Time getCurrentTime() {
    Time currentTime = wallTimeProvider.getCurrentTime();
    long offset = time.getOffset();
    return currentTime.add(Duration.fromMillis(offset));
  }
}
