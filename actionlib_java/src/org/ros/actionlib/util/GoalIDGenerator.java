package org.ros.actionlib.util;

import org.ros.message.Time;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.node.Node;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The GoalIDGenerator may be used to create unique GoalIDs.
 * 
 * <p>
 * The node's nodeName will be used and the time on the node.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 */
public class GoalIDGenerator {
  /**
   * A global ID which provide a count for each goal id.
   */
  private static AtomicLong goalCount = new AtomicLong(0);

  /**
   * Unique nodeName to prepend to the goal id. This will generally be a fully
   * qualified node nodeName.
   */
  private Node node;

  /**
   * Constructor to create a GoalIDGenerator using a unique nodeName to prepend to
   * the goal id. This will generally be a fully qualified node nodeName.
   * 
   * @param node
   *          The node used to generate IDs. The node's full nodeName should be
   *          unique in the system.
   */
  public GoalIDGenerator(Node node) {
    this.node = node;
  }

  /**
   * Creates a GoalID object with an unique id and a timestamp of the current
   * time.
   * 
   * @return GoalID object
   */
  public GoalID generateID() {

    Time t = node.getCurrentTime();
    GoalID id = new GoalID();

    StringBuilder sb = new StringBuilder(node.getName());
    sb.append("-").append(goalCount.incrementAndGet()).append("-").append(t.secs).append(".")
        .append(t.nsecs);

    id.id = sb.toString();
    id.stamp = t;

    return id;
  }
}
