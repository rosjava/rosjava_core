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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.message.app_manager.App;
import org.ros.service.app_manager.ListApps;
import ros.android.activity.RosAppActivity;
import ros.android.views.TurtlebotDashboard;

import java.util.ArrayList;

/**
 * Show a grid of applications that a given robot is capable of, and launch
 * whichever is chosen.
 */
public class AppChooser extends RosAppActivity {

  private ArrayList<App> availableAppsCache;
  private long availableAppsCacheTime;
  private TurtlebotDashboard dashboard;

  public AppChooser() {
    availableAppsCache = new ArrayList<App>();
    availableAppsCacheTime = 0;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    dashboard = (TurtlebotDashboard) findViewById( R.id.dashboard );
  }

  @Override
  protected void onResume() {
    super.onResume();
    // TODO: start spinner
    updateAppList(availableAppsCache);
  }

  /**
   * Must be run in UI thread.
   * 
   * @param apps
   */
  protected void updateAppList(final ArrayList<App> apps) {
    Log.i("RosAndroid", "updating gridview");
    GridView gridview = (GridView) findViewById(R.id.gridview);
    gridview.setAdapter(new AppAdapter(AppChooser.this, apps));
    gridview.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        AppLauncher.launch(AppChooser.this, apps.get(position));
      }
    });
    Log.i("RosAndroid", "gridview updated");
  }

  @Override
  protected void onNodeCreate(Node node) {
    Log.i("RosAndroid", "AppChooser.onNodeCreate");
    super.onNodeCreate(node);
    dashboard.setNode(node);
    if (System.currentTimeMillis() - availableAppsCacheTime < 2 * 1000) {
      Log.i("RosAndroid", "using app cache");
      return;
    }
    if (appManager == null) {
      safeSetStatus("Robot not available");
      return;
    }
    Log.i("RosAndroid", "sending list apps request");
    appManager.listApps(new MessageListener<ListApps.Response>() {

      @Override
      public void onSuccess(ListApps.Response message) {
        availableAppsCache = message.available_apps;
        Log.i("RosAndroid", "ListApps.Response: " + availableAppsCache.size() + " apps");
        availableAppsCacheTime = System.currentTimeMillis();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            updateAppList(availableAppsCache);
          }
        });
      }

      @Override
      public void onFailure(Exception e) {
        // TODO Auto-generated method stub
        
      }
    });

  }

  @Override
  protected void onNodeDestroy(Node node) {
    Log.i("RosAndroid", "onNodeDestroy");
    super.onNodeDestroy(node);
    dashboard.setNode(null);
  }

  public void chooseNewMasterClicked(View view) {
    chooseNewMaster();
  }

  private void setStatus(String status_message) {
    TextView statusView = (TextView) findViewById(R.id.status_view);
    if (statusView != null) {
      statusView.setText(status_message);
    }
  }

  private void safeSetStatus(final String statusMessage) {
    final TextView statusView = (TextView) findViewById(R.id.status_view);
    if (statusView != null) {
      statusView.post(new Runnable() {

        @Override
        public void run() {
          statusView.setText(statusMessage);
        }
      });
    }
  }
}
