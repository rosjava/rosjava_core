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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.util.Log;

import java.util.ArrayList;

import org.ros.message.app_manager.App;
import org.ros.message.app_manager.ClientApp;
import org.ros.message.app_manager.KeyValue;

public class AppLauncher {
  static private final String CLIENT_TYPE = "android";
  static public final String PACKAGE = "org.ros.android.app_chooser";

  /** Launch a client app for the given robot app. */
  static public void launch( Activity parent_activity, App app ) {
    ArrayList<ClientAppData> android_apps = new ArrayList<ClientAppData>();

    if( app.client_apps.size() == 0 )
    {
      launchStubApp( parent_activity, app );
      return;
    }

    Log.i( "RosAndroid", "launching robot app " + app.name + ".  Found " + app.client_apps.size() + " client apps." );

    // Loop over all possible client apps to find the android ones.
    for( int i = 0; i < app.client_apps.size(); i++ )
    {
      ClientApp client_app = app.client_apps.get( i );
      if( client_app.client_type != null && client_app.client_type.equals( CLIENT_TYPE ))
      {
        android_apps.add( new ClientAppData( client_app ));
      }
    }

    Log.i( "RosAndroid", "launching robot app " + app.name + ".  Found " + android_apps.size() + " android apps." );

    // TODO: filter out android apps which are not appropriate for
    // this device by looking at specific entries in the manager_data_
    // map of each app in android_apps.
    ArrayList<ClientAppData> appropriate_android_apps = android_apps;

    // Loop over all android apps, trying to launch one.
    for( int i = 0; i < appropriate_android_apps.size(); i++ )
    {
      ClientAppData app_data = appropriate_android_apps.get( i );
      Intent intent = app_data.createIntent();
      try
      {
        Log.i( "RosAndroid",
               "trying to startActivity( action: " + intent.getAction() + " )" );
        parent_activity.startActivity( intent );
        return;
      }
      catch (ActivityNotFoundException e)
      {
        Log.i( "RosAndroid",
               "activity not found for action: " + intent.getAction() );
      }
    }

    Log.i( "RosAndroid", "showing not-installed dialog." );

    // TODO:
    // Loop over all android apps, trying to install one. (??)
    // For now, just show a failure dialog.
    AlertDialog.Builder dialog = new AlertDialog.Builder( parent_activity );
    dialog.setTitle( "Android app not installed." );
    dialog.setMessage( "This robot app requires a client user interface app, but none of the applicable android apps are installed." );
    dialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
      public void onClick( DialogInterface dlog, int i) {
        dlog.dismiss();
      }
    });
  }

  /** Launch the "stub" app  */
  static public void launchStubApp( Activity parent_activity, App robot_app ) {
    Intent intent = new Intent( parent_activity, StubAppActivity.class );
    intent.putExtra( PACKAGE + ".robot_app_display_name", robot_app.display_name );
    intent.putExtra( PACKAGE + ".robot_app_name", robot_app.name );
    try
    {
      parent_activity.startActivity( intent );
      return;
    }
    catch (ActivityNotFoundException e)
    {
      AlertDialog.Builder dialog = new AlertDialog.Builder( parent_activity );
      dialog.setTitle( "Android ROS stub app not installed." );
      dialog.setMessage( "This robot app requires the stub client user interface app, but it is missing." );
      dialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
          public void onClick( DialogInterface dlog, int i) {
            dlog.dismiss();
          }
        });
    }
  }
}
