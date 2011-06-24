package org.ros.actionlib.client;

import org.ros.actionlib.state.CommState;
import org.ros.actionlib.state.TerminalState;
import org.ros.exception.RosException;
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.message.actionlib_msgs.GoalStatus;

/**
 * A GoalHandle is linked with a specific goal message and provides means to
 * check the current state of progress and to re-send or cancel the goal. If a
 * goal shall no longer be tracked, i.e. no further status, feedback and result
 * messages shall be processed, its GoalHandle has to be {@link #shutdown()}.
 * Every GoalHandle has its own CommStateMachine that monitors the communication
 * between the action client that sent out the goal and the corresponding action
 * server.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @param <T_ACTION_FEEDBACK>
 *          action feedback message
 * @param <T_ACTION_GOAL>
 *          action goal message
 * @param <T_ACTION_RESULT>
 *          action result message
 * @param <T_FEEDBACK>
 *          feedback message
 * @param <T_GOAL>
 *          goal message
 * @param <T_RESULT>
 *          result message
 */
public class ClientGoalHandle<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {
  /**
   * Client this goal is associated with.
   */
  protected ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionClient;

  /**
   * A Flag indicating whether the GoalHandle is active or not
   */
  protected boolean active;

  /**
   * The GoalManager, which created this GoalHandle
   */
  protected GoalManager<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalManager;

  /**
   * The GoalHandle's CommStateMachine, which monitors the communication between
   * action client and action server
   */
  protected CommStateMachine<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> commStateMachine;

  /**
   * Constructor to create a GoalHandle using a specific GoalManager and
   * CommStateMachine.
   * 
   * @param gm
   *          The GoalManager used to manage sending or canceling goal messages
   * @param stateMachine
   *          The CommStateMachine used to monitor the communication between
   *          action client and action server
   */
  public ClientGoalHandle(
      GoalManager<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> gm,
      ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionClient,
      CommStateMachine<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> stateMachine) {
    this.actionClient = actionClient;
    commStateMachine = stateMachine;
    this.goalManager = gm;
    active = true;
  }

  /**
   * Shuts down the GoalHandle and changes the GoalHandle's status to inactive. 
   * Further status, feedback or result messages regarding this GoalHandle's
   * goal will not be received anymore.
   * 
   * <p>Removes this GoalHandle from the GoalManagers list of active GoalHandles if requested.
   */
  public void shutdown(boolean deleteFromManager) {
    active = false;
    
    if (deleteFromManager)
      goalManager.deleteGoalHandle(this);
  }

  /**
   * Checks whether this GoalHandle was shutdown or not.
   * 
   * @return <tt>true</tt> - if this GoalHandle is not active anymore<br>
   *         <tt>false</tt> - otherwise
   */
  public boolean isExpired() {
    return !active;
  }

  /**
   * Gets the GoalHandle's CommStateMachine.
   * 
   * @return The CommStateMachine that monitors the communication between the
   *         action client, that sent out the goal message associated with this
   *         GoalHandle, and the action server.
   */
  public
      CommStateMachine<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>
      getStateMachine() {
    return commStateMachine;
  }

  /**
   * Gets the current communication state from the CommStateMachine.
   * 
   * @return A CommState object holding the current state.
   */
  public CommState getCommState() {

    if (!active) {
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Trying to getCommState() on an inactive ClientGoalHandle. You are incorrectly using a ClientGoalHandle.");
      return new CommState(CommState.StateEnum.DONE);
    }

    CommState commState = null;
    commState = commStateMachine.getCommState();

    return commState;

  }

