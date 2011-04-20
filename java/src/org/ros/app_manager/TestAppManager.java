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

package org.ros.app_manager;

import org.apache.commons.logging.Log;
import org.ros.Node;
import org.ros.NodeContext;
import org.ros.NodeMain;
import org.ros.message.app_manager.App;
import org.ros.service.app_manager.ListApps;
import org.ros.service.app_manager.StartApp;

import java.util.ArrayList;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class TestAppManager implements NodeMain {

  private AppManager appManager;

  @Override
  public void run(NodeContext context) {
    Node node = null;
    try {
      node = new Node("app_manager_client", context);
      final Log log = node.getLog();
      log.info("Creating app manager");
      appManager = new AppManager(node, "turtlebot");

      log.info("Listing apps");
      BasicAppManagerCallback<ListApps.Response> callback = new BasicAppManagerCallback<ListApps.Response>();
      appManager.listApps(callback);
      ArrayList<App> availableApps = callback.waitForResponse(10 * 1000).available_apps;
      for (App app : availableApps) {
        log.info("Available app: " + app.display_name);
      }
      log.info("Getting running apps");
      appManager.listApps(callback);
      ArrayList<App> runningApps = callback.waitForResponse(10 * 1000).running_apps;
      for (App app : runningApps) {
        log.info("Running app: " + app.display_name);
      }

      log.info("calling start app");
      BasicAppManagerCallback<StartApp.Response> startCallback = new BasicAppManagerCallback<StartApp.Response>();
      appManager.startApp("foo/fakeApp", startCallback);
      StartApp.Response startResponse = startCallback.waitForResponse(10 * 1000);
      log.info("start app called");
      if (!startResponse.started) {
        log.info("fake app failed to start (this is good).  response message is: "
            + startResponse.message);
      } else {
        log.error("fake app started, this is weird");
      }

      /*
      StartAppFuture startAppFuture = new StartAppFuture(appManager,
          "turtlebot_teleop/android_teleop", false);
      startAppFuture.start();
      while (startAppFuture.getResultCode() == StartAppFuture.PENDING) {
        Thread.sleep(100);
      }
      System.out.println(startAppFuture.getResultMessage());
*/
    } catch (Exception e) {
      if (node != null) {
        node.getLog().fatal(e);
      } else {
        e.printStackTrace();
      }
    }
    System.exit(0);
  }
}
