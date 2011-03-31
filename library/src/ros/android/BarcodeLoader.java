package ros.android;

import android.app.PendingIntent;

import android.content.Intent;

import android.app.Notification;

import android.content.Context;

import android.app.NotificationManager;

import android.app.Activity;
import android.util.Log;
import org.ros.NodeContext;
import org.ros.RosLoader;
import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import ros.android.utils.master.MasterChooser;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;

public class BarcodeLoader extends RosLoader {

  private String masterUri;
  private static final int ROS_MASTER_TICKER_CHOOSER = 1;

  public void makeNotification(Activity activity) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(ns);
    int icon = R.drawable.status;
    CharSequence ticker = "ROS Master Selector";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon, ticker, when);

    Context context = activity.getApplicationContext();
    CharSequence contentTitle = "Chose A Master";
    CharSequence contentText = "Scan a barcode to pair with a robot.";
    Intent notificationIntent = new Intent(activity, MasterChooserActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, notificationIntent, 0);
    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    mNotificationManager.notify(ROS_MASTER_TICKER_CHOOSER, notification);
  }

  public BarcodeLoader(Activity activity, int requestCode) {
    masterUri = MasterChooser.getCachedURI(activity);
    if (masterUri == null) {
      MasterChooser.launchUriIntent(activity, requestCode);
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
  public String getHostName() {
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
      // log.error(ex);
    }
    throw new RuntimeException("Could not find a network address for the local host!");
  }

}
