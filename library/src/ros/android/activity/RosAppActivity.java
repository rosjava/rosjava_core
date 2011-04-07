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

package ros.android.activity;

import org.ros.app_manager.AppManager;
import org.ros.app_manager.AppManagerNotAvailableException;
import org.ros.app_manager.AppNotInstalledException;
import org.ros.exceptions.RosInitException;
import org.ros.message.app_manager.App;

import java.util.ArrayList;

/**
 * Activity for Android that acts as a client for an external ROS app.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosAppActivity extends RosActivity {

  public RosAppActivity() {

  }

  protected AppManager createAppManager() throws RosInitException {
    String robotName = "turtlebot";
    return new AppManager(getNode(), robotName);
  }

  /**
   * Start ROS app if it is not already running.
   * @param appName
   * @throws RosInitException
   * @throws AppManagerNotAvailableException
   * @throws AppNotInstalledException
   */
  public void ensureAppRunning(String appName) throws RosInitException, AppManagerNotAvailableException,
      AppNotInstalledException {
    // TODO(kwc) create an explicit start app routine instead
    
    AppManager appManager = createAppManager();
    ArrayList<App> availableApps = appManager.getAvailableApps();
    boolean installed = false;
    for (App app : availableApps) {
      if (app.name.equals(appName)) {
        installed = true;
      }
    }
    if (!installed) {
      throw new AppNotInstalledException("App is not installed");
    }
    ArrayList<App> runningApps = appManager.getRunningApps();
    for (App app : runningApps) {
      if (app.name.equals(appName)) {
        return;
      }
    }
    appManager.startApp(appName);
  }

}
