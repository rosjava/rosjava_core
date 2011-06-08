/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ros.exception;


/**
 * Exception for errors initializing ROS state, inspired by rospy
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class RosInitException extends RosException {


  private static final long serialVersionUID = 1L;


  /**
   * @param exception
   */
  public RosInitException(final Exception exception) {
    super(exception);
  }

  /**
   * @param message
   * @param throwable
   */
  public RosInitException(final String message, final Throwable throwable) {
    super(message,throwable);
  }

  /**
   * @param message
   */
  public RosInitException(final String message) {
    super(message);
  }

}
