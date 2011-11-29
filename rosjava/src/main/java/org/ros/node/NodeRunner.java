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

import java.util.Collection;

/**
 * Executes {@link NodeMain}s.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface NodeRunner {

  /**
   * Executes the supplied {@link NodeMain} using the supplied
   * {@link NodeConfiguration}.
   * 
   * @param nodeMain
   *          the {@link NodeMain} to execute
   * @param nodeConfiguration
   *          the {@link NodeConfiguration} that will be used to create the
   *          {@link Node}
   * @param nodeListeners
   *        A collection of {@link NodeListener} instances to be added to the node
   *        immediately after creation (can be {@code null})
   */
  void run(NodeMain nodeMain, NodeConfiguration nodeConfiguration,
      Collection<? extends NodeListener> nodeListeners);
  
  /**
   * Executes the supplied {@link NodeMain} using the supplied
   * {@link NodeConfiguration}.
   * 
   * @param nodeMain
   *          the {@link NodeMain} to execute
   * @param nodeConfiguration
   *          the {@link NodeConfiguration} that will be used to create the
   *          {@link Node}
   */
  void run(NodeMain nodeMain, NodeConfiguration nodeConfiguration);

  /**
   * Shutdown all started {@link NodeMain}s.
   */
  void shutdown();

}