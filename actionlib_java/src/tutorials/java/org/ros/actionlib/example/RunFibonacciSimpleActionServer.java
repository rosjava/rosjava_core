package org.ros.actionlib.example;

import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

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
      final String nodeName = "fibonacci_server";
      final FibonacciSimpleActionServer sas =
          spec.buildSimpleActionServer(nodeName, impl, true);

      NodeConfiguration configuration = NodeConfiguration.newPrivate();

      NodeMainExecutor runner = DefaultNodeMainExecutor.newDefault();

      runner.executeNodeMain(new NodeMain() {

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

        @Override
        public GraphName getDefaultNodeName() {
          return new GraphName(nodeName);
        }
      }, configuration);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
