package org.ros;

/**
 * A simple interface for entry points into your app.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public abstract class RosMain {
  /**
   * Called to start your node by some magical ros java file.
   * 
   * @param argv
   */
  abstract public void rosMain(String argv[]);
}
