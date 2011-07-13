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

import org.ros.exception.RosRuntimeException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executes {@link NodeMain}s in separate threads. Note that the user is
 * responsible for shutting down {@link NodeMain}s.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeRunner {

  private final Executor executor;

  /**
   * @return an instance of {@link NodeRunner} that uses a default
   *         {@link Executor}
   */
  public static NodeRunner newDefault() {
    return new NodeRunner(Executors.newCachedThreadPool());
  }

  /**
   * @param executor
   *          {@link NodeMain}s will be executed using this
   */
  public NodeRunner(Executor executor) {
    this.executor = executor;
  }

  /**
   * Executes the supplied {@link NodeMain} using the supplied
   * {@link NodeConfiguration} and the configured {@link Executor}.
   * 
   * @param nodeMain
   *          the {@link NodeMain} to execute
   * @param nodeConfiguration
   *          the {@link NodeConfiguration} that will be passed to the
   *          {@link NodeMain}
   * @throws RosRuntimeException
   *           thrown if {@link NodeMain} throws an exception
   */
  public void run(final NodeMain nodeMain, final NodeConfiguration nodeConfiguration) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          nodeMain.main(nodeConfiguration);
        } catch (Exception e) {
          throw new RosRuntimeException(e);
        }
      }
    });
  }

}
