package org.ros.actionlib.server;

import org.ros.message.Message;

/**
 * Callbacks to the {@link SimpleActionServer}.
 */
public interface SimpleActionServerCallbacks<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {
  /**
   * A goal has been received by the server.
   * 
   * @param actionServer
   *          The server which received the goal.
   */
  void
  goalCallback(
      SimpleActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer);

  /**
   * A request to preempt a goal has been received by the server.
   * 
   * @param actionServer
   *          The server which received the request.
   */
  void
  preemptCallback(
      SimpleActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer);

  /**
   * 
   * @param goal
   *          The goal to blocked.
   * @param actionServer
   *          The server which received the goal.
   */
  void
  blockingGoalCallback(
      T_GOAL goal,
      SimpleActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer);
}
