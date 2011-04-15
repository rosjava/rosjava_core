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

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.namespace.NameResolver;
import org.ros.service.app_manager.ListApps;
import org.ros.service.app_manager.StartApp;
import org.ros.service.app_manager.StopApp;

//TODO(kwc) this class is not meant to be part of rosjava and is only being developed here until rosjava matures
/**
 * Interact with a remote ROS App Manager.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class AppManager {

  private final Node node;
  private ServiceClient<ListApps.Response> listAppsClient;
  private ServiceClient<StopApp.Response> stopAppClient;
  private ServiceClient<StartApp.Response> startAppClient;
  private NameResolver resolver;

  public AppManager(Node node, String robotName) {
    this.node = node;
    listAppsClient = null;
    stopAppClient = null;
    startAppClient = null;

    resolver = node.getResolver().createResolver(robotName);
  }

  private void initListApps() throws AppManagerException {
    if (listAppsClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("list_apps"),
          new ListApps());
      if (serviceIdentifier == null) {
        throw new AppManagerException();
      }
      listAppsClient = node.createServiceClient(serviceIdentifier, ListApps.Response.class);
    }
  }

  private void initStartApp() throws AppManagerException {
    if (startAppClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("start_app"),
          new StartApp());
      if (serviceIdentifier == null) {
        throw new AppManagerException();
      }
      startAppClient = node.createServiceClient(serviceIdentifier, StartApp.Response.class);
    }
  }

  private void initStopApp() throws AppManagerException {
    if (stopAppClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("stop_app"),
          new StopApp());
      if (serviceIdentifier == null) {
        throw new AppManagerException();
      }
      stopAppClient = node.createServiceClient(serviceIdentifier, StopApp.Response.class);
    }
  }

  public void listApps(MessageListener<ListApps.Response> callback)
      throws AppManagerException {
    initListApps();
    listAppsClient.call(new ListApps.Request(), callback);
  }

  public void startApp(String appName, MessageListener<StartApp.Response> callback)
      throws AppManagerException {
    initStartApp();
    StartApp.Request request = new StartApp.Request();
    request.name = appName;
    startAppClient.call(request, callback);
  }

  public void stopApp(String appName, MessageListener<StopApp.Response> callback)
      throws AppManagerException {
    initStopApp();
    StopApp.Request request = new StopApp.Request();
    stopAppClient.call(request, callback);
  }
}
