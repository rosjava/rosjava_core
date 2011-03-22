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

import org.ros.namespace.NameResolver;

import java.net.URI;

/**
 * Stores contextual information about a ROS node, including common ROS
 * configuration like the master URI.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeContext {
  private NameResolver resolver;
  private URI rosMasterUri;
  private String hostName;
  private String rosRoot;
  private String[] rosPackagePath;
  private int tcpRosPort;
  private int xmlRpcPort;
  private String nodeNameOverride;

  /**
   * default context
   */
  public NodeContext() {
    resolver = null;
    tcpRosPort = 0;
    xmlRpcPort = 0;
  }

  /**
   * @return The {@link NameResolver} for a {@link Node}'s parent namespace.
   * @see NameResolver
   */
  public NameResolver getParentResolver() {
    return resolver;
  }

  public void setParentResolver(NameResolver resolver) {
    this.resolver = resolver;
  }

  public URI getRosMasterUri() {
    return rosMasterUri;
  }

  public void setRosMasterUri(URI rosMasterUri) {
    this.rosMasterUri = rosMasterUri;
  }

  public String getRosRoot() {
    return rosRoot;
  }

  public void setRosRoot(String rosRoot) {
    this.rosRoot = rosRoot;
  }

  public void setRosRoot(String[] rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
  }

  public String[] getRosPackagePath() {
    return rosPackagePath;
  }

  public void setRosPackagePath(String[] rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
  }

  /**
   * @return host name/address to use when advertising this node via URLs.
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * Set host name/address to use when advertising this node via URLs.
   * 
   * @param hostName
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * @return Port to bind TCPROS server to, or 0 to bind to any open port.
   */
  public int getTcpRosPort() {
    return tcpRosPort;
  }

  /**
   * Set port to bind TCPROS server to. 0 binds to any open port.
   */
  public void setTcpRosPort(int tcpRosPort) {
    this.tcpRosPort = tcpRosPort;
  }

  /**
   * @return Port to bind XMLRPC server to, or 0 to bind to any open port.
   */
  public int getXmlRpcPort() {
    return xmlRpcPort;
  }

  /**
   * Set port to bind XMLRPC server to. 0 binds to any open port.
   */
  public void setXmlRpcPort(int xmlRpcPort) {
    this.xmlRpcPort = xmlRpcPort;
  }

  /**
   * @return Override for Node name or null if no override.
   */
  public String getNodeNameOverride() {
    return nodeNameOverride;
  }

  public void setNodeNameOverride(String nodeNameOverride) {
    this.nodeNameOverride = nodeNameOverride;
  }

}
