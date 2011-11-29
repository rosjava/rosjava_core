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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.namespace.GraphName;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
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
  private final ExecutorService executorService;
  private final Multimap<GraphName, Node> nodes;

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses a default
   *         {@link ExecutorService}
   */
  public static NodeRunner newDefault() {
    return newDefault(Executors.newCachedThreadPool());
  }

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses the supplied
   *         {@link ExecutorService}
   */
  public static NodeRunner newDefault(ExecutorService executorService) {
    return new DefaultNodeRunner(new DefaultNodeFactory(), executorService);
  }

  /**
   * @param nodeFactory
   *          Node factory to use for node creation.
   * @param executorService
   *          {@link NodeMain}s will be executed using this
   */
  public DefaultNodeRunner(NodeFactory nodeFactory, ExecutorService executorService) {
    this.nodeFactory = nodeFactory;
    this.executorService = executorService;
    nodes = Multimaps.synchronizedMultimap(HashMultimap.<GraphName, Node>create());
  }

  @Override
  public void run(final NodeMain nodeMain, final NodeConfiguration nodeConfiguration,
      final Collection<NodeListener> nodeListeners) {
    Preconditions.checkNotNull(nodeConfiguration.getNodeName(), "Node name not specified.");
    if (DEBUG) {
      log.info("Starting node: " + nodeConfiguration.getNodeName());
    }
    // NOTE(damonkohler): To help avoid race conditions, we have to make a copy
    // of the NodeConfiguration in the current thread.
    final NodeConfiguration nodeConfigurationCopy = NodeConfiguration.copyOf(nodeConfiguration);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        Collection<NodeListener> nodeListenersCopy = Lists.newArrayList();
        nodeListenersCopy.add(new RegistrationListener());
        nodeListenersCopy.add(nodeMain);
        if (nodeListeners != null) {
          nodeListenersCopy.addAll(nodeListeners);
        }
        // The new Node will call onStart().
        nodeFactory.newNode(nodeConfigurationCopy, nodeListenersCopy);
      }
    });
  }

  @Override
  public void run(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
    run(nodeMain, nodeConfiguration, null);
  }

  @Override
  public void shutdown() {
    synchronized (nodes) {
      for (Node node : nodes.values()) {
        safelyShutdownNode(node);
      }
    }
  }

  /**
   * Trap and log any exceptions while shutting down the supplied {@link Node}.
   * 
   * @param node
   *          the {@link Node} to shut down
   */
  private void safelyShutdownNode(Node node) {
    boolean success = true;
    try {
      node.shutdown();
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

  /**
   * Register a {@link Node} with the {@link NodeRunner}.
   * 
   * @param nodeMain
   *          the {@link NodeMain} associated with the {@link Node}
   */
  private void registerNode(Node node) {
    GraphName nodeName = node.getName();
    synchronized (nodes) {
      for (Node illegalNode : nodes.get(nodeName)) {
        System.err.println(String.format(
            "Node name collision. Existing node %s (%s) will be shutdown.", nodeName,
            illegalNode.getUri()));
        illegalNode.shutdown();
      }
      nodes.put(nodeName, node);
    }
  }

  /**
   * Unregister a {@link Node} with the {@link NodeRunner}.
   * 
   * @param node
   *          the {@link Node} to unregister
   */
  private void unregisterNode(Node node) {
    nodes.get(node.getName()).remove(node);
  }

  private class RegistrationListener implements NodeListener {
    @Override
    public void onStart(Node node) {
      registerNode(node);
    }

    @Override
    public void onShutdown(Node node) {
      unregisterNode(node);
    }
  }
}
