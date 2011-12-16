package org.ros.actionlib.example;

import org.ros.node.DefaultNodeRunner;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeRunner;

/**
 * A main for running the Fibonacci simple action server.
 */
public class RunFibonacciSimpleActionServer {

  public static void main(String[] args) {
    main();
  }

  public static void main() {
    try {
      // user code implementing the SimpleActionServerCallbacks interface
      FibonacciSimpleActionServerCallbacks impl = new FibonacciSimpleActionServerCallbacks();

      FibonacciActionSpec spec = new FibonacciActionSpec();
      final FibonacciSimpleActionServer sas =
          spec.buildSimpleActionServer("fibonacci_server", impl, true);

      NodeConfiguration configuration = NodeConfiguration.newPrivate();

      NodeRunner runner = DefaultNodeRunner.newDefault();

      runner.run(new NodeMain() {

        @Override
        public void onStart(Node node) {
          sas.addClientPubSub(node);
        }
        
        @Override
        public void onShutdown(Node node) {
        }

        @Override
        public void onShutdownComplete(Node node) {
        }

      }, configuration);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
