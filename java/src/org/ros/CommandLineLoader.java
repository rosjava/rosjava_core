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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.ros.exceptions.RosInitException;
import org.ros.internal.loader.CommandLine;
import org.ros.internal.loader.EnvironmentVariables;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create {@link NodeContext} instances using a ROS command-line and environment
 * specification.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class CommandLineLoader extends RosLoader {

  private final List<String> argv;
  private final List<String> nodeArguments;
  private final List<String> nodeRemappings;
  private final Map<String, String> environment;

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments.
   * Environment variables will be pulled from default {@link System}
   * environment variables.
   * 
   * @param argv command-line arguments
   */
  public CommandLineLoader(List<String> argv) {
    this(argv, System.getenv());
  }

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments
   * and environment variables.
   * 
   * @param argv command-line arguments
   * @param env environment variables
   */
  public CommandLineLoader(List<String> argv, Map<String, String> env) {
    Preconditions.checkArgument(argv.size() > 0);
    this.argv = argv;
    this.environment = env;
    nodeArguments = Lists.newArrayList();
    nodeRemappings = Lists.newArrayList();
    for (String argument : argv.subList(1, argv.size())) {
      if (argument.contains(":=")) {
        nodeRemappings.add(argument);
      } else {
        nodeArguments.add(argument);
      }
    }
  }

  public String getNodeClassName() {
    return argv.get(0);
  }

  // TODO(damonkohler): The NodeContext should possibly have access to
  // remappings and arguments.
  /**
   * Create NodeContext according to ROS command-line and environment
   * specification.
   */
  @Override
  public NodeContext createContext() throws RosInitException {
    Map<String, String> specialRemappings = getSpecialRemappings();
    String namespace = getNamespace(specialRemappings, environment);
    Map<GraphName, GraphName> remappings = parseRemappings();
    NameResolver resolver = new NameResolver(namespace, remappings);

    NodeContext context = new NodeContext();
    context.setParentResolver(resolver);
    context.setRosRoot(getRosRoot(specialRemappings, environment));
    context.setRosPackagePath(getRosPackagePath(specialRemappings, environment));
    context.setRosMasterUri(getRosMasterUri(specialRemappings, environment));

    if (specialRemappings.containsKey(CommandLine.NODE_NAME)) {
      context.setNodeNameOverride(specialRemappings.get(CommandLine.NODE_NAME));
    }
    String addressOverride = getAddressOverride(specialRemappings, environment);
    if (addressOverride != null) {
      context.setHostName(addressOverride);
    } else {
      try {
        context.setHostName(InetAddress.getLocalHost().getCanonicalHostName());
      } catch (UnknownHostException e) {
        throw new RosInitException(e);
      }
    }
    return context;
  }

  /**
   * @return command-line arguments without ROS remapping arguments
   */
  public List<String> getNodeArguments() {
    return Collections.unmodifiableList(nodeArguments);
  }

  /**
   * @return the original command-line node arguments
   */
  public List<String> getArgv() {
    return Collections.unmodifiableList(argv);
  }

  private Map<String, String> getSpecialRemappings() {
    HashMap<String, String> specialRemappings = new HashMap<String, String>();
    for (String remapping : nodeRemappings) {
      Preconditions.checkState(remapping.contains(":="));
      if (remapping.startsWith("__")) {
        String[] remap = remapping.split(":=");
        if (remap.length > 2) {
          throw new IllegalArgumentException("Invalid remapping argument: " + remapping);
        }
        specialRemappings.put(remap[0], remap[1]);
      }
    }
    return specialRemappings;
  }

  private String getAddressOverride(Map<String, String> specialRemappings, Map<String, String> env) {
    // Precedence (default: null):
    //
    // 1) The __ip:= command line argument.
    // 2) the ROS_IP environment variable.
    // 3) the ROS_HOSTNAME environment variable.

    String address = null;
    if (specialRemappings.containsKey(CommandLine.ROS_IP)) {
      address = specialRemappings.get(CommandLine.ROS_IP);
    } else if (env.containsKey(EnvironmentVariables.ROS_IP)) {
      address = env.get(EnvironmentVariables.ROS_IP);
    } else if (env.containsKey(EnvironmentVariables.ROS_HOSTNAME)) {
      address = env.get(EnvironmentVariables.ROS_HOSTNAME);
    }
    return address;
  }

  /**
   * Precedence:
   * 
   * <ol>
   * <li>The __ns:= command line argument.</li>
   * <li>The ROS_NAMESPACE environment variable.</li>
   * </ol>
   * 
   * @param specialRemappings
   * @param env
   */
  private String getNamespace(Map<String, String> specialRemappings, Map<String, String> env) {

    String namespace = Namespace.GLOBAL_NS;
    if (specialRemappings.containsKey(CommandLine.ROS_NAMESPACE)) {
      namespace =
          new GraphName(specialRemappings.get(CommandLine.ROS_NAMESPACE)).toGlobal().toString();
    } else if (env.containsKey(EnvironmentVariables.ROS_NAMESPACE)) {
      namespace = new GraphName(env.get(EnvironmentVariables.ROS_NAMESPACE)).toGlobal().toString();
    }
    return namespace;
  }

  private URI getRosMasterUri(Map<String, String> specialRemappings, Map<String, String> env)
      throws RosInitException {
    // Precedence:
    //
    // 1) The __master:= command line argument.
    // 2) the ROS_MASTER_URI environment variable.
    //
    // (1) is not required, but it is easy to support.

    String uriValue = null;
    try {
      if (specialRemappings.containsKey(CommandLine.ROS_MASTER_URI)) {
        uriValue = specialRemappings.get(CommandLine.ROS_MASTER_URI);
        return new URI(uriValue);
      } else if (env.containsKey(EnvironmentVariables.ROS_MASTER_URI)) {
        uriValue = env.get(EnvironmentVariables.ROS_MASTER_URI);
        return new URI(uriValue);
      } else {
        throw new RosInitException("ROS_MASTER_URI is not set");
      }
    } catch (URISyntaxException e) {
      throw new RosInitException("ROS_MASTER_URI [" + uriValue + "] is not a valid URI");
    }
  }

  private String getRosRoot(Map<String, String> specialRemappings, Map<String, String> env) {
    if (env.containsKey(EnvironmentVariables.ROS_ROOT)) {
      return env.get(EnvironmentVariables.ROS_ROOT);
    } else {
      // For now, this is not required as we are not doing anything (e.g.
      // ClassLoader) that requires it. In the future, this may become required.
      return null;
    }
  }

  private List<String> getRosPackagePath(Map<String, String> specialRemappings,
      Map<String, String> env) {
    if (env.containsKey(EnvironmentVariables.ROS_PACKAGE_PATH)) {
      String path = env.get(EnvironmentVariables.ROS_PACKAGE_PATH);
      return Lists.newArrayList(path.split(File.pathSeparator));
    } else {
      return Lists.newArrayList();
    }
  }

  private HashMap<GraphName, GraphName> parseRemappings() {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    for (String remapping : nodeRemappings) {
      Preconditions.checkState(remapping.contains(":="));
      if (!remapping.startsWith("__")) {
        String[] remap = remapping.split(":=");
        if (remap.length > 2) {
          throw new IllegalArgumentException("Invalid remapping argument: " + remapping);
        }
        remappings.put(new GraphName(remap[0]), new GraphName(remap[1]));
      }
    }
    return remappings;
  }

}
