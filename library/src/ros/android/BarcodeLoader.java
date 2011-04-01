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

package ros.android;

import android.widget.Toast;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.ros.NodeContext;
import org.ros.RosLoader;
import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import ros.android.activity.MasterChooserActivity;
import ros.android.util.MasterChooser;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class BarcodeLoader extends RosLoader {

  private String masterUri;
  private static final int ROS_MASTER_TICKER_CHOOSER = 1;
  private static final CharSequence TICKER_TITLE = "ROS";
  private static final CharSequence TICKER_CONTENT_TITLE = "Select a robot";
  private static final CharSequence TICKER_CONTENT_TEXT = "Scan a barcode to talk to robot.";

  /**
   * This will create notification that will allow easy switch of ros master uri
   * from a 2d barcode scanning app.
   * 
   * You should call this from your main activity, if you wish to be able to
   * update the cached uri.
   * 
   * @param activity
   */
  public void makeNotification(Activity activity) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(ns);
    int icon = R.drawable.status;
    CharSequence ticker = TICKER_TITLE;
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon, ticker, when);
    notification.flags |= Notification.FLAG_ONGOING_EVENT;
    
    Context context = activity.getApplicationContext();
    CharSequence contentTitle = TICKER_CONTENT_TITLE;
    CharSequence contentText = TICKER_CONTENT_TEXT;
    Intent notificationIntent = new Intent(activity, MasterChooserActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, notificationIntent, 0);
    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    mNotificationManager.notify(ROS_MASTER_TICKER_CHOOSER, notification);
  }

  /**
   * Tries to bring up the cached master uri, or else will launch a barcode
   * scanning intent.
   * 
   * @param activity
   * @param requestCode
   */
  public BarcodeLoader(Activity activity) {
    masterUri = MasterChooser.getCachedURI(activity);
    if (masterUri == null) {
      //MasterChooser.launchUriIntent(activity);
      Toast.makeText(activity, activity.getString(R.string.uri_not_set_toast), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public NodeContext createContext() throws RosInitException {
    if (masterUri == null) {
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
      context.setRosMasterUri(new URI(masterUri));
    } catch (URISyntaxException e1) {
      throw new RosInitException("ROS Master URI is not configured properly");
    }

    String hostName = getHostName();
    if (hostName != null) {
      context.setHostName(hostName);
    } else {
      throw new RosInitException("No address override.");
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
    throw new RuntimeException("Could not find a network address for the local host!");
  }

}
