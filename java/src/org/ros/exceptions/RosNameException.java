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

import org.ros.internal.namespace.GraphName;

/**
 * Thrown in the case where a {@link GraphName} is invalid.
 * 
 * <p>
 * This is a {@link RuntimeException} because in most cases it is the result of
 * a programming error. In addition, it is not typically possible to recover
 * from this error.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class RosNameException extends RosRuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param exception
   */
  public RosNameException(final Exception exception) {
    super(exception);
  }

  /**
   * @param message
   * @param throwable
   */
  public RosNameException(final String message, final Throwable throwable) {
    super(message, throwable);
  }

  /**
   * @param message
   */
  public RosNameException(final String message) {
    super(message);
  }

}
