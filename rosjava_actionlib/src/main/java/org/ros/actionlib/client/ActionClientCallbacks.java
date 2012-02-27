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

import org.ros.exception.RosException;
import org.ros.message.Message;

/**
 * 
 * An interface between an ActionClient and user code. Allows user code to react
 * on callbacks due to received feedback messages and occurred transitions of
 * the action client's state machine.<br>
 * Implementations of this interface may be used as a parameter for the
 * ActionClient's sendGoal()-method.
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
 * 
 * @see ActionClient#sendGoal(Message, ActionClientCallbacks)
 * 
 */
public interface ActionClientCallbacks<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  /**
   * Gets called when a transition in the action client's state machine occurs.
   * The implementation of this method should not contain any time-consuming
   * operations and return immediately.
   * 
   * @param goalHandle
   *          A goal handle of an active goal that caused the transition
   */
      void
      transitionCallback(
          ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle)
          throws RosException;

  /**
   * Gets called when the action client receives a feedback message from the
   * action server. The implementation of this method should not contain any
   * time-consuming operations and return immediately.
   * 
   * @param goalHandle
   *          A goal handle of an active goal that caused the feedback
   * @param feedback
   *          The received feedback message
   */
      void
      feedbackCallback(
          ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle,
          T_FEEDBACK feedback) throws RosException;

}
