package org.ros;

/**
 *  A test of dynamic loading for ros classes.
 *  
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public class RosTestMain {

  /**
   * @param args
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    // Example of using a string based class loader so that we can load classes
    // dynamically at runtime.
    RosLoader rl = new RosLoader();
    RosMain rm = rl.loadClass("org.ros.tutorials.RosPubSub");
    rm.rosMain(args);
  }

}
