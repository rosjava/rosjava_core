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

import org.ros.internal.message.Message;

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
