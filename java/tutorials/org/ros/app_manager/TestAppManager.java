package org.ros.app_manager;

import org.ros.service.app_manager.StartApp;

import org.ros.message.app_manager.App;

import org.apache.commons.logging.Log;
import org.ros.Node;
import org.ros.NodeContext;
import org.ros.NodeMain;

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
      
      ArrayList<App> availableApps = appManager.getAvailableApps();
      for (App app: availableApps) { 
        log.info("Available app: "+app.display_name);
      }
      
      ArrayList<App> runningApps = appManager.getRunningApps();
      for (App app: runningApps) { 
        log.info("Running app: "+app.display_name);
      }
      
      log.info("calling start app");
      StartApp.Response startResponse = appManager.startApp("foo/fakeApp");
      log.info("start app called");
      if (!startResponse.started) { 
        log.info("fake app failed to start (this is good).  response message is: "+startResponse.message);
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
