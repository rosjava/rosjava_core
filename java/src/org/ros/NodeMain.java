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

package org.ros;

/**
 * Defines a {@link Node} entry point.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public interface NodeMain {

  /**
   * {@link Node} entry point.
   * 
   * @param nodeConfiguration the {@link NodeConfiguration} that will be passed to the
   *        {@link Node}
   */
  void main(NodeConfiguration nodeConfiguration) throws Exception;

}
