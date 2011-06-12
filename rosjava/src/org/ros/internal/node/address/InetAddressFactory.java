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

package org.ros.internal.node.address;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class InetAddressFactory {

  /**
   * Ensures that if an IP address string is specified for the host we use that
   * in place of a host name.
   * 
   * @param host
   * @return an {@link InetAddress} with both an IP and a host set (no further
   *         resolving will take place)
   */
  public static InetAddress createFromHostString(String host) {
    InetAddress address;
    try {
      if (InetAddresses.isInetAddress(host)) {
        address = InetAddress.getByAddress(host, InetAddresses.forString(host).getAddress());
      } else {
        address = InetAddress.getByName(host);
      }
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
    return address;
  }

  public static InetAddress createLoopback() {
    return createFromHostString(Address.LOOPBACK);
  }

  public static InetAddress createNonLoopback() {
    try {
      String address = null;
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      for (Enumeration<NetworkInterface> e = networkInterfaces; e.hasMoreElements();) {
        NetworkInterface networkInterface = e.nextElement();
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        for (Enumeration<InetAddress> eAddresses = inetAddresses; eAddresses.hasMoreElements();) {
          InetAddress inetAddress = eAddresses.nextElement();
          // IPv4 only for now.
          if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
            return inetAddress;
          }
        }
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
    throw new RuntimeException("No non-loopback interface found.");
  }
  
  public static InetAddress copyHostAddressToHostName(InetAddress address) {
    return createFromHostString(address.getHostAddress());
  }

  private InetAddressFactory() {
    // Utility class
  }

}
