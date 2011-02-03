package org.ros.transport;

import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.collect.Lists;

public class ProtocolDescription {

  private final String name;
  private final InetSocketAddress address;
  
  public ProtocolDescription(String name, InetSocketAddress address) {
    this.name = name;
    this.address = address;
  }

  public String getName() {
    return name;
  }

  public InetSocketAddress getAddress() {
    return address;
  }

  public List<Object> toList() {
    // TODO(damonkohler): Why is the cast necessary?
    return Lists.newArrayList((Object) name, address.getHostName(), address.getPort());
  }
}
