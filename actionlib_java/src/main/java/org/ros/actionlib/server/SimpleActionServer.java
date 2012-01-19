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

/**
 * An action server which simplifies much of the complexity of a full
 * {@link DefaultActionServer}.
 * 
 * @author Keith M. Hughes
 * @since Jun 16, 2011
 */
public interface SimpleActionServer<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {

  /**
   * The server accepts a new goal from the client.
   * 
   * <p>
   * Any current goal is canceled.
   * 
   * @return The new goal.
   * @throws RosException
   */
  T_GOAL acceptNewGoal() throws RosException;

  boolean isNewGoalAvailable();

  boolean isPreemptRequested();

  boolean isActive();

  void setSucceeded();

  void setSucceeded(T_RESULT result, String text);

  void setAborted();

  void setAborted(T_RESULT result, String text);

  void setPreempted();

  void setPreempted(T_RESULT result, String text);

  /**
   * Publish feedback about the goal in process.
   * 
   * @param feedback
   *          The specific feedback to publish.
   */
  void publishFeedback(T_FEEDBACK feedback);
}