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
package org.ros.exceptions;

/**
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 *
 */
public class RosRuntimeException extends RuntimeException {

  /**
   * @param exception
   */
  public RosRuntimeException(final Exception exception) {
    super(exception);
  }
  /**
   * @param message
   * @param throwable
   */
  public RosRuntimeException(final String message, final Throwable throwable) {
    super(message,throwable);
  }
  /**
   * @param message
   */
  public RosRuntimeException(final String message) {
    super(message);
  }
  
//  /**
//   * @param object
//   */
//  public RosException(final Object object) {
//    super(object.toString());
//  }
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
