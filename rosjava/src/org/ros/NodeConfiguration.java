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

import org.ros.internal.namespace.DefaultNameResolver;

import org.ros.namespace.NameResolver;

import org.ros.internal.node.address.Address;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Stores contextual information about a ROS node, including common ROS
 * configuration like the master URI.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeConfiguration {

  public static final String DEFAULT_HOST = Address.LOCALHOST;
  public static final String DEFAULT_MASTER_URI = "http://" + DEFAULT_HOST + ":11311/";

  private NameResolver parentResolver;
  private URI masterUri;
  private String host;
  private File rosRoot;
  private List<String> rosPackagePath;
  private int tcpRosPort;
  private int xmlRpcPort;
  private String nodeNameOverride;
  private MessageSerializationFactory messageSerializationFactory;

  public static NodeConfiguration createDefault() {
    NodeConfiguration configuration = new NodeConfiguration();
    try {
      configuration.setMasterUri(new URI(DEFAULT_MASTER_URI));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    configuration.setParentResolver(DefaultNameResolver.createDefault());
    configuration.setHost(DEFAULT_HOST);
    configuration.setTcpRosPort(0); // OS determined free port.
    configuration.setXmlRpcPort(0); // OS determined free port.
    configuration.setMessageSerializationFactory(new org.ros.message.MessageSerializationFactory());
    return configuration;
  }

  /**
   * @return The {@link DefaultNameResolver} for a {@link Node}'s parent namespace.
   */
  public NameResolver getParentResolver() {
    return parentResolver;
  }

  public void setParentResolver(NameResolver resolver) {
    this.parentResolver = resolver;
  }

  public URI getMasterUri() {
    return masterUri;
  }

  public void setMasterUri(URI masterUri) {
    this.masterUri = masterUri;
  }

  public File getRosRoot() {
    return rosRoot;
  }

  public void setRosRoot(File rosRoot) {
    this.rosRoot = rosRoot;
  }

  public List<String> getRosPackagePath() {
    return rosPackagePath;
  }

  public void setRosPackagePath(List<String> rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
  }

  /**
   * @return host name/address to use when advertising this node via URLs.
   */
  public String getHost() {
    return host;
  }

  /**
   * Set host name/address to use when advertising this node via URLs.
   * 
   * @param host
   */
  public void setHost(String host) {
    this.host = host;
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

  public MessageSerializationFactory getMessageSerializationFactory() {
    return messageSerializationFactory;
  }

  public void
      setMessageSerializationFactory(MessageSerializationFactory messageSerializationFactory) {
    this.messageSerializationFactory = messageSerializationFactory;
  }

}
