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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executes {@link NodeMain}s in separate threads.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNodeRunner implements NodeRunner {

  private final Executor executor;
  private final Collection<NodeMain> nodeMains;

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses a default
   *         {@link Executor}
   */
  public static NodeRunner newDefault() {
    return new DefaultNodeRunner(Executors.newCachedThreadPool());
  }

  /**
   * @param executor
   *          {@link NodeMain}s will be executed using this
   */
  public DefaultNodeRunner(Executor executor) {
    this.executor = executor;
    nodeMains = Collections.synchronizedCollection(new ArrayList<NodeMain>());
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
   */
  @Override
  public void run(final NodeMain nodeMain, final NodeConfiguration nodeConfiguration) {
    nodeMains.add(nodeMain);
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          nodeMain.main(nodeConfiguration);
        } catch (Exception e) {
          nodeMains.remove(nodeMain);
          // TODO(damonkohler): Log to rosout. Maybe there should be a rosout
          // node associated with each DefaultNodeRunner?
          System.out.println("Exception thrown in NodeMain. Will attempt shutdown.");
          e.printStackTrace();
          shutdownNodeMain(nodeMain);
        }
      }
    });
  }

  @Override
  public void shutdown() {
    synchronized (nodeMains) {
      for (NodeMain nodeMain : nodeMains) {
        shutdownNodeMain(nodeMain);
      }
      nodeMains.clear();
    }
  }

  private void shutdownNodeMain(NodeMain nodeMain) {
    boolean success = true;
    try {
      nodeMain.shutdown();
    } catch (Exception e) {
      // Ignore spurious errors during shutdown.
      System.out.println("Exception thrown while shutting down NodeMain.");
      e.printStackTrace();
      success = false;
    }
    if (success) {
      System.out.println("Shutdown successful.");
    }
  }
}
