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

package org.ros.loader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.Ros;
import org.ros.RosLoader;
import org.ros.exception.RosInitException;
import org.ros.internal.namespace.DefaultGraphName;
import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.internal.node.DefaultNodeConfiguration;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Create {@link DefaultNodeConfiguration} instances using a ROS command-line and
 * environment specification.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CommandLineLoader extends RosLoader {

  private final List<String> argv;
  private final List<String> nodeArguments;
  private final List<String> remappingArguments;
  private final Map<String, String> environment;
  private final Map<String, String> specialRemappings;
  private final Map<GraphName, GraphName> remappings;

  private String nodeClassName;

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments.
   * Environment variables will be pulled from default {@link System}
   * environment variables.
   * 
   * @param argv
   *          command-line arguments
   */
  public CommandLineLoader(List<String> argv) {
    this(argv, System.getenv());
  }

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments
   * and environment variables.
   * 
   * @param argv
   *          command-line arguments
   * @param environment
   *          environment variables
   */
  public CommandLineLoader(List<String> argv, Map<String, String> environment) {
    Preconditions.checkArgument(argv.size() > 0);
    this.argv = argv;
    this.environment = environment;
    nodeArguments = Lists.newArrayList();
    remappingArguments = Lists.newArrayList();
    remappings = Maps.newHashMap();
    specialRemappings = Maps.newHashMap();
    parseArgv();
  }

  private void parseArgv() {
    nodeClassName = argv.get(0);
    for (String argument : argv.subList(1, argv.size())) {
      if (argument.contains(":=")) {
        remappingArguments.add(argument);
      } else {
        nodeArguments.add(argument);
      }
    }
  }

  public String getNodeClassName() {
    return nodeClassName;
  }

  public List<String> getNodeArguments() {
    return Collections.unmodifiableList(nodeArguments);
  }

  /**
   * Create NodeConfiguration according to ROS command-line and environment
   * specification.
   */
  @Override
  public DefaultNodeConfiguration createConfiguration() throws RosInitException {
    parseRemappingArguments();
    DefaultNodeConfiguration nodeConfiguration = DefaultNodeConfiguration.createDefault();
    nodeConfiguration.setParentResolver(buildParentResolver());
    nodeConfiguration.setRosRoot(getRosRoot());
    nodeConfiguration.setRosPackagePath(getRosPackagePath());
    nodeConfiguration.setMasterUri(getMasterUri());
    nodeConfiguration.setHost(getHost());
    if (specialRemappings.containsKey(CommandLine.NODE_NAME)) {
      nodeConfiguration.setNodeNameOverride(specialRemappings.get(CommandLine.NODE_NAME));
    }
    return nodeConfiguration;
  }

  private void parseRemappingArguments() {
    for (String remapping : remappingArguments) {
      Preconditions.checkState(remapping.contains(":="));
      String[] remap = remapping.split(":=");
      if (remap.length > 2) {
        throw new IllegalArgumentException("Invalid remapping argument: " + remapping);
      }
      if (remapping.startsWith("__")) {
        specialRemappings.put(remap[0], remap[1]);
      } else {
        remappings.put(Ros.newGraphName(remap[0]), Ros.newGraphName(remap[1]));
      }
    }
  }

  /**
   * Precedence:
   * 
   * <ol>
   * <li>The __ns:= command line argument.</li>
   * <li>The ROS_NAMESPACE environment variable.</li>
   * </ol>
   */
  private NameResolver buildParentResolver() {
    GraphName namespace = DefaultGraphName.createRoot();
    if (specialRemappings.containsKey(CommandLine.ROS_NAMESPACE)) {
      namespace = Ros.newGraphName(specialRemappings.get(CommandLine.ROS_NAMESPACE)).toGlobal();
    } else if (environment.containsKey(EnvironmentVariables.ROS_NAMESPACE)) {
      namespace = Ros.newGraphName(environment.get(EnvironmentVariables.ROS_NAMESPACE)).toGlobal();
    }
    return new DefaultNameResolver(namespace, remappings);
  }

  /**
   * Precedence (default: null):
   * 
   * <ol>
   * <li>The __ip:= command line argument.</li>
   * <li>The ROS_IP environment variable.</li>
   * <li>The ROS_HOSTNAME environment variable.</li>
   * <li>The default host as specified in {@link DefaultNodeConfiguration}.</li>
   * </ol>
   */
  private String getHost() {
    String host = DefaultNodeConfiguration.DEFAULT_HOST;
    if (specialRemappings.containsKey(CommandLine.ROS_IP)) {
      host = specialRemappings.get(CommandLine.ROS_IP);
    } else if (environment.containsKey(EnvironmentVariables.ROS_IP)) {
      host = environment.get(EnvironmentVariables.ROS_IP);
    } else if (environment.containsKey(EnvironmentVariables.ROS_HOSTNAME)) {
      host = environment.get(EnvironmentVariables.ROS_HOSTNAME);
    }
    return host;
  }

  /**
   * Precedence:
   * 
   * <ol>
   * <li>The __master:= command line argument. This is not required but easy to
   * support.</li>
   * <li>The ROS_MASTER_URI environment variable.</li>
   * <li>The default master URI as defined in {@link DefaultNodeConfiguration}.</li>
   * </ol>
   * 
   * @throws RosInitException
   */
  private URI getMasterUri() throws RosInitException {
    String uri = DefaultNodeConfiguration.DEFAULT_MASTER_URI;
    if (specialRemappings.containsKey(CommandLine.ROS_MASTER_URI)) {
      uri = specialRemappings.get(CommandLine.ROS_MASTER_URI);
    } else if (environment.containsKey(EnvironmentVariables.ROS_MASTER_URI)) {
      uri = environment.get(EnvironmentVariables.ROS_MASTER_URI);
    }
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RosInitException("Master URI \"" + uri + "\" is not a valid.");
    }
  }

  private File getRosRoot() {
    if (environment.containsKey(EnvironmentVariables.ROS_ROOT)) {
      return new File(environment.get(EnvironmentVariables.ROS_ROOT));
    } else {
      // For now, this is not required as we are not doing anything (e.g.
      // ClassLoader) that requires it. In the future, this may become required.
      return null;
    }
  }

  private List<String> getRosPackagePath() {
    if (environment.containsKey(EnvironmentVariables.ROS_PACKAGE_PATH)) {
      String path = environment.get(EnvironmentVariables.ROS_PACKAGE_PATH);
      return Lists.newArrayList(path.split(File.pathSeparator));
    } else {
      return Lists.newArrayList();
    }
  }

}
