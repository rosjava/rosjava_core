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

package org.ros.internal.node;

import org.ros.Ros;
import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.NameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;

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
public class DefaultNodeConfiguration implements NodeConfiguration {

  private NameResolver parentResolver;
  private URI masterUri;
  private File rosRoot;
  private List<String> rosPackagePath;
  private String nodeNameOverride;
  private MessageSerializationFactory messageSerializationFactory;
  private BindAddress tcpRosBindAddress;
  private AdvertiseAddress tcpRosAdvertiseAddress;
  private BindAddress xmlRpcBindAddress;
  private AdvertiseAddress xmlRpcAdvertiseAddress;

  public static NodeConfiguration newPublic(String advertiseHostname,
      int xmlRpcBindPort, int tcpRosBindPort) {
    NodeConfiguration configuration = new DefaultNodeConfiguration();
    configuration.setXmlRpcBindAddress(BindAddress.createPublic(xmlRpcBindPort));
    configuration.setXmlRpcAdvertiseAddress(new AdvertiseAddress(advertiseHostname));
    configuration.setTcpRosBindAddress(BindAddress.createPublic(tcpRosBindPort));
    configuration.setTcpRosAdvertiseAddress(new AdvertiseAddress(advertiseHostname));
    return configuration;
  }

  public static NodeConfiguration newPrivate(int xmlRpcBindPort, int tcpRosBindPort) {
    NodeConfiguration configuration = new DefaultNodeConfiguration();
    configuration.setXmlRpcBindAddress(BindAddress.createPrivate(xmlRpcBindPort));
    configuration.setXmlRpcAdvertiseAddress(AdvertiseAddress.createPrivate());
    configuration.setTcpRosBindAddress(BindAddress.createPrivate(tcpRosBindPort));
    configuration.setTcpRosAdvertiseAddress(AdvertiseAddress.createPrivate());
    return configuration;
  }

  private DefaultNodeConfiguration() {
    try {
      setMasterUri(new URI(NodeConfiguration.DEFAULT_MASTER_URI));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    setParentResolver(Ros.newNameResolver());
  }

  /**
   * @return The {@link DefaultNameResolver} for a {@link Node}'s parent
   *         namespace.
   */
  @Override
  public NameResolver getParentResolver() {
    return parentResolver;
  }

  @Override
  public void setParentResolver(NameResolver resolver) {
    this.parentResolver = resolver;
  }

  @Override
  public URI getMasterUri() {
    return masterUri;
  }

  @Override
  public void setMasterUri(URI masterUri) {
    this.masterUri = masterUri;
  }

  @Override
  public File getRosRoot() {
    return rosRoot;
  }

  @Override
  public void setRosRoot(File rosRoot) {
    this.rosRoot = rosRoot;
  }

  @Override
  public List<String> getRosPackagePath() {
    return rosPackagePath;
  }

  @Override
  public void setRosPackagePath(List<String> rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
  }

  /**
   * @return Override for Node name or null if no override.
   */
  @Override
  public String getNodeNameOverride() {
    return nodeNameOverride;
  }

  @Override
  public void setNodeNameOverride(String nodeNameOverride) {
    this.nodeNameOverride = nodeNameOverride;
  }

  @Override
  public MessageSerializationFactory getMessageSerializationFactory() {
    return messageSerializationFactory;
  }

  @Override
  public void
      setMessageSerializationFactory(MessageSerializationFactory messageSerializationFactory) {
    this.messageSerializationFactory = messageSerializationFactory;
  }

  @Override
  public BindAddress getTcpRosBindAddress() {
    return tcpRosBindAddress;
  }

  @Override
  public void setTcpRosBindAddress(BindAddress tcpRosBindAddress) {
    this.tcpRosBindAddress = tcpRosBindAddress;
  }

  @Override
  public AdvertiseAddress getTcpRosAdvertiseAddress() {
    return tcpRosAdvertiseAddress;
  }

  @Override
  public void setTcpRosAdvertiseAddress(AdvertiseAddress tcpRosAdvertiseAddress) {
    this.tcpRosAdvertiseAddress = tcpRosAdvertiseAddress;
  }

  @Override
  public BindAddress getXmlRpcBindAddress() {
    return xmlRpcBindAddress;
  }

  @Override
  public void setXmlRpcBindAddress(BindAddress xmlRpcBindAddress) {
    this.xmlRpcBindAddress = xmlRpcBindAddress;
  }

  @Override
  public AdvertiseAddress getXmlRpcAdvertiseAddress() {
    return xmlRpcAdvertiseAddress;
  }

  @Override
  public void setXmlRpcAdvertiseAddress(AdvertiseAddress xmlRpcAdvertiseAddress) {
    this.xmlRpcAdvertiseAddress = xmlRpcAdvertiseAddress;
  }

}
