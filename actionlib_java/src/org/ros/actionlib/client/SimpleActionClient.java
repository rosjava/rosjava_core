package org.ros.actionlib.client;

import org.ros.Node;
import org.ros.NodeConfiguration;
import org.ros.NodeMain;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.state.CommState;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.actionlib.state.SimpleGoalState;
import org.ros.actionlib.state.TerminalState;
import org.ros.exception.RosException;
import org.ros.message.Duration;
import org.ros.message.Message;
import org.ros.message.Time;

/**
 * A SimpleActionClient is a wrapper around a regular {@link ActionClient} that
 * hides the more complex mechanisms from the user. It allows to pursue exactly
 * one goal. If a new goal is sent out before the current one was finalized, the
 * current goal does not get canceled but tracking of this goal is stopped. If
 * the current goal shall be canceled, one of the cancelGoal-methods have to be
 * called.
 * 
 * <p>
 * This client must be started by using the {@link #} method.
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
public class SimpleActionClient<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message>
    implements
    ActionClientCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>,
    NodeMain {

  /**
   * The ActionClient used to communicate with the action server
   */
  protected ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionClient;

  /**
   * User code callbacks that are called when the SimpleActionClient receives
   * <ul>
   * <li>an acknowledgment from the server that a goal is being worked on</li>
   * <li>the final result from the server or another final state is reached</li>
   * <li>feedback messages</li>
   * </ul>
   */
  protected SimpleActionClientCallbacks<T_FEEDBACK, T_RESULT> callbacks;

  /**
   * The ClientGoalHandle that is tracking the current goal.
   */
  protected ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle;

  /**
   * Current simple goal state (one of PENDING, ACTIVE or DONE)
   */
  protected SimpleGoalState currSimpleState;

  /**
   * Monitor used to wait for the result and get notified as soon as it arrives.
   */
  private Object waitSync = new Object();

  /**
   * Constructor used to create a SimpleActionClient that communicates in a
   * given nodeName space on a given action specification. An extra thread that
   * services the callbacks may be demanded.
   * 
   * @param nameSpace
   *          The nodeName space to communicate within (specified by the action
   *          server)
   * @param spec
   *          The specification of the action the SimpleActionClient shall use
   * @throws RosException
   *           If setting up the needed subscribers and publishers fail
   */
  public SimpleActionClient(
      String nameSpace,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec)
      throws RosException {

    this.actionClient =
        new ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            nameSpace, spec);
    this.callbacks = null;
    this.goalHandle = null;
    this.currSimpleState = new SimpleGoalState(SimpleGoalState.StateEnum.PENDING);
  }

  /**
   * Constructor used to create a SimpleActionClient that will be a child node
   * of the node represented by the given node handle. It communicates in a
   * given nodeName space on a given action specification. An extra thread that
   * services the callbacks may be demanded.
   * 
   * @param parentNode
   *          The parent node of this action client
   * @param nameSpace
   *          The nodeName space to communicate within (specified by the action
   *          server)
   * @param spec
   *          The specification of the action the SimpleActionClient shall use
   * @throws RosException
   *           If setting up the needed subscribers and publishers fail
   */
  public SimpleActionClient(
      Node parentNode,
      String nameSpace,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec)
      throws RosException {

    this.actionClient =
        new ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            parentNode, nameSpace, spec);
    this.callbacks = null;
    this.goalHandle = null;
    this.currSimpleState = new SimpleGoalState(SimpleGoalState.StateEnum.PENDING);
  }

  @Override
  public void main(NodeConfiguration configuration) throws Exception {
    actionClient.main(configuration);
  }

  /**
   * Sends a goal message to the action server without demanding callback
   * methods which would enable the user to track the progress of the goal. If
   * there is another active goal currently being worked on by the server,
   * tracking of that goal stops without canceling it.
   * 
   * @param goal
   *          A goal message
   */
  public void sendGoal(T_GOAL goal) throws RosException {
    sendGoal(goal, null);
  }

  /**
   * Sends a goal message to the action server demanding callback methods which
   * enable the user to track the progress of the goal. If there is another
   * active goal currently being worked on by the server, tracking of that goal
   * stops without canceling it.
   * 
   * @param goal
   *          A goal message
   * @param callbacks
   *          The user's callback methods
   * 
   * @see SimpleActionClientCallbacks#activeCallback()
   * @see SimpleActionClientCallbacks#doneCallback(SimpleClientGoalState, Message)
   * @see SimpleActionClientCallbacks#feedbackCallback(Message)
   */
  public void sendGoal(T_GOAL goal, SimpleActionClientCallbacks<T_FEEDBACK, T_RESULT> callbacks)
      throws RosException {
    this.callbacks = callbacks;

    if (goalHandle != null) {
      goalHandle.shutdown(true);
    }

    currSimpleState.setState(SimpleGoalState.StateEnum.PENDING);
    goalHandle = actionClient.sendGoal(goal, this);
  }

  /**
   * Uses the {@linkplain #sendGoal(Message)} method to send out the given goal
   * and then waits for the result using the
   * {@linkplain #waitForResult(Duration)} method. If no result was received
   * within the specified duration the goal gets canceled and the
   * SimpleActionClient waits up to the specified duration for the action
   * server's acknowledgment.
   * 
   * @param goal
   *          A goal message
   * @param execTimeout
   *          The maximum duration that the SimpleActionClient is going to wait
   *          for a result before canceling the goal. A zero length duration
   *          results in unlimited waiting.
   * @param preemptTimeout
   *          The maximum duration that the SimpleActionClient is going to wait
   *          for the action server's acknowledgment after canceling the goal. A
   *          zero length duration results in unlimited waiting.
   * @return The latest goal state
   */
  public SimpleClientGoalState sendGoalAndWait(T_GOAL goal, Duration execTimeout,
      Duration preemptTimeout) throws RosException {
    return sendGoalAndWait(goal, execTimeout, preemptTimeout, null);
  }

  /**
   * Uses the {@linkplain #sendGoal(Message, SimpleActionClientCallbacks)}
   * method to send out the given goal and then waits for the result using the
   * {@linkplain #waitForResult(Duration)} method. If no result was received
   * within the specified duration the goal gets canceled and the
   * SimpleActionClient waits up to the specified duration for the action
   * server's acknowledgment. The demanded callback methods get called on
   * certain circumstances enabling the user to track the progress of the goal.
   * 
   * @param goal
   *          A goal message
   * @param execTimeout
   *          The maximum duration that the SimpleActionClient is going to wait
   *          for a result before canceling the goal. A zero length duration
   *          results in unlimited waiting.
   * @param preemptTimeout
   *          The maximum duration that the SimpleActionClient is going to wait
   *          for the action server's acknowledgment after canceling the goal. A
   *          zero length duration results in unlimited waiting.
   * @param callbacks
   *          The user's callback methods
   * @return The latest goal state
   * 
   * @see SimpleActionClientCallbacks#activeCallback()
   * @see SimpleActionClientCallbacks#doneCallback(SimpleClientGoalState, Message)
   * @see SimpleActionClientCallbacks#feedbackCallback(Message)
   */
  public SimpleClientGoalState sendGoalAndWait(T_GOAL goal, Duration execTimeout,
      Duration preemptTimeout, SimpleActionClientCallbacks<T_FEEDBACK, T_RESULT> callbacks)
      throws RosException {

    sendGoal(goal, callbacks);

    if (waitForResult(execTimeout)) {
      actionClient.getNode().getLog().debug(
          "[SimpleActionClient] Goal was achieved within specified execTimeout ["
          + (execTimeout.totalNsecs() / 1000000) + " ms]");
    } else {
      cancelGoal();

      actionClient.getNode().getLog().debug(
          "[SimpleActionClient] Goal wasn't achieved within specified execTimeout ["
          + (execTimeout.totalNsecs() / 1000000) + " ms]");
      if (waitForResult(preemptTimeout)) {
        actionClient.getNode().getLog().debug(
            "[SimpleActionClient] Preempt finished within specified preemptTimeout ["
            + (preemptTimeout.totalNsecs() / 1000000) + " ms]");
      } else {
        actionClient.getNode().getLog().debug(
            "[SimpleActionClient] Preempt didn't finish within specified preemptTimeout ["
            + (preemptTimeout.totalNsecs() / 1000000) + " ms]");
      }
    }

    return getState();
  }

  /**
   * Cancels the execution of the current goal.
   */
  public void cancelGoal() throws RosException {

    if (goalHandle == null || goalHandle.isExpired()) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Trying to cancelGoal() while no goal is active. You are incorrectly using SimpleActionClient!");
    } else {
      goalHandle.cancel();
    }

  }

  /**
   * Cancels the execution of all goals that were sent out with a timestamp
   * equal to or before the specified time.
   * 
   * @param time
   *          A point in time.
   */
  public void cancelGoalsAtAndBeforeTime(Time time) {
    actionClient.cancelGoalsAtAndBeforeTime(time);
  }

  /**
   * Cancels the execution of all goals.
   */
  public void cancelAllGoals() {
    actionClient.cancelAllGoals();
  }

  /**
   * Stops tracking of the current goal without canceling it.
   */
  public void stopTrackingGoal() {
    if (goalHandle == null || goalHandle.isExpired()) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Trying to stopTrackingGoal() while no goal is active. You are incorrectly using SimpleActionClient!");
    } else {
      goalHandle.shutdown(true);
    }
  }

  @Override
  public void shutdown() {
    // Shuts down the SimpleActionClient and stops tracking all goals without
    // canceling them. After a SimpleActionClient was shutdown, it cannot be
    // used anymore. In fact this methods shuts down the wrapped ActionClient.
    actionClient.shutdown();
  }

  /**
   * Gets the result associated with the current goal. In order to check if a
   * result message is available the goal state received from calling
   * {@link #getState()} should be
   * {@link SimpleClientGoalState.StateEnum#SUCCEEDED}.
   * 
   * @return The result message received from the action server or NULL if no
   *         result message was received.
   */
  public T_RESULT getResult() throws RosException {
    if (goalHandle == null || goalHandle.isExpired()) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Trying to getResult() while no goal is active. You are incorrectly using SimpleActionClient!");
      return null;
    } else {
      return goalHandle.getResult();
    }

  }

  /**
   * Gets the current goal state. Possible states are:
   * <ul>
   * <li>PENDING</li>
   * <li>ACTIVE</li>
   * <li>REJECTED</li>
   * <li>PREEMPTED</li>
   * <li>SUCCEEDED</li>
   * <li>ABORTED</li>
   * <li>LOST</li>
   * </ul>
   * 
   * @return The current goal state
   */
  public SimpleClientGoalState getState() {
    if (goalHandle == null || goalHandle.isExpired()) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Trying to getState() while no goal is active. You are incorrectly using SimpleActionClient!");
      return null;
    }

    SimpleClientGoalState state = new SimpleClientGoalState(SimpleClientGoalState.StateEnum.LOST);

    CommState commState = goalHandle.getCommState();
    switch (commState.getState()) {
    case WAITING_FOR_GOAL_ACK:
    case PENDING:
    case RECALLING:
      state.setState(SimpleClientGoalState.StateEnum.PENDING);
      break;
    case ACTIVE:
    case PREEMPTING:
      state.setState(SimpleClientGoalState.StateEnum.ACTIVE);
      break;
    case DONE: {

      TerminalState terminalState = goalHandle.getTerminalState();
      state.setText(terminalState.getText());
      switch (terminalState.getState()) {
      case RECALLED:
        state.setState(SimpleClientGoalState.StateEnum.RECALLED);
        break;
      case REJECTED:
        state.setState(SimpleClientGoalState.StateEnum.REJECTED);
        break;
      case PREEMPTED:
        state.setState(SimpleClientGoalState.StateEnum.PREEMPTED);
        break;
      case ABORTED:
        state.setState(SimpleClientGoalState.StateEnum.ABORTED);
        break;
      case SUCCEEDED:
        state.setState(SimpleClientGoalState.StateEnum.SUCCEEDED);
        break;
      case LOST:
        state.setState(SimpleClientGoalState.StateEnum.LOST);
        break;
      default:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Unknown TerminalState '" + terminalState
            + "'. This is a bug in SimpleActionClient!");
        break;
      }
      break;

    }
    case WAITING_FOR_RESULT:
    case WAITING_FOR_CANCEL_ACK: {

      switch (currSimpleState.getState()) {
      case PENDING:
        state.setState(SimpleClientGoalState.StateEnum.PENDING);
        break;
      case ACTIVE:
        state.setState(SimpleClientGoalState.StateEnum.ACTIVE);
        break;
      case DONE:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] In WAITING_FOR_RESULT or WAITING_FOR_CANCEL_ACK, yet we are in SimpleGoalState DONE. This is a bug in SimpleActionClient!");
        state.setState(SimpleClientGoalState.StateEnum.LOST);
        break;
      default:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Unknown SimpleGoalState '" + currSimpleState
            + "'. This is a bug in SimpleActionClient!");
        break;
      }
      break;

    }
    default:
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Error trying to interpret CommState - " + commState);
      break;
    }

    return state;

  }

  /**
   * Sets the simplified goal state. This is used internally in the
   * {@link #transitionCallback(ClientGoalHandle)} method. It is the
   * {@link #waitForResult()} method that relies on this reduced set of goal
   * states. The simplified states are:
   * <ul>
   * <li>PENDING</li>
   * <li>ACTIVE</li>
   * <li>DONE</li>
   * </ul>
   * 
   * @param nextState
   *          A new simplified goal state
   */
  protected void setSimpleState(SimpleGoalState.StateEnum nextState) {
    synchronized (waitSync) {
      currSimpleState.setState(nextState);
    }
  }

  // /**
  // * Starts a thread that services the callbacks. This method gets
  // * automatically called if the useSpinThread parameter is set to
  // * <tt>true</tt> when invoking one of the constructors. If there already is
  // * a spin thread running, this method does nothing.
  // *
  // * @param wallTimeInMS The minimum time between two calls of 'spinOnce()'
  // * in milliseconds
  // */
  // public void startSpinThread(long wallTimeInMS) {
  //
  // final long wallTime;
  // if (wallTimeInMS < 0) {
  // Ros.getInstance().logWarn("[SimpleActionClient] The spin rate for servicing the callbacks must be positive. Now using a rate of ~5Hz");
  // wallTime = 200;
  // } else {
  // wallTime = wallTimeInMS;
  // }
  //
  // synchronized (spinSync) {
  // if (spinThread == null) {
  // spinThread = new Thread() {
  //
  // private long wallTimeInNs = wallTime*1000000;
  //
  // @Override
  // public void run() {
  //
  // long startTime = Ros.getInstance().now().totalNsecs();
  // long endTime;
  //
  // while (actionClient!= null && actionClient.getNodeHandle().ok()) {
  //
  // if (terminateSpinThread) {
  // terminateSpinThread = false;
  // return;
  // }
  //
  // Ros.getInstance().spinOnce();
  //
  // endTime = Ros.getInstance().now().totalNsecs();
  // long sleepTime = (wallTimeInNs-endTime+startTime)/1000000L;
  // if (sleepTime > 0) {
  // try {
  // Thread.sleep(sleepTime);
  // } catch (InterruptedException e) {
  // }
  // startTime = endTime+sleepTime;
  // } else {
  // startTime = endTime;
  // }
  //
  // Ros.getInstance().logInfo("SleepTime: "+sleepTime);
  // }
  // }
  //
  // };
  // spinThread.start();
  // } else {
  // Ros.getInstance().logWarn("[SimpleActionClient] startSpinThread: spin thread is already running");
  // }
  // }
  //
  // }
  //
  // /**
  // * Stops the thread that services the callbacks. If the spin thread is not
  // * running, this method does nothing.
  // */
  // public void stopSpinThread() {
  //
  // synchronized (spinSync) {
  // if (spinThread != null) {
  // terminateSpinThread = true;
  // try {
  // spinThread.join();
  // } catch (InterruptedException e) {
  // e.printStackTrace();
  // }
  // spinThread = null;
  // } else {
  // Ros.getInstance().logWarn("[SimpleActionClient] stopSpinThread: spin thread is not running");
  // }
  // }
  //
  // }

  /**
   * Waits for the action server to start up.
   * 
   * @return <tt>true</tt> - if the SimpleActionClient could establish a
   *         connection to the action server<br>
   *         <tt>false</tt> - If the action client's node handle is not ok
   *         (normally, the method would block forever if no connection is
   *         established)
   */
  public boolean waitForServer() {
    return waitForServer(new Duration(0, 0));
  }

  /**
   * Waits up to the specified duration for the action server to start up. If no
   * connection could be established within the given time, the method returns
   * indicating the error.
   * 
   * @param timeout
   *          The maximum duration to wait for the action server to start up. A
   *          zero length duration results in unlimited waiting.
   * @return <tt>true</tt> - if the SimpleActionClient could establish a
   *         connection to the action server within the given time<br>
   *         <tt>false</tt> - otherwise
   */
  public boolean waitForServer(Duration timeout) {
    return actionClient.waitForActionServerToStart(timeout);
  }

  /**
   * Waits for the action server to successfully send the final result to this
   * SimpleActionClient or the transition to another final state. This happens
   * when the goal was recalled, rejected, preempted, aborted or lost.
   * 
   * @return <tt>true</tt> - If a final state is reached<br>
   *         <tt>false</tt> - If the action client's node handle is not ok
   *         (normally, the method would block forever if no final state is
   *         reached)
   */
  public boolean waitForResult() {
    return waitForResult(new Duration(0, 0));
  }

  /**
   * Waits up to the specified duration for the action server to successfully
   * send the final result to this SimpleActionClient or the transition to
   * another final state. This happens when the goal was recalled, rejected,
   * preempted, aborted or lost.
   * 
   * @param timeout
   *          The maximum duration to wait for the action server to reach a
   *          final goal state. A zero length duration results in unlimited
   *          waiting.
   * @return <tt>true</tt> - If a final state is reached within the given time<br>
   *         <tt>false</tt> - Otherwise
   */
  public boolean waitForResult(Duration timeout) {
    if (timeout.isNegative()) {
      actionClient.getNode().getLog().warn(
          "[SimpleActionClient] Timeouts can't be negative. Timeout is ["
          + (timeout.totalNsecs() / 1000000) + " ms]");
      return false;
    } else if (goalHandle == null || goalHandle.isExpired()) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Trying to waitForResult() while no goal is active. You are incorrectly using SimpleActionClient!");
      return false;
    }

    Time timeoutTime = actionClient.getNode().getCurrentTime().add(timeout);
    Duration loopCheckTime = new Duration(1, 0); // Check every second, if
                                                 // NodeHandle is ok

    synchronized (waitSync) {
      while (actionClient.getNode().isOk()) {

        Duration timeLeft = timeoutTime.subtract(actionClient.getNode().getCurrentTime());
        if ((timeLeft.totalNsecs() / 1000000 <= 0 && !timeout.isZero())
            || currSimpleState.equals(SimpleGoalState.StateEnum.DONE)) {
          break;
        }

        if ((timeLeft.totalNsecs() > loopCheckTime.totalNsecs()) || timeout.isZero()) {
          timeLeft = loopCheckTime;
        }

        try {
          waitSync.wait(timeLeft.totalNsecs() / 1000000);
        } catch (InterruptedException e) {
        }

      }

      return currSimpleState.equals(SimpleGoalState.StateEnum.DONE);
    }

  }

  @Override
  public void feedbackCallback(
      ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle,
      T_FEEDBACK feedback) {
    if (this.goalHandle != goalHandle) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Got a callback on a goalHandle that the client is not tracking. This is an internal SimpleActionClient/ActionClient bug or a GoalID collision.");
    }

    if (callbacks != null) {
      callbacks.feedbackCallback(feedback);
    }
  }

  @Override
  public void transitionCallback(
      ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle)
      throws RosException {
    if (this.goalHandle != goalHandle) {
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Got a callback on a goalHandle that the client is not tracking. This is an internal SimpleActionClient/ActionClient bug or a GoalID collision.");
    }

    CommState commState = goalHandle.getCommState();
    switch (commState.getState()) {
    case WAITING_FOR_GOAL_ACK:
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] BUG: Shouldn't ever get a transition callback for WAITING_FOR_GOAL_ACK");
      break;
    case PENDING:
      if (!currSimpleState.equals(SimpleGoalState.StateEnum.PENDING)) {
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Got a transition to CommState '" + commState
            + "' while client is in SimpleGoalState '" + currSimpleState + "'");
      }
      break;
    case ACTIVE:

      switch (currSimpleState.getState()) {
      case PENDING:
        setSimpleState(SimpleGoalState.StateEnum.ACTIVE);
        if (callbacks != null) {
          callbacks.activeCallback();
        }
        break;
      case ACTIVE:
        break;
      case DONE:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Got a transition to CommState '" + commState
            + "' while client is in SimpleGoalState '" + currSimpleState + "'");
        break;
      default:
        actionClient.getNode().getLog().fatal(
            "[SimpleActionClient] Unknown SimpleGoalState '" + currSimpleState + "'");
        break;
      }
      break;

    case WAITING_FOR_RESULT:
      break;
    case WAITING_FOR_CANCEL_ACK:
      break;
    case RECALLING:
      if (!currSimpleState.equals(SimpleGoalState.StateEnum.PENDING)) {
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Got a transition to CommState '" + commState
            + "' while client is in SimpleGoalState '" + currSimpleState + "'");
      }
      break;
    case PREEMPTING:

      switch (currSimpleState.getState()) {
      case PENDING:
        setSimpleState(SimpleGoalState.StateEnum.ACTIVE);
        if (callbacks != null) {
          callbacks.activeCallback();
        }
        break;
      case ACTIVE:
        break;
      case DONE:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] Got a transition to CommState '" + commState
            + "' while client is in SimpleGoalState '" + currSimpleState + "'");
        break;
      default:
        actionClient.getNode().getLog().fatal("" +
            "[SimpleActionClient] Unknown SimpleGoalState '" + currSimpleState + "'");
        break;
      }
      break;

    case DONE:

      switch (currSimpleState.getState()) {
      case PENDING:
      case ACTIVE:
        setSimpleState(SimpleGoalState.StateEnum.DONE);
        if (callbacks != null) {
          callbacks.doneCallback(getState(), goalHandle.getResult());
        }
        synchronized (waitSync) {
          waitSync.notifyAll();
        }
        break;
      case DONE:
        actionClient.getNode().getLog().error(
            "[SimpleActionClient] BUG: Got a second transition to 'DONE'");
        break;
      default:
        actionClient.getNode().getLog().fatal(
            "[SimpleActionClient] Unknown SimpleGoalState '" + currSimpleState + "'");
        break;
      }
      break;

    default:
      actionClient.getNode().getLog().error(
          "[SimpleActionClient] Unknown CommState '" + commState + "'");
      break;
    }

  }
}
