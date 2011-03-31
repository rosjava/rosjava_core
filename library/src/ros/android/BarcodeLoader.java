package ros.android;

import android.util.Log;

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
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;

public class BarcodeLoader extends RosLoader {

  @Override
  public NodeContext createContext() throws RosInitException {
    String namespace = Namespace.GLOBAL_NS;
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    NameResolver resolver = new NameResolver(namespace, remappings);

    NodeContext context = new NodeContext();
    context.setParentResolver(resolver);
    context.setRosRoot("fixme");
    context.setRosPackagePath(null);
    try {
      context.setRosMasterUri(new URI("http://10.0.129.60:11311"));
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
    }

    String addressOverride = getAddressOverride();
    if (addressOverride != null) {
      context.setHostName(addressOverride);
    } else {
      throw new RosInitException("No address override.");
    }
    return context;
  }

  /**
   * @return The url of the local host. IPv4 only for now.
   */
  public String getAddressOverride() {
    try {
      String address = null;
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          Log.i("RosAndroid","Address: " + inetAddress.getHostAddress().toString());
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
