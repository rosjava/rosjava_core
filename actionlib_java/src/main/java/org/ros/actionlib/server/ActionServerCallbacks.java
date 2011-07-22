package org.ros.actionlib.server;

import org.ros.message.Message;

/**
 * Callbacks for the action server.
 */
public interface ActionServerCallbacks<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  /**
   * A goal has been submitted to the server.
   * 
   * @param goal
   *          The goal to be done.
   */
  void
  goalCallback(
      ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goal);

  /**
   * Cancel a goal.
   * 
   * @param goalToCancel
   *          The goal to cancel.
   */
  void
  cancelCallback(
      ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalToCancel);
}
