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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.DefaultNodeFactory;
import org.ros.internal.node.NodeFactory;
import org.ros.namespace.GraphName;

import java.util.Collection;
import java.util.List;
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
  private final Map<GraphName, NodeRunnerNodeMain> nodeMains;

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses a default
   *         {@link Executor}
   */
  public static NodeRunner newDefault() {
    return newDefault(Executors.newCachedThreadPool());
  }

  /**
   * @return an instance of {@link DefaultNodeRunner} that uses the supplied
   *         {@link Executor}
   */
  public static NodeRunner newDefault(Executor executor) {
    return new DefaultNodeRunner(new DefaultNodeFactory(), executor);
  }

  /**
   * @param nodeFactory
   *          Node factory to use for node creation.
   * @param executor
   *          {@link NodeMain}s will be executed using this
   */
  public DefaultNodeRunner(NodeFactory nodeFactory, Executor executor) {
    this.nodeFactory = nodeFactory;
    this.executor = executor;
    nodeMains = Maps.newConcurrentMap();
  }

  @Override
  public void run(final NodeMain nodeMain, final NodeConfiguration nodeConfiguration,
      final Collection<? extends NodeListener> nodeListeners) {
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
        List<NodeListener> finalNodeListeners = Lists.newArrayList();
        NodeRunnerNodeMain finalNodeMain = new NodeRunnerNodeMain(nodeMain);
        finalNodeListeners.add(finalNodeMain);
        if (nodeListeners != null) {
          for (NodeListener listener : nodeListeners) {
            finalNodeListeners.add(listener);
          }
        }
        
        try {
          // The node factory willcall onCreate for the NodeMain.
          nodeFactory.newNode(nodeConfigurationCopy, finalNodeListeners);
        } catch (Exception e) {
          // TODO(damonkohler): Log to rosout. Maybe there should be a rosout
          // node associated with each DefaultNodeRunner?
          System.err.println("Exception thrown in NodeMain. Will attempt shutdown.");
          e.printStackTrace();
          shutdownNodeMain(finalNodeMain);
       }
        // TODO(damonkohler): If NodeMain.main() exits, we no longer know when
        // to remove it from our map. Once the NodeStateListener is implemented,
        // we can add a listener after NodeMain.main() exits that will remove
        // the node from the map on shutdown.
      }
    });
  }

  @Override
  public void run(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
    run(nodeMain, nodeConfiguration, null);
  }

  @Override
  public void shutdown() {
    synchronized (nodeMains) {
      for (NodeRunnerNodeMain nodeMain : nodeMains.values()) {
        shutdownNodeMain(nodeMain);
      }
      nodeMains.clear();
    }
  }

  /**
   * Register a node main with the runner.
   * 
   * @param finalNodeMain
   */
  private void registerNodeMain(NodeRunnerNodeMain finalNodeMain) {
    GraphName nodeName = finalNodeMain.getMainNode().getName();
    synchronized (nodeMains) {
      Preconditions.checkState(!nodeMains.containsKey(nodeName), "A node with name \""
          + nodeName + "\" already exists.");
      nodeMains.put(nodeName, finalNodeMain);
    }
  }

  /**
   * Unregister a node main with the runner.
   * 
   * @param finalNodeMain
   * @param node
   */
  private void unregisterNodeMain(NodeRunnerNodeMain finalNodeMain) {
    GraphName nodeName = finalNodeMain.getMainNode().getName();
    synchronized (nodeMains) {
      nodeMains.remove(nodeName);
    }
  }

  /**
   * Shut down the node associated with the node main.
   * 
   * @param nodeMain
   */
  private void shutdownNodeMain(NodeRunnerNodeMain nodeMain) {
    boolean success = true;
    try {
      nodeMain.getMainNode().shutdown();
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
   * A {@link NodeMain} which wraps the client's main so that various items can be trapped.
   *
   *
   * @author Keith M. Hughes
   * @since Nov 28, 2011
   */
  private class NodeRunnerNodeMain extends NodeMain {
    /**
     * The {@link NodeMain} handed in by the client.
     */
    private NodeMain original;
    
    /**
     * The node created for the original {link NodeMain}.
     */
    private Node mainNode;
    
    public NodeRunnerNodeMain(NodeMain original) {
      this.original = original;
    }

    @Override
    public void onNodeCreate(Node node) {
      this.mainNode = node;
      
      original.onNodeCreate(node);
      
      registerNodeMain(this);
    }

    @Override
    public void onNodeShutdown(Node node) {
      original.onNodeShutdown(node);
      
      unregisterNodeMain(this);
    }

    /**
     * @return the node which was created for the node main.
     */
    public Node getMainNode() {
      return mainNode;
    }

  }
}
