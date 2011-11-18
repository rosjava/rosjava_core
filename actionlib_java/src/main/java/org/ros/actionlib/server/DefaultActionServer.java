package org.ros.actionlib.server;

import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.util.GoalIDGenerator;
import org.ros.exception.RosException;
import org.ros.message.Duration;
import org.ros.message.Message;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.message.actionlib_msgs.GoalStatus;
import org.ros.message.actionlib_msgs.GoalStatusArray;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A base implementation of an actionlib server.
 * 
 * <p>
 * This server should be started by calling {@link #main(NodeConfiguration)}.
 */
public class DefaultActionServer<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message>
    implements NodeMain {
  /**
   * Parent node, if any. Can be null.
   */
  protected Node parent;

  /**
   * Name of the server node.
   */
  protected String name;

  /**
   * The action specification
   */
  protected ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec;

  protected ActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks;
  protected Node node;

  /**
   * The subscriber which listens for goals coming into the server.
   */
  protected Subscriber<T_ACTION_GOAL> subGoal;

  /**
   * The subscriber which listens for cancellation requests coming into the
   * server.
   */
  protected Subscriber<GoalID> subCancelGoal;

  /**
   * The publisher which sends out feedback messages.
   */
  protected Publisher<T_ACTION_FEEDBACK> pubFeedback;

  /**
   * The publisher which sends out final result messages.
   */
  protected Publisher<T_ACTION_RESULT> pubResult;

  /**
   * The publisher which sends out status messages about the server.
   */
  protected Publisher<GoalStatusArray> pubStatus;

  /**
   * The list of status trackers for every goal being handled by the server.
   */
  protected List<StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>> statusList =
      new ArrayList<StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>>(
          50);

  /**
   * Last time when a cancel was done.
   */
  protected Time lastCancelTime = new Time(0, 0);

  protected Duration statusListTimeout;

  /**
   * Timer for sending out status updates on the server.
   */
  protected Timer statusTimer = new Timer();

  protected GoalIDGenerator idGenerator;
  protected boolean active = false;
  protected boolean shutdown = false;

  protected ReentrantLock lock = new ReentrantLock(true); // fair lock, so no
                                                          // goal has to starve

  public DefaultActionServer(
      String name,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec,
      ActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks) {

    this(null, name, spec, callbacks);
  }

  public DefaultActionServer(
      Node parent,
      String name,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec,
      ActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks) {
    this.parent = parent;
    this.name = name;
    this.callbacks = callbacks;
    this.spec = spec;
  }

  @Override
  public void main(Node node) throws Exception {
	this.node = node;
    // TODO(damonkohler): Move the logic for conditionally changing this node's
    // name to the location where this NodeMain is launched.
    idGenerator = new GoalIDGenerator(node);

    if (!active) {
      if (initServer()) {
        active = true;
        publishStatus();
      } else {
        node.getLog()
            .error(
                "[DefaultActionServer] Couldn't set up needed Subscribers and Publishers due to a RosException");
      }
    }
  }

  @Override
  public void shutdown() {
    if (statusTimer != null) {
      statusTimer.cancel();
      statusTimer = null;
    }

    shutdown = true;
    active = false;

    // pubStatus.shutdown();
    // pubFeedback.shutdown();
    // pubResult.shutdown();
    // subGoal.shutdown();
    // subCancelGoal.shutdown();

    node.shutdown();

  }

  /**
   * Initialize the server.
   * 
   * @return True if the server initialized, false otherwise.
   */
  protected boolean initServer() {

    try {
      pubFeedback = node.newPublisher("feedback", spec.getActionFeedbackMessage());
      pubResult = node.newPublisher("result", spec.getActionResultMessage());
      pubStatus = node.newPublisher("status", "actionlib_msgs/GoalStatusArray");

      MessageListener<T_ACTION_GOAL> goalCallback = new MessageListener<T_ACTION_GOAL>() {
        @Override
        public void onNewMessage(T_ACTION_GOAL actionGoal) {
          doGoalCallback(actionGoal);
        }
      };
      subGoal = node.newSubscriber("goal", spec.getActionGoalMessage(), goalCallback);

      MessageListener<GoalID> cancelCallback = new MessageListener<GoalID>() {
        @Override
        public void onNewMessage(GoalID goalID) {
          doCancelCallback(goalID);
        }
      };
      subCancelGoal = node.newSubscriber("cancel", "actionlib_msgs/GoalID", cancelCallback);

    } catch (Exception re) {
      node.getLog().error("Unable to start up " + getClass().getName(), re);

      if (subGoal != null) {
        // subGoal.shutdown();
        subGoal = null;
      }
      if (subCancelGoal != null) {
        // subCancelGoal.shutdown();
        subCancelGoal = null;
      }
      if (pubFeedback != null) {
        // pubFeedback.shutdown();
        pubFeedback = null;
      }
      if (pubResult != null) {
        // pubResult.shutdown();
        pubResult = null;
      }
      if (pubStatus != null) {
        // pubStatus.shutdown();
        pubStatus = null;
      }
      return false;

    }

    double pStatusFrequency;
    double pStatusListTimeout;

    ParameterTree parameterClient = node.newParameterTree();
    try {
      pStatusListTimeout = parameterClient.getDouble("status_list_timeout", 5.0);
    } catch (Exception e) {
      e.printStackTrace();
      pStatusListTimeout = 5.0;
    }
    statusListTimeout = new Duration(pStatusListTimeout);

    try {
      pStatusFrequency = parameterClient.getDouble("status_frequency", 5.0);
    } catch (Exception e) {
      e.printStackTrace();
      pStatusFrequency = 5.0;
    }
    if (pStatusFrequency <= 0) {
      pStatusFrequency = 5.0;
      node.getLog()
          .warn(
              "[DefaultActionServer] Status frequency parameter is not a positive number. Using default value of 5Hz!");
    }

    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        publishStatus();
      }
    };
    long milliSecsPeriod = (long) (1000 / pStatusFrequency);
    if (milliSecsPeriod == 0) {
      node.getLog()
          .warn(
              "[DefaultActionServer] Status frequency parameter is too large. Maximum status update rate capped to 1000Hz.");
      milliSecsPeriod = 1;
    }
    if (statusTimer != null) {
      statusTimer.cancel();
    }
    statusTimer = new Timer();
    statusTimer.scheduleAtFixedRate(task, 0, milliSecsPeriod);

    return true;

  }

  /**
   * Gets the action server's node.
   * 
   * @return The node.
   */
  protected Node getNode() {
    return node;
  }

  /**
   * Publish feedback for a given goal.
   * 
   * @param goalStatus
   *          The goal the feedback is being published for.
   * @param feedback
   *          The feedback on the goal.
   */
  protected void publishFeedback(GoalStatus goalStatus, T_FEEDBACK feedback) {
    if (!active) {
      node.getLog().warn(
          "[DefaultActionServer] Trying to publishFeedback() on an inactive ActionServer.");
      return;
    }

    Time now = node.getCurrentTime();
    T_ACTION_FEEDBACK actionFeedback = spec.createActionFeedbackMessage(feedback, now, goalStatus);
    node.getLog().debug(
        "[DefaultActionServer] Publishing feedback for goal, id: " + goalStatus.goal_id.id
            + ", stamp: " + (goalStatus.goal_id.stamp.totalNsecs() / 1000000) + "ms");
    pubFeedback.publish(actionFeedback);
  }

  /**
   * A goal has completed. Publish the result for the goal.
   * 
   * @param goalStatus
   *          The goal which has been completed.
   * @param result
   *          The result for the goal.
   */
  protected void publishResult(GoalStatus goalStatus, T_RESULT result) {

    if (!active) {
      node.getLog().warn(
          "[DefaultActionServer] Trying to publishResult() on an inactive DefaultActionServer.");
      return;
    }

    Time now = node.getCurrentTime();
    T_ACTION_RESULT actionResult = spec.createActionResultMessage(result, now, goalStatus);
    node.getLog().debug(
        "[DefaultActionServer] Publishing result for goal, id: " + goalStatus.goal_id.id
            + ", stamp: " + (goalStatus.goal_id.stamp.totalNsecs() / 1000000) + "ms");
    pubResult.publish(actionResult);
  }

  /**
   * Publish the status of the server.
   * 
   * <p>
   * This will include the status of all goals in the server.
   */
  protected void publishStatus() {
    if (!active) {
      node.getLog().warn(
          "[DefaultActionServer] Trying to publishStatus() on an inactive DefaultActionServer.");
      return;
    }

    lock.lock();
    try {
      GoalStatusArray statusArray = new GoalStatusArray();
      statusArray.header.stamp = node.getCurrentTime();
      statusArray.status_list.ensureCapacity(statusList.size());

      for (int i = 0; i < statusList.size(); i++) {

        StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> st =
            statusList.get(i);
        statusArray.status_list.add(st.goalStatus);

        if (!st.destructionTime.isZero()) {

          Time timeoutTime = st.destructionTime.add(statusListTimeout);
          Duration timeoutDur = timeoutTime.subtract(node.getCurrentTime());
          if (timeoutDur.isNegative()) {
            statusList.remove(st);
            i--;
          }
        }
      }

      pubStatus.publish(statusArray);

    } finally {
      lock.unlock();
    }

  }

  /**
   * A new goal has arrived in the server from the client. Add it to the queue.
   * 
   * @param actionGoal
   *          The action goal received.
   */
  public void doGoalCallback(T_ACTION_GOAL actionGoal) {

    StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> newTracker =
        null;

    lock.lock();
    try {

      if (!active) {
        return;
      }

      node.getLog().debug("[DefaultActionServer] Received a new goal request");

      // check if new goal already is represented in status list
      GoalID goalID = spec.getGoalIDFromActionGoal(actionGoal);
      for (StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> st : statusList) {
        GoalStatus statusOfExistingGoal = st.goalStatus;
        if (goalID.id.equals(statusOfExistingGoal.goal_id.id)) {

          // The goal can be in a RECALLING state if a cancel message came in
          // before the goal
          if (statusOfExistingGoal.status == GoalStatus.RECALLING) {
            statusOfExistingGoal.status = GoalStatus.RECALLED;
            publishResult(statusOfExistingGoal, spec.createResultMessage());
          }

          if (st.goalHandle == null) {
            st.destructionTime = node.getCurrentTime();
          }

          return;
        }

      }

      // Goal didn't exist. Create a new one.
      newTracker =
          new StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
              actionGoal, spec, idGenerator);
      statusList.add(newTracker);

      // if goal has already been canceled by a cancel message according to its
      // timestamp
      if (!goalID.stamp.isZero() && !goalID.stamp.subtract(lastCancelTime).isPositive()) {
        ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> gh =
            new ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
                newTracker, this);
        gh.setCanceled(spec.createResultMessage(), "This goal handle was canceled "
            + "by the action server because its timestamp is before the "
            + "timestamp of the last cancel request.");
        return;
      }

    } catch (RosException e) {
      node.getLog().error("Exception during goal callback", e);
    } finally {
      lock.unlock();
    }

    // Tell the callbacks that we have a new goal.
    ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle =
        new ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            newTracker, this);
    callbacks.goalCallback(goalHandle);
  }

  /**
   * The server has received a cancellation request.
   * 
   * @param cancelGoal
   *          The goal to be canceled.
   */
  public void doCancelCallback(GoalID cancelGoal) {
    List<StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>> callbackList =
        null;

    lock.lock();
    try {

      if (!active) {
        return;
      }

      node.getLog().debug("[DefaultActionServer] Received a new cancel request");

      boolean cancelIDfound = false;
      callbackList =
          new ArrayList<StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>>();
      for (int i = 0; i < statusList.size(); i++) {

        StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> existingST =
            statusList.get(i);
        if (existingST.isCancelRequestTracker()) {
          continue;
        }
        GoalID existingGoal = existingST.goalStatus.goal_id;

        // if id is "" and time stamp is 0 => cancel everything
        // if ids match => cancel this goal
        // if time stamp is not 0 => cancel everything before time stamp
        if ((cancelGoal.id.equals("") && cancelGoal.stamp.isZero())
            || cancelGoal.id.equals(existingGoal.id)
            || (!cancelGoal.stamp.isZero() && cancelGoal.stamp.subtract(existingGoal.stamp)
                .isPositive())) {

          if (cancelGoal.id.equals(existingGoal.id)) {
            cancelIDfound = true;
          }

          if (!existingST.isCancelRequestTracker()) {
            if (existingST.goalHandle == null) {
              new ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
                  existingST, this);
              existingST.destructionTime = new Time(0, 0);
            }

            if (existingST.goalHandle.setCancelRequested()) {
              callbackList.add(existingST);
            }
          }

        }
      }

      if (!cancelGoal.id.equals("") && !cancelIDfound) {
        StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> cancelTracker =
            new StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
                this, cancelGoal, GoalStatus.RECALLING);
        cancelTracker.destructionTime = node.getCurrentTime();
        statusList.add(cancelTracker);
      }

      if (cancelGoal.stamp.subtract(lastCancelTime).isPositive()) {
        lastCancelTime = new Time(cancelGoal.stamp);
      }

    } finally {
      lock.unlock();
    }

    // Send a callback for every goal which is being canceled.
    if (callbackList != null) {
      for (StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> st : callbackList) {
        callbacks.cancelCallback(st.goalHandle);
      }
    }

  }
}
