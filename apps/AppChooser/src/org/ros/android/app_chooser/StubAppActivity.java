/*
 * Copyright (c) 2011, Willow Garage, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Willow Garage, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.android.app_chooser;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.ros.app_manager.AppManagerCallback;
import org.ros.app_manager.AppManagerException;
import org.ros.message.app_manager.StatusCodes;
import org.ros.service.app_manager.StartApp;
import org.ros.service.app_manager.StopApp;
import ros.android.activity.AppStartCallback;
import ros.android.activity.RosAppActivity;

public class StubAppActivity extends RosAppActivity implements AppStartCallback {
  private String robotAppName;
  private String robotAppDisplayName;
  private TextView statusView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    robotAppName = getIntent().getStringExtra(AppLauncher.PACKAGE + ".robot_app_name");
    robotAppDisplayName = getIntent().getStringExtra(
        AppLauncher.PACKAGE + ".robot_app_display_name");

    setTitle(robotAppDisplayName);
    setContentView(R.layout.stub_app);
    statusView = (TextView) findViewById(R.id.status_view);
  }

  private void startApp() {
    appManager.startApp(robotAppName, new AppManagerCallback<StartApp.Response>() {
      @Override
      public void onNewMessage(StartApp.Response message) {
        if (message.started) {
          safeSetStatus("started");
        } else {
          safeSetStatus(message.message);
        }
      }

      @Override
      public void callFailed(AppManagerException e) {
        safeSetStatus("Failed: " + e.getMessage());
      }
    });
  }

  public void onStartClicked(View view) {
    setStatus("Starting...");
    // TODO: add guard so that we cannot start multiple times
    startApp();
    setStatus("Launching");
  }

  public void onStopClicked(View view) {
    setStatus("Stopping...");
    appManager.stopApp("*", new AppManagerCallback<StopApp.Response>() {

      @Override
      public void onNewMessage(StopApp.Response message) {
        if (message.stopped || message.error_code == StatusCodes.NOT_RUNNING) {
          safeSetStatus("Stopped.");
        } else {
          safeSetStatus("ERROR: " + message.message);
        }
      }

      @Override
      public void callFailed(AppManagerException e) {
        safeSetStatus("Failed: cannot contact robot!");
      }
    });
  }

  public void onExitClicked(View view) {
    finish();
  }

  /**
   * Set the status text. Safe to call from any thread.
   */
  private void safeSetStatus(final String status) {
    statusView.post(new Runnable() {
      @Override
      public void run() {
        setStatus(status);
      }
    });
  }

  private void setStatus(String status) {
    statusView.setText(status);
  }

  @Override
  public void appStartResult(boolean success, int resultCode, String message) {
    if (success) {
      safeSetStatus("started");
    } else {
      safeSetStatus(message);
    }
  }
}
