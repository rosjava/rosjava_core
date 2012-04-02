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

import actionlib_msgs.GoalStatus;
import org.ros.actionlib.ActionSpec;
import org.ros.exception.RosException;
import org.ros.internal.message.Message;
import org.ros.node.Node;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An action server which simplifies much of the complexity of a full
 * {@link DefaultActionServer}.
 */
public class DefaultSimpleActionServer<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message>
    implements
    ActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>,
    SimpleActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> {
  /**
   * The action server which is doing all of the work.
   */
  protected DefaultActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionServer;

  /**
   * The callbacks which this server will use.
   * 
   * <p>
   * Can be full.
   */
  protected SimpleActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks;

  /**
   * The current goal being processed by this server, if any.
   * 
   * <p>
   * Can be full.
   */
  protected ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> currentGoal;

  /**
   * The next goal to be used by this server.
   */
  protected ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> nextGoal;

  protected volatile boolean newGoal = false;
  protected volatile boolean preemptRequest = false;
  protected volatile boolean newGoalPreemptRequest = false;

  protected volatile boolean killCallbackThread = false;
  private Object threadSync = new Object();
  protected Thread callbackThread = null;

  protected ReentrantLock lock = new ReentrantLock(true); // fair lock
  protected Condition c = lock.newCondition();

  protected boolean useBlockingGoalCallback = false;

  public DefaultSimpleActionServer(
      String nameSpace,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec,
      SimpleActionServerCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks,
      boolean useBlockingGoalCallback) {

    this.callbacks = callbacks;
    this.actionServer =
        new DefaultActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            nameSpace, spec, this);
    if (useBlockingGoalCallback) {
      this.useBlockingGoalCallback = true;
      startCallbackThread();
    }

  }

  /**
   * The server accepts a new goal from the client.
   * 
   * <p>
   * Any current goal is canceled.
   * 
   * @return The new goal.
   * @throws RosException
   */
  @Override
  public T_GOAL acceptNewGoal() throws RosException {
    lock.lock();
    try {

      if (!newGoal || nextGoal == null) {
        actionServer
            .getNode()
            .getLog()
            .error(
                "[DefaultSimpleActionServer] Attempting to accept the next goal when a new goal is not available");
        return null;
      }

      if (isActive() && currentGoal != null && !currentGoal.equals(nextGoal)) {

        currentGoal.setCanceled(actionServer.spec.createResultMessage(),
            "This goal was canceled because another goal was received by the simple action server");
      }

      actionServer.getNode().getLog().debug("[DefaultSimpleActionServer] Accepting a new goal");

      currentGoal = nextGoal;
      nextGoal = null;
      newGoal = false;

      preemptRequest = newGoalPreemptRequest;
      newGoalPreemptRequest = false;

      currentGoal.setAccepted("This goal has been accepted by the simple action server");
      return currentGoal.getGoal();

    } finally {
      lock.unlock();
    }

  }

  @Override
  public boolean isNewGoalAvailable() {
    return newGoal;
  }

  @Override
  public boolean isPreemptRequested() {
    return newGoalPreemptRequest;
  }

  @Override
  public boolean isActive() {

    if (currentGoal == null) {
      return false;
    }

    short currStatus = currentGoal.getGoalStatus().status;
    return (currStatus == GoalStatus.ACTIVE || currStatus == GoalStatus.PREEMPTING);

  }

  @Override
  public void setSucceeded() {
    setSucceeded(actionServer.spec.createResultMessage(), "");
  }

  @Override
  public void setSucceeded(T_RESULT result, String text) {

    lock.lock();
    try {
      actionServer.getNode().getLog()
          .debug("[DefaultSimpleActionServer] setting the current goal to 'SUCCEEDED'");
      currentGoal.setSucceeded(result, text);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void setAborted() {
    setAborted(actionServer.spec.createResultMessage(), "");
  }

  @Override
  public void setAborted(T_RESULT result, String text) {

    lock.lock();
    try {
      actionServer.getNode().getLog()
          .debug("[DefaultSimpleActionServer] setting the current goal to 'ABORTED'");
      currentGoal.setAborted(result, text);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void setPreempted() {
    setPreempted(actionServer.spec.createResultMessage(), "");
  }

  @Override
  public void setPreempted(T_RESULT result, String text) {

    lock.lock();
    try {
      actionServer.getNode().getLog()
          .debug("[DefaultSimpleActionServer] setting the current goal to a canceled state'");
      currentGoal.setCanceled(result, text);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void publishFeedback(T_FEEDBACK feedback) {
    currentGoal.publishFeedback(feedback);
  }

  /**
   * Add all actionclient publishers and subscribers to the given node.
   * 
   * <p>
   * Lifetime of the node is taken over by the client.
   * 
   * @param node
   */
  public void addClientPubSub(Node node) {
    actionServer.addClientPubSub(node);
  }

  /**
   * Shut the server down.
   */
  public void shutdown() {
    stopCallbackThread();
    actionServer.shutdown();
  }

  protected void startCallbackThread() {
    // TODO(keith): make this truly asynchronous and remove this thread.
    synchronized (threadSync) {
      if (callbackThread == null) {

        final DefaultSimpleActionServer<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> sas =
            this;

        callbackThread = new Thread() {

          @Override
          public void run() {

            while (actionServer != null) {

              if (killCallbackThread) {
                killCallbackThread = false;
                return;
              }

              lock.lock();
              try {
                if (isActive()) {
                  actionServer
                      .getNode()
                      .getLog()
                      .error(
                          "[DefaultSimpleActionServer] This code should never be reached with an active goal.");
                } else if (isNewGoalAvailable()) {

                  T_GOAL goal = acceptNewGoal();

                  lock.unlock();
                  boolean exception = false;
                  try {
                    callbacks.blockingGoalCallback(goal, sas);
                  } catch (Exception e) {
                    actionServer
                        .getNode()
                        .getLog()
                        .error(
                            "[DefaultSimpleActionServer] Exception in user callback, current goal gets aborted: "
                                + e.toString());
                    exception = true;
                  } finally {
                    lock.lock();
                  }

                  if (exception) {
                    setAborted(
                        actionServer.spec.createResultMessage(),
                        "This goal was set to 'ABORTED' by the simple action server due to an exception in the user callback (blockingGoalCallback()).");
                  } else if (isActive()) {
                    actionServer
                        .getNode()
                        .getLog()
                        .error(
                            "The blockingGoalCallback did not set the goal to a terminal status.\nThis is a bug in the user's action server implementation, which has to be fixed!\n For now, the current goal gets set to 'ABORTED'.");
                    setAborted(
                        actionServer.spec.createResultMessage(),
                        "This goal was aborted by the simple action server. The user should have set a terminal status on this goal but did not.");
                  }

                } else {
                  if (!killCallbackThread) {
                    c.awaitNanos(1000000000);
                  }
                }

              } catch (RosException e) {
                actionServer.getNode().getLog().error("Exception in callback thread", e);
              } catch (InterruptedException e) {
                // Don't care
              } finally {
                lock.unlock();
              }
            }
          }

        };
        callbackThread.start();
      } else {
        actionServer
            .getNode()
            .getLog()
            .warn(
                "[DefaultSimpleActionServer] startCallbackThread(): callback thread is already running");
      }
    }
  }

  protected void stopCallbackThread() {

    synchronized (threadSync) {
      if (callbackThread != null) {

        killCallbackThread = true;

        lock.lock();
        try {
          c.signalAll();
        } finally {
          lock.unlock();
        }

        try {
          callbackThread.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        callbackThread = null;

      } else {
        actionServer.getNode().getLog()
            .warn("[SimpleActionClient] stopCallbackThread(): callback thread is not running");
      }
    }

  }

  @Override
  public
      void
      cancelCallback(
          ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalToCancel) {

    lock.lock();
    try {
      actionServer.getNode().getLog()
          .debug("[DefaultSimpleActionServer] received new cancel goal request");

      if (currentGoal != null && goalToCancel.equals(currentGoal)) {

        actionServer
            .getNode()
            .getLog()
            .debug(
                "[DefaultSimpleActionServer] Setting preemptRequest flag for the current goal and invoking callback");

        preemptRequest = true;
        if (callbacks != null) {
          callbacks.preemptCallback(this);
        }

      } else if (nextGoal != null && goalToCancel.equals(nextGoal)) {

        actionServer.getNode().getLog()
            .debug("[DefaultSimpleActionServer] Setting preemptRequest flag for the next goal");
        newGoalPreemptRequest = true;

      }

    } finally {
      lock.unlock();
    }

  }

  @Override
  public
      void
      goalCallback(
          ServerGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goal) {

    lock.lock();
    try {

      actionServer.getNode().getLog().debug("[DefaultSimpleActionServer] received a new goal");

      long goalTime = goal.getGoalID().stamp.totalNsecs();
      if ((currentGoal == null || goalTime >= currentGoal.getGoalID().stamp.totalNsecs())
          && (nextGoal == null || goalTime >= nextGoal.getGoalID().stamp.totalNsecs())) {

        if (nextGoal != null && (currentGoal == null || !nextGoal.equals(currentGoal))) {
          nextGoal.setCanceled(actionServer.spec.createResultMessage(),
              "This goal was canceled because another goal was "
                  + "received by the simple action server");
        }

        nextGoal = goal;
        newGoal = true;
        newGoalPreemptRequest = false;

        if (isActive()) {
          preemptRequest = true;
          callbacks.preemptCallback(this);
        }

        callbacks.goalCallback(this);

        if (useBlockingGoalCallback) {
          c.signalAll();
        }

      } else {
        goal.setCanceled(actionServer.spec.createResultMessage(),
            "This goal was canceled because another goal was "
                + "received by the simple action server");
      }

    } finally {
      lock.unlock();
    }

  }

}
