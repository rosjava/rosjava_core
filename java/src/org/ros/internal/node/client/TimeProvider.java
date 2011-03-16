package org.ros.internal.node.client;

import org.ros.message.Time;

public interface TimeProvider {

  /**
   * @return The current time of the system, using rostime.
   */
  public abstract Time currentTime();

}