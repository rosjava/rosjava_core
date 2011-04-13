/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2011, Willow Garage, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * Neither the name of Willow Garage, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.app_manager;

import org.ros.message.app_manager.App;
import org.ros.message.app_manager.StatusCodes;
import org.ros.service.app_manager.ListApps;
import org.ros.service.app_manager.StartApp;
import org.ros.service.app_manager.StopApp;

import java.util.ArrayList;

//Very, very volatile
public class StartAppFuture {

  private final boolean restart;
  private final String appName;
  private final AppManagerCb appManager;

  public static final int PENDING = -1;
  public static final int SUCCESS = 0;
  public static final int APP_MANAGER_NOT_AVAILABLE = 1;
  public static final int RESTART_FAILED = 2;
  public static final int UNKNOWN_ERROR = 10;
  public static final int NOT_INSTALLED = StatusCodes.NOT_FOUND;

  private int resultCode;
  private String resultMessage;

  public StartAppFuture(AppManagerCb appManager, String appName, boolean restart) {
    this.appManager = appManager;
    this.appName = appName;
    this.restart = restart;

    resultCode = PENDING;
    resultMessage = null;
  }

  class ListAppsResponseListener implements AppManagerCallback<ListApps.Response> {

    @Override
    public void onNewMessage(ListApps.Response message) {
      boolean installed = false;
      for (App app : message.available_apps) {
        if (app.name.equals(appName)) {
          installed = true;
        }
      }
      if (!installed) {
        setResult(NOT_INSTALLED, "App is not installed on robot");
        return;
      }

      // Check to see if already running.
      if (!restart) {
        ArrayList<App> runningApps = message.running_apps;
        for (App app : runningApps) {
          if (app.name.equals(appName)) {
            setResult(SUCCESS, "already started");
            return;
          }
        }
      }

      if (restart) {
        appManager.stopApp("*", new StopAppsResponseListener());
      } else {
        appManager.startApp(appName, new StartAppsResponseListener());
      }

    }

    @Override
    public void callFailed(AppManagerException e) {
      setResult(APP_MANAGER_NOT_AVAILABLE, e.getMessage());
    }

  }

  class StartAppsResponseListener implements AppManagerCallback<StartApp.Response> {

    @Override
    public void onNewMessage(StartApp.Response message) {
      if (message.started) {
        setResult(SUCCESS, "app started");
      } else {
        if (message.error_code == StatusCodes.MULTIAPP_NOT_SUPPORTED) {
          setResult(UNKNOWN_ERROR,
              "Unable to start app. Someone else may be attempting to run apps on the robot.");
        } else if (message.error_code == StatusCodes.APP_INVALID) {
          // In the future can preserve granularity.
          setResult(NOT_INSTALLED, "App is installation is not valid on the robot");
        } else if (message.error_code == StatusCodes.NOT_FOUND) {
          setResult(NOT_INSTALLED, "App is not installed on the robot");
        } else if (message.error_code == StatusCodes.INTERNAL_ERROR) {
          setResult(UNKNOWN_ERROR, message.message);
        } else {
          setResult(UNKNOWN_ERROR, message.message);
        }
      }
    }

    @Override
    public void callFailed(AppManagerException e) {
      setResult(APP_MANAGER_NOT_AVAILABLE, e.getMessage());
    }
  }

  class StopAppsResponseListener implements AppManagerCallback<StopApp.Response> {

    @Override
    public void onNewMessage(StopApp.Response message) {
      if (message.stopped || message.error_code == StatusCodes.NOT_RUNNING) {
        appManager.startApp(appName, new StartAppsResponseListener());
      } else {
        setResult(RESTART_FAILED, "App did not successfully restart");
      }
    }

    @Override
    public void callFailed(AppManagerException e) {
      setResult(APP_MANAGER_NOT_AVAILABLE, e.getMessage());
    }
  }

  public void start() {
    ListAppsResponseListener listAppsCallback = new ListAppsResponseListener();
    appManager.listApps(listAppsCallback);
  }

  void setResult(int resultCode, String resultMessage) {
    this.resultCode = resultCode;
    this.resultMessage = resultMessage;
  }

  /**
   * @return Result code for operation. Result code is {@code PENDING} if
   *         operation has not completed.
   */
  public int getResultCode() {
    return resultCode;
  }

  /**
   * @return Result message for operation. Callers should first check
   *         {@code getResultCode()} to determine whether or not operation has
   *         completed.
   */
  public String getResultMessage() {
    return resultMessage;
  }
}
