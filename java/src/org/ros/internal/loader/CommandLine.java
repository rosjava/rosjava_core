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
package org.ros.internal.loader;

/**
 * Remapping keys used to override ROS environment and other configuration
 * settings. As of ROS 1.4, only __ns is required to be supported.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public interface CommandLine {

  public static String ROS_NAMESPACE = "__ns";
  public static String ROS_IP = "__ip";
  public static String ROS_MASTER_URI = "__master";
  public static String TCPROS_PORT = "__tcpros_server_port";

}
