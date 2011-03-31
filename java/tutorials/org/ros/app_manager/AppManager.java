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

import org.ros.namespace.NameResolver;

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.message.Message;
import org.ros.message.app_manager.App;
import org.ros.service.app_manager.ListApps;
import org.ros.service.app_manager.StartApp;
import org.ros.service.app_manager.StopApp;

import java.util.ArrayList;

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

  private void initListApps() throws AppManagerNotAvailableException {
    if (listAppsClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("list_apps"), new ListApps());
      if (serviceIdentifier == null) {
        throw new AppManagerNotAvailableException();
      }
      listAppsClient = node.createServiceClient(serviceIdentifier, ListApps.Response.class);
    }
  }

  private void initStartApp() throws AppManagerNotAvailableException {
    if (startAppClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("start_app"), new StartApp());
      if (serviceIdentifier == null) {
        throw new AppManagerNotAvailableException();
      }
      startAppClient = node.createServiceClient(serviceIdentifier, StartApp.Response.class);
    }
  }

  private void initStopApp() throws AppManagerNotAvailableException {
    if (stopAppClient == null) {
      ServiceIdentifier serviceIdentifier = node.lookupService(resolver.resolveName("stop_app"), new StopApp());
      if (serviceIdentifier == null) {
        throw new AppManagerNotAvailableException();
      }
      stopAppClient = node.createServiceClient(serviceIdentifier, StopApp.Response.class);
    }
  }

  static class ResponseListener<T extends Message> implements MessageListener<T> {
    private T response;

    ResponseListener() {
      response = null;
    }

    @Override
    public void onNewMessage(T message) {
      System.out.println("got response");
      response = message;
    }

    public T getResponse() {
      return response;
    }

    public void waitForResponse() {
      // TODO: need equivalent of node.ok()
      while (getResponse() == null) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private ListApps.Response callListApps() throws AppManagerNotAvailableException {
    initListApps();
    ListApps.Request request = new ListApps.Request();

    ResponseListener<ListApps.Response> listener = new ResponseListener<ListApps.Response>();
    listAppsClient.call(request, listener);
    listener.waitForResponse();
    return listener.getResponse();
  }

  private StartApp.Response callStartApp(String appName) throws AppManagerNotAvailableException {
    initStartApp();
    StartApp.Request request = new StartApp.Request();
    request.name = appName;
    
    ResponseListener<StartApp.Response> listener = new ResponseListener<StartApp.Response>();
    startAppClient.call(request, listener);
    listener.waitForResponse();
    return listener.getResponse();
  }

  private StopApp.Response callStopApp() throws AppManagerNotAvailableException {
    initStopApp();
    StopApp.Request request = new StopApp.Request();

    ResponseListener<StopApp.Response> listener = new ResponseListener<StopApp.Response>();
    stopAppClient.call(request, listener);
    listener.waitForResponse();
    return listener.getResponse();
  }

  public ArrayList<App> getRunningApps() throws AppManagerNotAvailableException {
    ListApps.Response response = callListApps();
    return response.running_apps;
  }

  public ArrayList<App> getAvailableApps() throws AppManagerNotAvailableException {
    ListApps.Response response = callListApps();
    return response.available_apps;
  }

  public StartApp.Response startApp(String appName) throws AppManagerNotAvailableException {
    return callStartApp(appName);
  }

  public void stopApp(String appName) throws AppManagerNotAvailableException {
    // TODO: pass in app name
    callStopApp();
  }
}
