package org.ros.tutorials;

import org.ros.Callback;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.RosLoader;
import org.ros.RosMain;
import org.ros.exceptions.RosInitException;

/**
 * Simple rosjava publisher and subscriber node, requires an external roscore
 * (master) running.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public class RosPubSub extends RosMain {
  Node node;
  // FIXME ugly huge expansion of message name due to String class?
  Publisher<org.ros.message.std.String> pub;

  // callback for string messages
  Callback<org.ros.message.std.String> hello_cb = new Callback<org.ros.message.std.String>() {

    @Override
    public void onNewMessage(org.ros.message.std.String m) {
      node.logInfo(m.data);
    }
  };

  public void rosMain(String[] argv) {
    try {
      node = new Node(argv, "/sample_node");
      node.init();
      pub = node.createPublisher("hello", org.ros.message.std.String.class);
      node.createSubscriber("hello", hello_cb, org.ros.message.std.String.class);

      int seq = 0;
      while (true) {
        org.ros.message.std.String str = new org.ros.message.std.String();
        str.data = "Hello " + seq++;
        pub.publish(str);
        Thread.sleep(100);
      }
    } catch (Exception e) {
      node.logFatal(e);
    }
  }

  public static void main(String[] argv) throws RosInitException, ClassNotFoundException,
      InstantiationException, IllegalAccessException {

    // Example of using a string based class loader so that we can load classes
    // dynamically at runtime.
    // TODO(ethan) this is internal stuff, move away.
    RosLoader rl = new RosLoader();
    RosMain rm = rl.loadClass("org.ros.tutorials.RosPubSub");
    rm.rosMain(argv);
  }

}