  /**
   * Returns the terminal state of this GoalHandle. This method should only be
   * invoked when the GoalHandle's CommStateMachine is in state 'DONE'.<br>
   * Possible terminal states are:
   * <ul>
   * <li>RECALLED</li>
   * <li>REJECTED</li>
   * <li>PREEMPTED</li>
   * <li>SUCCEEDED</li>
   * <li>ABORTED</li>
   * <li>LOST</li>
   * </ul>
   * 
   * @return The terminal state of this GoalHandle. If the CommStateMachine is
   *         not in state 'DONE' the terminal state 'LOST' will be returned.
   */
  public TerminalState getTerminalState() {

    if (!active) {
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Trying to getTerminalState() on an inactive ClientGoalHandle. You are incorrectly using a ClientGoalHandle.");
      return new TerminalState(TerminalState.StateEnum.LOST);
    }

    CommState commState = null;
    commState = commStateMachine.getCommState();

    if (!commState.equals(CommState.StateEnum.DONE)) {
      actionClient
          .getNode()
          .getLog()
          .warn(
              "[ClientGoalHandle] Asking for terminal state when we're in state '" + commState
                  + "'");
    }

    GoalStatus goalStatus = commStateMachine.getGoalStatus();

    switch (goalStatus.status) {
    case GoalStatus.PENDING:
    case GoalStatus.ACTIVE:
    case GoalStatus.PREEMPTING:
    case GoalStatus.RECALLING:
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Asking for terminal state, but latest goal status is '"
                  + goalStatus.status + "'");
      return new TerminalState(TerminalState.StateEnum.LOST, goalStatus.text);
    case GoalStatus.PREEMPTED:
      return new TerminalState(TerminalState.StateEnum.PREEMPTED, goalStatus.text);
    case GoalStatus.SUCCEEDED:
      return new TerminalState(TerminalState.StateEnum.SUCCEEDED, goalStatus.text);
    case GoalStatus.ABORTED:
      return new TerminalState(TerminalState.StateEnum.ABORTED, goalStatus.text);
    case GoalStatus.REJECTED:
      return new TerminalState(TerminalState.StateEnum.REJECTED, goalStatus.text);
    case GoalStatus.RECALLED:
      return new TerminalState(TerminalState.StateEnum.RECALLED, goalStatus.text);
    case GoalStatus.LOST:
      return new TerminalState(TerminalState.StateEnum.LOST, goalStatus.text);
    default:
      actionClient.getNode().getLog()
          .error("[ClientGoalHandle] Unknown goal status '" + goalStatus.status + "'");
      break;
    }

    actionClient.getNode().getLog().error("[ClientGoalHandle] Bug in determining terminal state");
    return new TerminalState(TerminalState.StateEnum.LOST, goalStatus.text);

  }

  /**
   * Gets the result message received from the action server.
   * 
   * @return
   */
  public T_RESULT getResult() throws RosException {

    if (!active) {
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Trying to getResult() on an inactive ClientGoalHandle. You are incorrectly using a ClientGoalHandle.");
    }

    return commStateMachine.getResult();
  }

  /**
   * Re-sends the GoalHandle's goal message. This can be necessary when the
   * action server didn't receive the goal message which was sent out before.
   */
  public void resend() {

    if (!active) {
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Trying to resend() on an inactive ClientGoalHandle. You are incorrectly using a ClientGoalHandle.");
    }

    T_ACTION_GOAL actionGoal = commStateMachine.getActionGoal();
    if (actionGoal == null) {
      actionClient.getNode().getLog().error("[ClientGoalHandle] Got a NULL action goal");
    } else {
      goalManager.sendActionGoal(actionGoal);
    }

  }

  /**
   * Cancels this GoalHandle's goal by sending a cancel message to the action
   * server.
   */
  public void cancel() throws RosException {
    if (!active) {
      actionClient
          .getNode()
          .getLog()
          .error(
              "[ClientGoalHandle] Trying to cancel() on an inactive ClientGoalHandle. You are incorrectly using a ClientGoalHandle.");
    }

    T_ACTION_GOAL actionGoal = commStateMachine.getActionGoal();
    String id = goalManager.actionClient.spec.getGoalIDFromActionGoal(actionGoal).id;

    GoalID cancelMessage = new GoalID();
    cancelMessage.stamp = new Time(0, 0);
    cancelMessage.id = id;

    goalManager.sendCancelGoal(cancelMessage);
    commStateMachine.transitionToState(CommState.StateEnum.WAITING_FOR_CANCEL_ACK, this);

  }

}
