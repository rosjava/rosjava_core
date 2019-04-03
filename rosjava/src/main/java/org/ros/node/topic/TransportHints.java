package org.ros.node.topic;

import java.util.Map;

import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.node.ConnectedNode;

import com.google.common.collect.Maps;


/**
 * Provides a way of specifying network transport hints to
 * {@link ConnectedNode#newSubscriber(String, String)} and
 * {@link ConnectedNode#newSubscriber(org.ros.namespace.GraphName, String)}.
 *
 * @author stefan.glaser@hs-offenburg.de (Stefan Glaser)
 */
public class TransportHints {

  Map<String, String> options;

 public TransportHints() {
   this.options = Maps.newConcurrentMap();
  }

  public TransportHints(boolean tcpNoDelay) {
    tcpNoDelay(tcpNoDelay);
  }

  public TransportHints tcpNoDelay(boolean nodelay) {
    options.put(ConnectionHeaderFields.TCP_NODELAY, nodelay ? "1" : "0");

    return this;
  }

  public boolean getTcpNoDelay() {
    return "1".equals(options.get(ConnectionHeaderFields.TCP_NODELAY));
  }

  public Map<String, String> getOptions() {
    return options;
  }
}
