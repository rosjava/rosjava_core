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

package org.ros.actionlib.example;

import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.server.ActionServerCallbacks;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.message.actionlib_tutorials.FibonacciAction;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * The FibonacciActionSpec class represents the action specification for the
 * Fibonacci action. It completely hides the Generics approach of the Actionlib
 * implementation.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @see ActionSpec
 */
public class FibonacciActionSpec
    extends
    ActionSpec<FibonacciAction, FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  /**
   * Constructor to create an action specification for the Fibonacci action.
   */
  public FibonacciActionSpec() throws RosException {
    super(FibonacciAction.class, "actionlib_tutorials/FibonacciAction",
        "actionlib_tutorials/FibonacciActionFeedback", "actionlib_tutorials/FibonacciActionGoal",
        "actionlib_tutorials/FibonacciActionResult", "actionlib_tutorials/FibonacciFeedback",
        "actionlib_tutorials/FibonacciGoal", "actionlib_tutorials/FibonacciResult");
  }

  @Override
  public FibonacciActionClient buildActionClient(String nameSpace) {

    FibonacciActionClient ac = null;
    try {
      ac = new FibonacciActionClient(nameSpace, this);
    } catch (RosException e) {
      e.printStackTrace();
    }
    return ac;

  }

  @Override
  public FibonacciSimpleActionClient buildSimpleActionClient(String nameSpace) {

    FibonacciSimpleActionClient sac = null;
    try {
      return new FibonacciSimpleActionClient(nameSpace, this);
    } catch (RosException e) {
      e.printStackTrace();
    }
    return sac;

  }

  @Override
  public
      FibonacciActionServer
      buildActionServer(
          String nameSpace,
          ActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks) {

    return new FibonacciActionServer(nameSpace, this, callbacks);

  }

  @Override
  public
      FibonacciSimpleActionServer
      buildSimpleActionServer(
          String nameSpace,
          SimpleActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks,
          boolean useBlockingGoalCallback) {

    return new FibonacciSimpleActionServer(nameSpace, this, callbacks, useBlockingGoalCallback);

  }

}
