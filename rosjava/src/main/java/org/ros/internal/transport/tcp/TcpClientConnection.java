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

import com.google.common.base.Preconditions;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.net.SocketAddress;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnection {

  private final ClientBootstrap bootstrap;
  private final SocketAddress remoteAddress;

  /**
   * {@code true} if this client connection should reconnect when disconnected.
   */
  private boolean persistent;

  /**
   * This connection's {@link Channel}. May be {@code null} if we're not
   * currently connected.
   */
  private Channel channel;

  /**
   * @param bootstrap
   *          the {@link ClientBootstrap} instance to use when reconnecting
   * @param remoteAddress
   *          the {@link SocketAddress} to reconnect to
   */
  TcpClientConnection(ClientBootstrap bootstrap, SocketAddress remoteAddress) {
    this.bootstrap = bootstrap;
    this.remoteAddress = remoteAddress;
    persistent = true;
  }

  /**
   * @return the {@link ClientBootstrap} instance used to reconnect
   */
  public ClientBootstrap getBootstrap() {
    return bootstrap;
  }

  /**
   * @return the {@link SocketAddress} to reconnect to
   */
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * @param persistent
   *          {@code true} if this client connection should reconnect when
   *          disconnected
   */
  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }

  /**
   * @return {@code true} if this client connection should reconnect when
   *         disconnected
   */
  public boolean isPersistent() {
    return persistent;
  }

  /**
   * @see {@link Channel#write(ChannelBuffer)}
   */
  public ChannelFuture write(ChannelBuffer buffer) {
    Preconditions.checkNotNull(channel, "Not connected.");
    return channel.write(buffer);
  }

  /**
   * @param channel
   *          this connection's {@link Channel} or {@code null} if we're not
   *          currently connected
   */
  void setChannel(Channel channel) {
    this.channel = channel;
  }
}
