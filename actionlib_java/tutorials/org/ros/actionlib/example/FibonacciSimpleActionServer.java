package org.ros.actionlib.example;

import org.ros.Node;
import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosInitException;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * The FibonacciSimpleActionServer is a specialized DefaultSimpleActionServer
 * that offers services related to the Fibonacci action. The
 * FibonacciSimpleActionServer completely hides the Generics approach of the
 * DefaultSimpleActionServer's implementation.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @see DefaultSimpleActionServer
 */
public class FibonacciSimpleActionServer
    extends
    DefaultSimpleActionServer<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  public FibonacciSimpleActionServer(
      String nameSpace,
      ActionSpec<?, FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> spec,
      SimpleActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks,
      boolean useBlockingGoalCallback) throws RosInitException {
    super(nameSpace, spec, callbacks, useBlockingGoalCallback);
  }

  public FibonacciSimpleActionServer(
      Node parent,
      String nameSpace,
      ActionSpec<?, FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> spec,
      SimpleActionServerCallbacks<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> callbacks,
      boolean useBlockingGoalCallback) throws RosInitException {
    super(parent, nameSpace, spec, callbacks, useBlockingGoalCallback);
  }
}
