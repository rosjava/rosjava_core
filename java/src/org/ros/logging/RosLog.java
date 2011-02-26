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
package org.ros.logging;

import org.apache.commons.logging.impl.SimpleLog;

/** A Ros implementation of the commons logging.
 * FIXME Make it a full implementation that barfs to /rosout.
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 *
 */
public class RosLog extends SimpleLog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param nodeName This will be added as the tag to all log messages.
   */
  public RosLog(String nodeName) {
    super(nodeName);
    setLevel(LOG_LEVEL_DEBUG);
  }


}
