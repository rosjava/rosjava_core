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

package ros.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import org.ros.NodeContext;
import org.ros.RosLoader;
import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;

import ros.android.activity.MasterChooserActivity;

/** Helper class for launching the MasterChooserActivity for choosing a ROS master.
 * Keep this object around for the lifetime of an Activity.
 */
public class MasterChooser extends RosLoader {

  private Activity calling_activity_;
  private String master_uri_;

  /** REQUEST_CODE number must be unique among activity requests which
   * might be seen by handleActivityResult(). */
  private static final int REQUEST_CODE = 8748792;

  private static final String MASTER_URI_PREFS = "MASTER_URI_PREFS";
  private static final String MASTER_URI = "MASTER_URI";

  public MasterChooser( Activity calling_activity ) {
    calling_activity_ = calling_activity;
    master_uri_ = null;
  }

  public boolean haveMaster() {
    return( master_uri_ != null && master_uri_.length() != 0 );
  }

  /** Call this from your activity's onActivityResult() to record the
   * master URI.
   * @returns true if the activity result came from the activity
   *          started by this class, false otherwise. */
  public boolean handleActivityResult( int requestCode, int resultCode, Intent result_intent ) {
    if( requestCode != REQUEST_CODE )
      return false;

    if( resultCode == Activity.RESULT_OK )
    {
      master_uri_ = result_intent.getStringExtra( MasterChooserActivity.MASTER_URI_EXTRA );
    }
    return true;
  }

  public void launchChooserActivity() throws ActivityNotFoundException {
    Log.i( "RosAndroid", "starting master chooser activity" );
    Intent chooser_intent = new Intent( calling_activity_, MasterChooserActivity.class );
    calling_activity_.startActivityForResult( chooser_intent, REQUEST_CODE );
  }

  @Override
  public NodeContext createContext() throws RosInitException {
    if( master_uri_ == null) {
      throw new RosInitException("ROS Master URI is not set");
    }
    String namespace = Namespace.GLOBAL_NS;
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    NameResolver resolver = new NameResolver(namespace, remappings);

    NodeContext context = new NodeContext();
    context.setParentResolver(resolver);
    context.setRosRoot("fixme");
    context.setRosPackagePath(null);
    try {
      context.setRosMasterUri( new URI( master_uri_ ));
    } catch( URISyntaxException ex ) {
      throw new RosInitException( "ROS Master URI (" + master_uri_ + ") is invalid: " + ex.getMessage() );
    }

    String hostName = getHostName();
    if (hostName != null) {
      context.setHostName(hostName);
    } else {
      throw new RosInitException( "Could not get a hostname for this device.");
    }
    return context;
  }

  /**
   * @return The url of the local host. IPv4 only for now.
   */
  private String getHostName() {
    try {
      String address = null;
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          Log.i("RosAndroid", "Address: " + inetAddress.getHostAddress().toString());
          // IPv4 only for now
          if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
            if (address == null)
              address = inetAddress.getHostAddress().toString();

          }
        }
      }
      if (address != null)
        return address;
    } catch (SocketException ex) {
      Log.i("RosAndroid", "SocketException: " + ex.getMessage());
    }
    return null;
  }
}
