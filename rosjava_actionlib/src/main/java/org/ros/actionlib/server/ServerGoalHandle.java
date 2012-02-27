/*
 * Copyright (C) 2011 Alexander Perzylo, Technische Universität München
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.actionlib.server;

import org.ros.exception.RosException;
import org.ros.message.Message;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.message.actionlib_msgs.GoalStatus;

/**
 * A goal on the server.
 */
public class ServerGoalHandle<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  protected T_ACTION_GOAL actionGoal;
  protected DefaultActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer;
  protected StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> statusTracker;
  private static Object statusSync = new Object();

  protected ServerGoalHandle(
      StatusTracker<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> statusTracker,
      DefaultActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer) {
    this.statusTracker = statusTracker;
    this.statusTracker.goalHandle = this;
    this.actionGoal = statusTracker.actionGoal;
    this.actionServer = actionServer;

  }

  /**
   * Request a cancel.
   * 
   * @return True if the request went out, false otherwise.
   */
  protected boolean setCancelRequested() {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return false;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Transitioning to a cancel requested state on goal, id: "
                + getGoalID().id + ", stamp: " + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    boolean ok = false;
    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.PENDING:
        goalStatus.status = GoalStatus.RECALLING;
        actionServer.publishStatus();
        ok = true;
        break;
      case GoalStatus.ACTIVE:
        goalStatus.status = GoalStatus.PREEMPTING;
        actionServer.publishStatus();
        ok = true;
        break;
      }
    }
    return ok;

  }

  public void setAccepted(String text) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Accepting goal, id: " + getGoalID().id + ", stamp: "
                + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.PENDING:
        goalStatus.status = GoalStatus.ACTIVE;
        goalStatus.text = text;
        actionServer.publishStatus();
        break;
      case GoalStatus.RECALLING:
        goalStatus.status = GoalStatus.PREEMPTING;
        goalStatus.text = text;
        actionServer.publishStatus();
        break;
      default:
        actionServer
            .getNode()
            .getLog()
            .error(
                "[ServerGoalHandle] To transition to "
                    + "an active state, the goal must be in state '" + GoalStatus.PENDING
                    + "' (PENDING) or '" + GoalStatus.RECALLING + "' (RECALLING), "
                    + "it is currently in state '" + goalStatus.status + "'");
        break;
      }
    }

  }

  public void setCanceled(T_RESULT result, String text) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Setting canceled status on goal, id: " + getGoalID().id
                + ", stamp: " + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.PENDING:
      case GoalStatus.RECALLING:
        goalStatus.status = GoalStatus.RECALLED;
        goalStatus.text = text;
        actionServer.publishResult(goalStatus, result);
        break;
      case GoalStatus.ACTIVE:
      case GoalStatus.PREEMPTING:
        goalStatus.status = GoalStatus.PREEMPTED;
        goalStatus.text = text;
        actionServer.publishResult(goalStatus, result);
        break;
      default:
        actionServer
            .getNode()
            .getLog()
            .error(
                "[ServerGoalHandle] To transition to "
                    + "a cancelled state, the goal must be in state '" + GoalStatus.PENDING
                    + "' (PENDING), '" + GoalStatus.ACTIVE + "' (ACTIVE), '"
                    + GoalStatus.PREEMPTING + "' (PREEMPTING) or '" + GoalStatus.RECALLING
                    + "' (RECALLING). It is currently in state '" + goalStatus.status + "'");
        break;
      }
    }

  }

  public void setRejected(T_RESULT result, String text) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Setting status to 'REJECTED' on goal, id: " + getGoalID().id
                + ", stamp: " + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.PENDING:
      case GoalStatus.RECALLING:
        goalStatus.status = GoalStatus.REJECTED;
        goalStatus.text = text;
        actionServer.publishResult(goalStatus, result);
        break;
      default:
        actionServer
            .getNode()
            .getLog()
            .error(
                "[ServerGoalHandle] To transition to "
                    + "a rejected state, the goal must be in state '" + GoalStatus.PENDING
                    + "' (PENDING) or '" + GoalStatus.RECALLING + "' (RECALLING). "
                    + "It is currently in state '" + goalStatus.status + "'");
        break;
      }
    }

  }

  public void setAborted(T_RESULT result, String text) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Setting status to 'ABORTED' on goal, id: " + getGoalID().id
                + ", stamp: " + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.ACTIVE:
      case GoalStatus.PREEMPTING:
        goalStatus.status = GoalStatus.ABORTED;
        goalStatus.text = text;
        actionServer.publishResult(goalStatus, result);
        break;
      default:
        actionServer
            .getNode()
            .getLog()
            .error(
                "[ServerGoalHandle] To transition to "
                    + "an aborted state, the goal must be in state '" + GoalStatus.ACTIVE
                    + "' (ACTIVE) or '" + GoalStatus.PREEMPTING + "' (PREEMPTING). "
                    + "It is currently in state '" + goalStatus.status + "'");
        break;
      }
    }

  }

  public void setSucceeded(T_RESULT result, String text) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Setting status to 'SUCCEEDED' on goal, id: " + getGoalID().id
                + ", stamp: " + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      GoalStatus goalStatus = statusTracker.goalStatus;
      short status = goalStatus.status;
      switch (status) {
      case GoalStatus.ACTIVE:
      case GoalStatus.PREEMPTING:
        goalStatus.status = GoalStatus.SUCCEEDED;
        goalStatus.text = text;
        actionServer.publishResult(goalStatus, result);
        break;
      default:
        actionServer
            .getNode()
            .getLog()
            .error(
                "[ServerGoalHandle] To transition to "
                    + "a succeeded state, the goal must be in state '" + GoalStatus.ACTIVE
                    + "' (ACTIVE) or '" + GoalStatus.PREEMPTING + "' (PREEMPTING). "
                    + "It is currently in state '" + goalStatus.status + "'");
        break;
      }
    }

  }

  /**
   * Cancel any future status updates.
   */
  public void cancelStatusUpdates() {
    statusTracker.destructionTime = actionServer.getNode().getCurrentTime();
  }

  public void publishFeedback(T_FEEDBACK feedback) {

    if (actionServer == null || actionGoal == null) {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] You are attempting to call methods on an uninitialized goal handle");
      return;
    }

    actionServer
        .getNode()
        .getLog()
        .debug(
            "[ServerGoalHandle] Publishing feedback for goal, id: " + getGoalID().id + ", stamp: "
                + (getGoalID().stamp.totalNsecs() / 1000000) + "ms");

    synchronized (statusSync) {
      actionServer.publishFeedback(statusTracker.goalStatus, feedback);
    }

  }

  /**
   * Get the goal from the action goal.
   * 
   * @return The goal.
   * @throws RosException
   */
  public T_GOAL getGoal() throws RosException {
    if (actionGoal != null && actionServer != null) {
      return actionServer.spec.getGoalFromActionGoal(actionGoal);
    } else {
      actionServer.getNode().getLog()
          .error("[ServerGoalHandle] Attempt to getGoal() on an uninitialized ServerGoalHandle");
      return null;
    }
  }

  public GoalID getGoalID() {

    GoalID goalId;
    if (actionServer != null && actionGoal != null) {
      synchronized (statusSync) {
        goalId = statusTracker.goalStatus.goal_id;
      }
    } else {
      actionServer.getNode().getLog()
          .error("[ServerGoalHandle] Attempt to getGoalID() on an uninitialized ServerGoalHandle");
      goalId = null;
    }
    return goalId;

  }

  public GoalStatus getGoalStatus() {

    GoalStatus goalStatus;
    if (actionServer != null && actionGoal != null) {
      synchronized (statusSync) {
        goalStatus = statusTracker.goalStatus;
      }
    } else {
      actionServer
          .getNode()
          .getLog()
          .error(
              "[ServerGoalHandle] Attempt to getGoalStatus() on an uninitialized ServerGoalHandle");
      goalStatus = null;
    }
    return goalStatus;

  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object o) {

    if (!(o instanceof ServerGoalHandle<?, ?, ?, ?, ?, ?>)) {
      return false;
    }

    ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> otherHandle =
        null;
    try {
      otherHandle =
          (ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>) o;
    } catch (Exception cce) {
      return false;
    }

    if (actionGoal == null && otherHandle.actionGoal == null) {
      return true;
    }

    if (actionGoal == null || otherHandle.actionGoal == null) {
      return false;
    }

    return getGoalID().equals(otherHandle.getGoalID());

  }

}
