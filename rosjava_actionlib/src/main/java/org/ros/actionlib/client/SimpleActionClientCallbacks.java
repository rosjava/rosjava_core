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

import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.message.Message;

/**
 * 
 * An interface between a SimpleActionClient and user code. Allows user code to
 * react on callbacks due to the start and stop of the SimpleActionClient and
 * received feedback messages.<br>
 * Implementations of this interface may be used as a parameter for the
 * SimpleActionClient's sendGoal()-method.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @param <T_FEEDBACK>
 *          feedback message
 * @param <T_RESULT>
 *          result message
 * 
 * @see SimpleActionClient#sendGoal(Message, SimpleActionClientCallbacks)
 * 
 */
public interface SimpleActionClientCallbacks<T_FEEDBACK extends Message, T_RESULT extends Message> {
  /**
   * Gets called when the SimpleActionClient transitions to state 'ACTIVE'. This
   * happens when the SimpleActionClient receives an acknowledgment from the
   * server that a goal is being worked on. The implementation of this method
   * should not contain any time-consuming operations and return immediately.
   */
  void activeCallback();

  /**
   * Gets called when the SimpleActionClient receives a feedback message from
   * the action server. The implementation of this method should not contain any
   * time-consuming operations and return immediately.
   * 
   * @param feedback
   *          The received feedback message
   */
  void feedbackCallback(T_FEEDBACK feedback);

  /**
   * Gets called when the SimpleActionClient received the final result from the
   * action server or a transition to another final state occurred. This happens
   * when the goal was recalled, rejected, preempted, aborted or lost. The
   * implementation of this method should not contain any time-consuming
   * operations and return immediately.
   * 
   * @param state
   *          The final goal state
   * @param result
   *          The received result message
   */
  void doneCallback(SimpleClientGoalState state, T_RESULT result);
}
