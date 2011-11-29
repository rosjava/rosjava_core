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

package org.ros.node;

/**
 * A listener for lifecycle events on a {@link Node}.
 *
 * @author Keith M. Hughes
 * @since Nov 22, 2011
 */
public interface NodeListener {

  /**
   * The node has just been created.
   * 
   * <p>
   * The node will not have been started yet.
   * 
   * @param node
   *          The {@link Node} which has been started.
   */
  void onNodeCreate(Node node);
  
  /**
   * The node has just been shut down.
   * 
   * <p>
   * The node will have been shut down.
   * 
   * @param node
   *          The {@link Node} which has been shut down.
   */
  void onNodeShutdown(Node node);

}
