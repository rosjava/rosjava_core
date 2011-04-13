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

import org.ros.app_manager.BasicAppManagerCallback.TimeoutException;

import org.ros.app_manager.AppManagerException;

import org.ros.service.app_manager.StartApp;

import org.ros.app_manager.BasicAppManagerCallback;

import org.ros.app_manager.AppManagerCb;

import android.util.Log;
import org.ros.Node;
import org.ros.app_manager.AppManager;
import org.ros.app_manager.StartAppFuture;
import org.ros.exceptions.RosInitException;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import ros.android.util.RobotDescription;

/**
 * Activity for Android that acts as a client for an external ROS app.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosAppActivity extends RosActivity {

  public RosAppActivity() {

  }

  protected AppManager createAppManager() throws RosInitException {
    RobotDescription robotDescription = getCurrentRobot();
    if (robotDescription == null) {
      throw new RosInitException("no robot available");
    } else {
      Log.i("RosAndroid", "Using Robot: " + robotDescription.robotName + " "
          + robotDescription.masterUri);
      return new AppManager(getNode(), robotDescription.robotName);
    }
  }

  protected AppManagerCb createAppManagerCb() throws RosInitException {
    RobotDescription robotDescription = getCurrentRobot();
    if (robotDescription == null) {
      throw new RosInitException("no robot available");
    } else {
      Log.i("RosAndroid", "Using Robot: " + robotDescription.robotName + " "
          + robotDescription.masterUri);
      return new AppManagerCb(getNode(), robotDescription.robotName);
    }
  }

  protected Namespace getAppNamespace() throws RosInitException {
    RobotDescription robotDescription = getCurrentRobot();
    if (robotDescription == null) {
      throw new RosInitException("no robot available");
    }
    Node node = getNode();
    if (node == null) {
      throw new RosInitException("node not available");
    }
    return node.createNamespace(NameResolver.join(robotDescription.robotName, "application"));
  }

  public boolean startApp(final String appName) {
    AppManager appManager;
    final BasicAppManagerCallback<StartApp.Response> callback;
    try {
      appManager = createAppManager();
      callback = new BasicAppManagerCallback<StartApp.Response>();
      appManager.startApp(appName, callback);
      callback.waitForResponse(10 * 1000);
      return true;
    } catch (RosInitException e1) {
      return false;
    } catch (AppManagerException e) {
      return false;
    } catch (TimeoutException e) {
      return false;
    }
  }

  /**
   * Start ROS app if it is not already running.
   * 
   * @param appName
   * @param restart
   *          If true, will restart the app if it is already running.
   * @param statusCallback
   * @throws RosInitException
   */
  public void startAppCb(String appName, boolean restart, final AppStartCallback callback)
      throws RosInitException {
    AppManagerCb appManager = createAppManagerCb();
    final StartAppFuture startAppFuture = new StartAppFuture(appManager, appName, restart);
    startAppFuture.start();

    Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          long timeoutT = System.currentTimeMillis() + 30 * 1000;
          while (startAppFuture.getResultCode() == StartAppFuture.PENDING
              && System.currentTimeMillis() < timeoutT) {
            Thread.sleep(100);
          }
          if (startAppFuture.getResultCode() != StartAppFuture.SUCCESS) {
            callback.appStartResult(false, "Failed to start: " + startAppFuture.getResultMessage());
          } else {
            callback.appStartResult(true, "success");
          }

        } catch (InterruptedException e) {
          callback.appStartResult(false, "Cancelled");
        }
      }

    });
    thread.start();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

}
