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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.namespace.GraphName;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executes {@link NodeMain}s in separate threads.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DefaultNodeRunner implements NodeRunner {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(DefaultNodeRunner.class);

  private final NodeFactory nodeFactory;
  private final Executor executor;
  private final Map<GraphName, NodeMain> nodeMains;

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses a default
   *         {@link Executor}
   */
  public static NodeRunner newDefault() {
    return new DefaultNodeRunner(new DefaultNodeFactory(), Executors.newCachedThreadPool());
  }

  /**
   * @param executor
   *          {@link NodeMain}s will be executed using this
   */
  public DefaultNodeRunner(NodeFactory nodeFactory, Executor executor) {
    this.nodeFactory = nodeFactory;
    this.executor = executor;
    nodeMains = Maps.newConcurrentMap();
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
    Preconditions.checkNotNull(nodeConfiguration.getNodeName(), "Node name not specified.");
    if (DEBUG) {
      log.info("Starting node: " + nodeConfiguration.getNodeName());
    }
    // NOTE(damonkohler): To help avoid race conditions, we have to make a copy
    // of the NodeConfiguration in the current thread.
    final NodeConfiguration nodeConfigurationCopy = NodeConfiguration.copyOf(nodeConfiguration);
    executor.execute(new Runnable() {
      @Override
      public void run() {
        Node node = nodeFactory.newNode(nodeConfigurationCopy);
        GraphName nodeName = node.getName();
        synchronized (nodeMains) {
          Preconditions.checkState(!nodeMains.containsKey(nodeName), "A node with name \""
              + nodeName + "\" already exists.");
          nodeMains.put(nodeName, nodeMain);
        }
        try {
          nodeMain.main(node);
        } catch (Exception e) {
          // TODO(damonkohler): Log to rosout. Maybe there should be a rosout
          // node associated with each DefaultNodeRunner?
          System.err.println("Exception thrown in NodeMain. Will attempt shutdown.");
          e.printStackTrace();
          shutdownNodeMain(nodeMain);
          nodeMains.remove(nodeName);
        }
        // TODO(damonkohler): If NodeMain.main() exits, we no longer know when
        // to remove it from our map. Once the NodeStateListener is implemented,
        // we can add a listener after NodeMain.main() exits that will remove
        // the node from the map on shutdown.
      }
    });
  }

  @Override
  public void shutdown() {
    synchronized (nodeMains) {
      for (NodeMain nodeMain : nodeMains.values()) {
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
      System.err.println("Exception thrown while shutting down NodeMain.");
      e.printStackTrace();
      success = false;
    }
    if (success) {
      System.out.println("Shutdown successful.");
    }
  }
}
