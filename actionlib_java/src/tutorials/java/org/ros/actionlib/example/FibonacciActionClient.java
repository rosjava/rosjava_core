package org.ros.actionlib.example;

import org.ros.actionlib.client.ActionClient;
import org.ros.exception.RosException;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * The FibonacciActionClient is a specialized ActionClient that is intended to
 * work with an action server offering services related to the Fibonacci action.
 * The FibonacciActionClient completely hides the Generics approach of the
 * ActionClient's implementation.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @see ActionClient
 */
public class FibonacciActionClient
    extends
    ActionClient<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  public FibonacciActionClient(String nameSpace, FibonacciActionSpec spec) throws RosException {
    super(nameSpace, spec);
  }
}