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

import org.ros.exceptions.RosInitException;

import org.ros.exceptions.RosNameException;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Create {@link NodeContext} instances using a ROS command-line and environment
 * specification.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class CommandLineLoader extends RosLoader {

  private final String[] cleanCommandLineArgs;
  private final String[] commandLineArgs;
  private final Map<String, String> environment;

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments.
   * Environment variables will be pulled from default {@link System}
   * environment variables.
   * 
   * @param commandLineArgs command-line arguments
   */
  public CommandLineLoader(String[] commandLineArgs) {
    this(commandLineArgs, System.getenv());
  }

  /**
   * Create new {@link CommandLineLoader} with specified command-line arguments
   * and environment variables.
   * 
   * @param commandLineArgs command-line arguments
   * @param env environment variables
   */
  public CommandLineLoader(String[] commandLineArgs, Map<String, String> env) {
    this.environment = env;
    this.commandLineArgs = commandLineArgs;

    LinkedList<String> clean = new LinkedList<String>();
    for (String x : commandLineArgs) {
      if (!x.contains(":=")) {
        clean.add(x);
      }
    }
    cleanCommandLineArgs = clean.toArray(new String[0]);
  }

  /**
   * Create NodeContext according to ROS command-line and environment
   * specification.
   */
  @Override
  public NodeContext createContext() throws RosInitException {
    try {
      Map<String, String> specialRemappings = getSpecialRemappings(commandLineArgs);
      String namespace = getNamespace(specialRemappings, environment);
      HashMap<GraphName, GraphName> remappings = parseRemappings(commandLineArgs);
      NameResolver resolver = new NameResolver(namespace, remappings);

      NodeContext context = new NodeContext();
      context.setParentResolver(resolver);
      context.setRosRoot(getRosRoot(specialRemappings, environment));
      context.setRosPackagePath(getRosPackagePath(specialRemappings, environment));
      context.setRosMasterUri(getRosMasterUri(specialRemappings, environment));
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
    } catch (RosNameException e) {
      throw new RosInitException(e);
    }
  }

  /**
   * @return command-line arguments without ROS remapping arguments
   */
  public String[] getCleanCommandLineArgs() {
    return cleanCommandLineArgs;
  }

  /**
   * @return the original command-line arguments
   */
  public String[] getCommandLineArgs() {
    return commandLineArgs;
  }

  private Map<String, String> getSpecialRemappings(String[] commandLineArgs) {
    HashMap<String, String> specialRemappings = new HashMap<String, String>();
    for (String arg : commandLineArgs) {
      if (arg.contains(":=") && arg.startsWith("__")) {
        String[] remap = arg.split(":=");
        if (remap.length > 2) {
          throw new IllegalArgumentException("invalid command-line args");
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

  private String getNamespace(Map<String, String> specialRemappings, Map<String, String> env)
      throws RosNameException {
    // Precedence:
    //
    // 1) The __ns:= command line argument.
    // 2) the ROS_NAMESPACE environment variable.

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

  private String[] getRosPackagePath(Map<String, String> specialRemappings, Map<String, String> env) {
    if (env.containsKey(EnvironmentVariables.ROS_PACKAGE_PATH)) {
      String path = env.get(EnvironmentVariables.ROS_PACKAGE_PATH);
      return path.split(File.pathSeparator);
    } else {
      return new String[] {};
    }
  }

  private HashMap<GraphName, GraphName> parseRemappings(String[] commandLineArgs)
      throws RosNameException {
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    for (String arg : commandLineArgs) {
      if (arg.contains(":=") && !arg.startsWith("__")) {
        String[] remap = arg.split(":=");
        if (remap.length > 2) {
          throw new IllegalArgumentException("invalid command-line args");
        }
        remappings.put(new GraphName(remap[0]), new GraphName(remap[1]));
      }
    }
    return remappings;
  }

}
