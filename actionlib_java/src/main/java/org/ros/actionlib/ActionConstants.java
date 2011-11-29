/**
 * 
 */
package org.ros.actionlib;

/**
 * Constants used by the actionlib clients and servers.
 * 
 * @author Keith M. Hughes
 */
public class ActionConstants {
  
  /**
   * The topic name for feedback.
   */
  public static final String TOPIC_NAME_FEEDBACK = "feedback";

  /**
   * The topic name for results.
   */
  public static final String TOPIC_NAME_RESULT = "result";

  /**
   * The topic name for status.
   */
  public static final String TOPIC_NAME_STATUS = "status";

  /**
   * The topic name for canceling.
   */
  public static final String TOPIC_NAME_CANCEL = "cancel";

  /**
   * The topic name for setting a goal.
   */
  public static final String TOPIC_NAME_GOAL = "goal";

  /**
   * The message type for status.
   */
  public static final String MESSAGE_TYPE_STATUS = "actionlib_msgs/GoalStatusArray";

  /**
   * The message type for goal cancellations.
   */
  public static final String MESSAGE_TYPE_CANCEL = "actionlib_msgs/GoalID";
}
