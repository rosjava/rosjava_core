package org.ros.app_manager;

import org.apache.commons.logging.Log;
import org.ros.Node;
import org.ros.NodeContext;
import org.ros.NodeMain;
import org.ros.message.app_manager.App;
import org.ros.service.app_manager.ListApps;
import org.ros.service.app_manager.StartApp;

import java.util.ArrayList;
import java.util.List;

public class TestAppManager implements NodeMain {
  private AppManager appManager;

  @Override
  public void run(List<String> argv, NodeContext context) {
    Node node = null;
    try {
      node = new Node("app_manager_client", context);
      final Log log = node.getLog();
      log.info("Creating app manager");
      appManager = new AppManager(node, "robot1");

      log.info("Listing apps");
      BasicMessageListener<ListApps.Response> callback = new BasicMessageListener<ListApps.Response>();
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
      BasicMessageListener<StartApp.Response> startCallback = new BasicMessageListener<StartApp.Response>();
      appManager.startApp("foo/fakeApp", startCallback);
      StartApp.Response startResponse = startCallback.waitForResponse(10 * 1000);
      log.info("start app called");
      if (!startResponse.started) {
        log.info("fake app failed to start (this is good).  response message is: "
            + startResponse.message);
      } else {
        log.error("fake app started, this is weird");
      }

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
