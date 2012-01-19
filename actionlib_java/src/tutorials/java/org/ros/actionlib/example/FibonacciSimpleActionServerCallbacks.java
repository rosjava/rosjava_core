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

import org.ros.actionlib.server.SimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * A {@link SimpleActionServerCallbacks} for the fibonacci example.
 */
public class FibonacciSimpleActionServerCallbacks
    implements
    SimpleActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  @Override
  public
      void
      blockingGoalCallback(
          FibonacciGoal goal,
          SimpleActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> actionServer) {

    System.out.println("BLOCKING GOAL CALLBACK");

    int order = (goal.order > 0) ? goal.order : 0;
    int[] seq = new int[order];

    if (order > 0) {
      seq[0] = 0;
      publishFeedback(seq, actionServer);
      snore();
    }
    if (order > 1) {
      seq[1] = 1;
      publishFeedback(seq, actionServer);
      snore();
    }

    for (int i = 2; i < order; i++) {
      seq[i] = seq[i - 1] + seq[i - 2];
      publishFeedback(seq, actionServer);
      snore();
    }

    FibonacciResult result = new FibonacciResult();
    result.sequence = seq;
    actionServer.setSucceeded(result, "");

  }

  @Override
  public
      void
      goalCallback(
          SimpleActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> actionServer) {
    System.out.println("GOAL CALLBACK");
  }

  @Override
  public
      void
      preemptCallback(
          SimpleActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> actionServer) {
    System.out.println("PREEMPT CALLBACK");
  }

  private void snore() {

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }

  }

  /**
   * Send feedback to the client on how far along the computation is.
   * 
   * @param seq
   *          The sequence step the computation is on.
   * @param actionServer
   *          The action server publishing information.
   */
  private
      void
      publishFeedback(
          int[] seq,
          SimpleActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> actionServer) {
    FibonacciFeedback feedback = new FibonacciFeedback();
    feedback.sequence = seq;
    actionServer.publishFeedback(feedback);

    System.out.print("FEEDBACK:");
    for (int i = 0; i < feedback.sequence.length; i++) {
      if (feedback.sequence[i] == 0 && i != 0) {
        break;
      }
      System.out.print(" " + feedback.sequence[i]);
    }
    System.out.println();

  }

}
