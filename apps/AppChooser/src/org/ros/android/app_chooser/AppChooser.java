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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.view.View;

import org.ros.message.app_manager.App;
import org.ros.Node;
import org.ros.app_manager.AppManager;
import org.ros.app_manager.AppManagerNotAvailableException;
import org.ros.exceptions.RosInitException;

import ros.android.activity.RosActivity;
import ros.android.util.RobotDescription;

/** Show a grid of applications that a given robot is capable of, and
 * launch whichever is chosen. */
public class AppChooser extends RosActivity
{
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateList();
  }

  private void updateList() {
    setContentView(R.layout.main);
    final ArrayList<App> apps = getAppList();
    GridView gridview = (GridView) findViewById(R.id.gridview);
    gridview.setAdapter(new AppAdapter(this, apps));

    gridview.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          AppLauncher.launch( AppChooser.this, apps.get( position ));
        }
      });
  }

  public void chooseNewMasterClicked( View view ) {
    chooseNewMaster();
  }

  public ArrayList<App> getAppList() {
    try {
      RobotDescription robot = getCurrentRobot();
      AppManager app_man = new AppManager( getNode(), robot.robot_name_ );
      return app_man.getAvailableApps();
    }
    catch( AppManagerNotAvailableException ex )
    {
      setStatus("AppManager not available");
      return null;
    }
    catch( RosInitException ex )
    {
      setStatus("Ros init exception: " + ex.getMessage());
      return null;
    }
  }

  private void setStatus( String status_message ) {
    TextView status_view = (TextView) findViewById(R.id.status_view);
    if( status_view != null )
    {
      status_view.setText( status_message );
    }
  }
}
