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
public class NodeSocketAddress extends InetSocketAddress {

  private final String publicHostname;

  public NodeSocketAddress(InetSocketAddress address, String publicHostname) {
    super(address.getAddress(), address.getPort());
    this.publicHostname = publicHostname;
  }

  public String getPublicHostname() {
    return publicHostname;
  }

  /**
   * Create a {@link NodeSocketAddress} with the specified port and the public
   * hostname set to the default (canonical host name).
   * 
   * @param port
   * @return {@link NodeSocketAddress} instance with specified port and default public hostname.
   * @throws UnknownHostException 
   */
  public static NodeSocketAddress createDefault(int port) throws UnknownHostException {
    String canonicalHostname = InetAddress.getLocalHost().getCanonicalHostName();
    NodeSocketAddress address = new NodeSocketAddress(new InetSocketAddress(port),
        canonicalHostname);
    return address;
  }
  
}
