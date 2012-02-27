/*
 * Copyright (C) 2011 Google Inc.
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

package org.ros.actionlib;

/**
 * Constants used by the actionlib clients and servers.
 * 
 * @author khughes@google.com (Keith M. Hughes)
 */
public class ActionConstants {
  
  /**
   * The topic name for feedback.
   */
  public static final String TOPIC_NAME_FEEDBACK = "feedback";

  /**
   * The topic name for results.
   */
  public static final String TOPIC_NAME_RESULT = "result";

  /**
   * The topic name for status.
   */
  public static final String TOPIC_NAME_STATUS = "status";

  /**
   * The topic name for canceling.
   */
  public static final String TOPIC_NAME_CANCEL = "cancel";

  /**
   * The topic name for setting a goal.
   */
  public static final String TOPIC_NAME_GOAL = "goal";

  /**
   * The message type for status.
   */
  public static final String MESSAGE_TYPE_STATUS = "actionlib_msgs/GoalStatusArray";

  /**
   * The message type for goal cancellations.
   */
  public static final String MESSAGE_TYPE_CANCEL = "actionlib_msgs/GoalID";
}
