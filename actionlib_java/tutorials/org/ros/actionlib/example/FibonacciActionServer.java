package org.ros.actionlib.example;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.server.ActionServerCallbacks;
import org.ros.actionlib.server.DefaultActionServer;
import org.ros.exception.RosInitException;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * The FibonacciActionServer is a specialized DefaultActionServer that offers
 * services related to the Fibonacci action. The FibonacciActionServer
 * completely hides the Generics approach of the DefaultActionServer's
 * implementation.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @see DefaultActionServer
 */
public class FibonacciActionServer
    extends
    DefaultActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  public FibonacciActionServer(
      String nameSpace,
      ActionSpec<?, FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> spec,
      ActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks)
      throws RosInitException {

    super(nameSpace, spec, callbacks);

  }

  public FibonacciActionServer(
      Node parent,
      String nameSpace,
      ActionSpec<?, FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> spec,
      ActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks)
      throws RosInitException {
    super(parent, nameSpace, spec, callbacks);
  }
}
