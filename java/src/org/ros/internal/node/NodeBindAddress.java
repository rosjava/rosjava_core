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
package org.ros.internal.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Wrapper for {@link InetSocketAddress} that also enables setting of a separate
 * public hostname override. This enables APIs to distinguish between an address
 * that a resource is bound to versus how that address should be reported to
 * external entities.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeBindAddress {

  private final String publicHostname;
  private InetSocketAddress address;

  public NodeBindAddress(InetSocketAddress address, String publicHostname) {
    this.address = new InetSocketAddress(address.getAddress(), address.getPort());
    this.publicHostname = publicHostname;
  }

  public String getPublicHostName() {
    return publicHostname;
  }

  /**
   * Create a {@link NodeBindAddress} with the specified port and the public
   * hostname set to the default (canonical host name).
   * 
   * @param port
   * @return {@link NodeBindAddress} instance with specified port and default
   *         public hostname.
   * @throws UnknownHostException
   */
  public static NodeBindAddress createDefault(int port) throws UnknownHostException {
    String canonicalHostname = InetAddress.getLocalHost().getCanonicalHostName();
    NodeBindAddress address = new NodeBindAddress(new InetSocketAddress(port), canonicalHostname);
    return address;
  }

  @Override
  public String toString() {
    return "NodeSocketAddress<" + publicHostname + "<" + address.getHostName() + ">:"
        + address.getPort() + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((publicHostname == null) ? 0 : publicHostname.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NodeBindAddress other = (NodeBindAddress) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (publicHostname == null) {
      if (other.publicHostname != null)
        return false;
    } else if (!publicHostname.equals(other.publicHostname))
      return false;
    return true;
  }

  public int getPort() {
    return address.getPort();
  }

  public InetSocketAddress getBindAddress() {
    return address;
  }

  public InetSocketAddress getPublicAddress() {
    return new InetSocketAddress(publicHostname, address.getPort());
  }
}
