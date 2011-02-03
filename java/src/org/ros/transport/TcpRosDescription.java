package org.ros.transport;

import java.net.InetSocketAddress;

public class TcpRosDescription extends ProtocolDescription {

  public TcpRosDescription(InetSocketAddress address) {
    super("TCPROS", address);
  }

}
