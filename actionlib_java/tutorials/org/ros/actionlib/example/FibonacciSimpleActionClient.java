package org.ros.actionlib.example;

import org.ros.Node;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.exception.RosException;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;

/**
 * The FibonacciSimpleActionClient is a specialized SimpleActionClient that is
 * intended to work with an action server offering services related to the
 * Fibonacci action. The FibonacciSimpleActionClient completely hides the
 * Generics approach of the SimpleActionClient's implementation.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @see SimpleActionClient
 */
public class FibonacciSimpleActionClient
    extends
    SimpleActionClient<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> {

  public FibonacciSimpleActionClient(String nameSpace, FibonacciActionSpec spec)
      throws RosException {
    super(nameSpace, spec);
  }

  public FibonacciSimpleActionClient(Node parentNode, String nameSpace, FibonacciActionSpec spec)
      throws RosException {
    super(parentNode, nameSpace, spec);
  }

}
