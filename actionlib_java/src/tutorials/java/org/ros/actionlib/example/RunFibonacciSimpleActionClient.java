package org.ros.actionlib.example;

import org.ros.actionlib.client.SimpleActionClient;
import org.ros.actionlib.client.SimpleActionClientCallbacks;
import org.ros.actionlib.state.SimpleClientGoalState;
import org.ros.exception.RosException;
import org.ros.message.actionlib_tutorials.FibonacciActionFeedback;
import org.ros.message.actionlib_tutorials.FibonacciActionGoal;
import org.ros.message.actionlib_tutorials.FibonacciActionResult;
import org.ros.message.actionlib_tutorials.FibonacciFeedback;
import org.ros.message.actionlib_tutorials.FibonacciGoal;
import org.ros.message.actionlib_tutorials.FibonacciResult;
import org.ros.node.DefaultNodeRunner;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeRunner;

import java.util.concurrent.TimeUnit;

public class RunFibonacciSimpleActionClient {

  public static void main(String[] args) {
    NodeConfiguration configuration = NodeConfiguration.newPrivate();
    NodeRunner runner = DefaultNodeRunner.newDefault();

    run(runner, configuration);

  }

  public static void run(NodeRunner runner, NodeConfiguration configuration) {
    try {
      int length = 10;

      // create action specification for the Fibonacci action without
      // having to write a lot of data types as class parameters for
      // the ActionSpec/ActionClient classes based on Generics
      FibonacciActionSpec spec = new FibonacciActionSpec();
      final FibonacciSimpleActionClient sac = spec.buildSimpleActionClient("fibonacci_client");

      runner.run(new NodeMain() {

        @Override
        public void onStart(Node node) {
          sac.addClientPubSub(node);
        }
        
        @Override
        public void onShutdown(Node node) {
        }

      }, configuration);

      System.out.println("[Test] Waiting for action server to start");
      // wait for the action server to start
      sac.waitForServer(); // will wait for infinite time

      System.out.println("[Test] Action server started, sending goal");
      // send a goal to the action
      FibonacciGoal goal = spec.createGoalMessage();
      goal.order = length;
      sac.sendGoal(goal, new SimpleActionClientCallbacks<FibonacciFeedback, FibonacciResult>() {
        @Override
        public void feedbackCallback(FibonacciFeedback feedback) {
          System.out.print("Client feedback\n\t");
          for (int num : feedback.sequence)
            System.out.print(" " + num);
          System.out.println();
        }

        @Override
        public void doneCallback(SimpleClientGoalState state, FibonacciResult result) {
          System.out.println("Client done " + state);
          System.out.println("\t" + result.sequence[result.sequence.length - 1]);
        }

        @Override
        public void activeCallback() {
          System.out.println("Client active");
        }
      });

      // wait for the action to return
      System.out.println("[Test] Waiting for result.");
      boolean finished_before_timeout = sac.waitForResult(100, TimeUnit.SECONDS);

      if (finished_before_timeout) {
        SimpleClientGoalState state = sac.getState();
        System.out.println("[Test] Action finished: " + state.toString());

        FibonacciResult res = sac.getResult();
        System.out.print("[Test] Fibonacci sequence (" + goal.order + "):");
        for (int i : res.sequence) {
          System.out.print(" " + i);
        }
        System.out.println();
      } else {
        System.out.println("[Test] Action did not finish before the time out");
      }
    } catch (RosException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void run2(NodeRunner runner, NodeConfiguration configuration) {

    try {
      int order = 4;

      FibonacciActionSpec spec = new FibonacciActionSpec();

      final SimpleActionClient<FibonacciActionFeedback, FibonacciActionGoal, FibonacciActionResult, FibonacciFeedback, FibonacciGoal, FibonacciResult> sac =
          spec.buildSimpleActionClient("fibonacci");
      runner.run(new NodeMain() {

        @Override
        public void onStart(Node node) {
          sac.addClientPubSub(node);
        }
        
        @Override
        public void onShutdown(Node node) {
        }

      }, configuration);

      System.out.println("[Test] Waiting for action server to start");
      // wait for the action server to start
      sac.waitForServer(); // will wait for infinite time

      System.out.println("[Test] Action server started, sending goal");
      // send a goal to the action
      FibonacciGoal goal = spec.createGoalMessage();
      goal.order = order;
      sac.sendGoal(goal);

      // wait for the action to return
      System.out.println("[Test] Waiting for result.");
      boolean finished_before_timeout = sac.waitForResult(100, TimeUnit.SECONDS);

      if (finished_before_timeout) {
        SimpleClientGoalState state = sac.getState();
        System.out.println("[Test] Action finished: " + state.toString());

        FibonacciResult res = sac.getResult();
        System.out.print("[Test] Fibonacci sequence (" + goal.order + "):");
        for (int i : res.sequence) {
          System.out.print(" " + i);
        }
        System.out.println();
      } else {
        System.out.println("[Test] Action did not finish before the time out");
      }
    } catch (RosException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
