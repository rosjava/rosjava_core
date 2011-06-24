package org.ros.actionlib.server;

import org.ros.exception.RosException;
import org.ros.message.Message;

/**
 * An action server which simplifies much of the complexity of a full
 * {@link DefaultActionServer}.
 * 
 * @author Keith M. Hughes
 * @since Jun 16, 2011
 */
public interface SimpleActionServer<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  /**
   * The server accepts a new goal from the client.
   * 
   * <p>
   * Any current goal is canceled.
   * 
   * @return The new goal.
   * @throws RosException
   */
  T_GOAL acceptNewGoal() throws RosException;

  boolean isNewGoalAvailable();

  boolean isPreemptRequested();

  boolean isActive();

  void setSucceeded();

  void setSucceeded(T_RESULT result, String text);

  void setAborted();

  void setAborted(T_RESULT result, String text);

  void setPreempted();

  void setPreempted(T_RESULT result, String text);

  /**
   * Publish feedback about the goal in process.
   * 
   * @param feedback
   *          The specific feedback to publish.
   */
  void publishFeedback(T_FEEDBACK feedback);
}