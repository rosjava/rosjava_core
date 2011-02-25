package org.ros.exceptions;


/**
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 *  Exception for errors initializing ROS state, inspired by rospy
 */
public class RosInitException extends RosException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   */
  public RosInitException(String message) {
    super(message);
  }

}
