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

package org.ros.actionlib.client;

import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.util.GoalIDGenerator;
import org.ros.exception.RosException;
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.message.actionlib_msgs.GoalStatusArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A GoalManager maintains a collection of all active goals represented by their
 * respective GoalHandle. The
 * {@link #initGoal(Message, ActionClientCallbacks, ActionSpec)} method sends
 * out new goals and adds their GoalHandles to its list of active goals. When
 * new status, feedback or result messages are received by the ActionClient, it
 * can use the GoalManager to update the affected GoalHandles in a convenient
 * way. If a GoalHandle shall not be updated anymore, it can be deleted using
 * the {@link #deleteGoalHandle(ClientGoalHandle)} or
 * {@link #deleteGoalHandles(Collection)} method.
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
public class GoalManager<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  /**
   * The collection of active GoalHandles
   */
  protected List<ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>> listOfGoalHandles;

  /**
   * The associated ActionClient
   */
  protected ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionClient;

  /**
   * ID Generator for creating unique GoalIDs
   */
  protected GoalIDGenerator idGenerator;

  /**
   * Constructor to create a GoalManager that is associated with the given
   * ActionClient.
   * 
   * @param actionClient
   *          The action client to use in order to send out goal or cancel
   *          messages
   */
  public GoalManager(
      ActionClient<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> actionClient) {

    this.actionClient = actionClient;
    idGenerator = new GoalIDGenerator(actionClient.getNode());
    listOfGoalHandles =
        new ArrayList<ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>>(
            50);
  }

  /**
   * Creates and sends out a new action goal message using an unique GoalID and
   * the given goal message and builds a GoalHandle and its CommStateMachine to
   * keep track of the progress of that goal. The goalHandle gets added to the
   * list of active goals.
   * 
   * @param goal
   *          The goal message, which shall be sent to the action server
   * @param callbacks
   *          The callback methods that get called when transitions in a
   *          GoalHandle's CommStateMachine occur or feedback messages are
   *          received.
   * @param spec
   *          The action specification
   * @return The GoalHandle used to track the goal
   */
  public
      ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>
      initGoal(
          T_GOAL goal,
          ActionClientCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks,
          ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec)
          throws RosException {

    GoalID id = idGenerator.generateID();
    Time time = actionClient.getNode().getCurrentTime();
    T_ACTION_GOAL actionGoal = spec.createActionGoalMessage(goal, time, id);

    if (actionGoal == null) {
      actionClient.getNode().getLog().error("[GoalManager] Couldn't create action goal message");
    } else {
      actionClient.publishActionGoal(actionGoal);
    }

    CommStateMachine<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> stateMachine =
        new CommStateMachine<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            actionGoal, callbacks, spec, actionClient);
    ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle =
        new ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            this, actionClient, stateMachine);

    synchronized (listOfGoalHandles) {
      listOfGoalHandles.add(goalHandle);
    }

    return goalHandle;

  }

  /**
   * Deletes the specified GoalHandle from the list of active goals. It will not
   * receive updates on new status, feedback or result messages anymore.
   * 
   * @param goalHandle
   *          The GoalHandle that is to be removed
   */
  public
      void
      deleteGoalHandle(
          ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle) {
    actionClient.getNode().getLog().debug("[GoalManager] Deleting goal handle");
    synchronized (listOfGoalHandles) {
      listOfGoalHandles.remove(goalHandle);
    }
  }

  /**
   * Deletes the specified GoalHandles from the list of active goals. They will
   * not receive updates on new status, feedback or result messages anymore.
   * 
   * @param c
   *          A collection of GoalHandles which shall be removed
   */
  public
      void
      deleteGoalHandles(
          Collection<ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>> c) {
    actionClient.getNode().getLog().debug("[GoalManager] Deleting goal handles");
    synchronized (listOfGoalHandles) {
      listOfGoalHandles.removeAll(c);
    }
  }

  /**
   * Updates all listed GoalHandles on received GoalStatus messages.
   * 
   * @param goalStatuses
   *          A list of GoalStatus messages
   */
  public void updateStatuses(GoalStatusArray goalStatuses) {
    synchronized (listOfGoalHandles) { // TODO use reentrant read write lock
                                       // instead? lots of read access and way
                                       // less write access
      for (ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle : listOfGoalHandles) {
        try {
          goalHandle.getStateMachine().updateStatus(goalStatuses, goalHandle);
        } catch (RosException e) {
          actionClient.getNode().getLog().error("Error during updateStatuses", e);
        }
      }
    }
  }

  /**
   * Updates all listed GoalHandles on received action feedback messages.
   * 
   * @param actionFeedback
   */
  public void updateFeedbacks(T_ACTION_FEEDBACK actionFeedback) throws RosException {
    synchronized (listOfGoalHandles) {
      for (ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle : listOfGoalHandles) {
        goalHandle.getStateMachine().updateFeedback(actionFeedback, goalHandle);
      }
    }
  }

  /**
   * Updates all listed GoalHandles on received action result messages.
   * 
   * @param actionResult
   */
  public void updateResults(T_ACTION_RESULT actionResult) throws RosException {
    synchronized (listOfGoalHandles) {
      for (ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle : listOfGoalHandles) {
        goalHandle.getStateMachine().updateResult(actionResult, goalHandle);
      }
    }
  }

  /**
   * Deactivates all ClientGoalHandles in the list of active goal handles and
   * clears the list.
   */
  public void clear() {
    synchronized (listOfGoalHandles) {
      for (ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle : listOfGoalHandles) {
        goalHandle.shutdown(false);
      }
      listOfGoalHandles.clear();
    }
  }

  /**
   * Sends out an action goal message using the associated ActionClient.
   * 
   * @param actionGoal
   *          An action goal message
   */
  protected void sendActionGoal(T_ACTION_GOAL actionGoal) {
    actionClient.publishActionGoal(actionGoal);
  }

  /**
   * Sends out a cancel message using the associated ActionClient.
   * 
   * @param goalIDMessage
   *          The GoalID of the goal to be canceled
   */
  protected void sendCancelGoal(GoalID goalIDMessage) {
    actionClient.publishCancelGoal(goalIDMessage);
  }
}
