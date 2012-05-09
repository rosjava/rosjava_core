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

package org.ros.internal.transport.tcp;

import com.google.common.collect.Lists;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnectionManager {

  private final ChannelGroup channelGroup;
  private final TcpClientConnectionFactory tcpClientConnectionFactory;
  private final Collection<TcpClientConnection> tcpClientConnections;

  public TcpClientConnectionManager(String channelHandlerName, ChannelHandler channelHandler,
      ScheduledExecutorService scheduledExecutorService) {
    channelGroup = new DefaultChannelGroup();
    tcpClientConnectionFactory =
        new TcpClientConnectionFactory(channelHandlerName, channelHandler, channelGroup,
            scheduledExecutorService);
    tcpClientConnections = Lists.newArrayList();
  }

  /**
   * Connects to a server.
   *
   * <p>
   * This call blocks until the connection is established or fails.
   *
   * @param connectionName
   * @param address
   * @param handler
   * @param handlerName
   * @return a new {@link TcpClientConnection}
   */
  public TcpClientConnection connect(String connectionName, SocketAddress socketAddress) {
    TcpClientConnection tcpClientConnection =
        tcpClientConnectionFactory.connect(connectionName, socketAddress);
     tcpClientConnections.add(tcpClientConnection);
     return tcpClientConnection;
   }

  /**
   * Sets all {@link TcpClientConnection}s as non-persistent and closes all open
   * {@link Channel}s.
   */
  public void shutdown() {
    for (TcpClientConnection tcpClientConnection : tcpClientConnections) {
      tcpClientConnection.setPersistent(false);
      tcpClientConnection.setChannel(null);
    }
    channelGroup.close().awaitUninterruptibly();
    tcpClientConnections.clear();
    // Not calling channelFactory.releaseExternalResources() or
    // bootstrap.releaseExternalResources() since only external resources are
    // the ExecutorService and control of that must remain with the overall
    // application.
  }
}
