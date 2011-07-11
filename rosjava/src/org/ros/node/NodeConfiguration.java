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

import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;

import org.ros.Ros;
import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.NameResolver;

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

  public static final URI DEFAULT_MASTER_URI;

  static {
    try {
      DEFAULT_MASTER_URI = new URI("http://localhost:11311/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

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

  public static NodeConfiguration newPublic(String advertiseHostname, URI masterUri) {
    NodeConfiguration configuration = new NodeConfiguration();
    // OS picks an available port.
    configuration.setXmlRpcBindAddress(BindAddress.createPublic(0));
    configuration.setXmlRpcAdvertiseAddress(new AdvertiseAddress(advertiseHostname));
    configuration.setTcpRosBindAddress(BindAddress.createPublic(0));
    configuration.setTcpRosAdvertiseAddress(new AdvertiseAddress(advertiseHostname));
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPublic(String advertiseHostname) {
    return newPublic(advertiseHostname, DEFAULT_MASTER_URI);
  }

  public static NodeConfiguration newPrivate(URI masterUri) {
    NodeConfiguration configuration = new NodeConfiguration();
    // OS picks an available port.
    configuration.setXmlRpcBindAddress(BindAddress.createPrivate(0));
    configuration.setXmlRpcAdvertiseAddress(AdvertiseAddress.createPrivate());
    configuration.setTcpRosBindAddress(BindAddress.createPrivate(0));
    configuration.setTcpRosAdvertiseAddress(AdvertiseAddress.createPrivate());
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPrivate() {
    return newPrivate(DEFAULT_MASTER_URI);
  }

  private NodeConfiguration() {
    setMessageSerializationFactory(new org.ros.internal.message.old_style.MessageSerializationFactory());
    setParentResolver(Ros.newNameResolver());
  }

  /**
   * @return The {@link DefaultNameResolver} for a {@link Node}'s parent
   *         namespace.
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

  public BindAddress getTcpRosBindAddress() {
    return tcpRosBindAddress;
  }

  public void setTcpRosBindAddress(BindAddress tcpRosBindAddress) {
    this.tcpRosBindAddress = tcpRosBindAddress;
  }

  public AdvertiseAddress getTcpRosAdvertiseAddress() {
    return tcpRosAdvertiseAddress;
  }

  public void setTcpRosAdvertiseAddress(AdvertiseAddress tcpRosAdvertiseAddress) {
    this.tcpRosAdvertiseAddress = tcpRosAdvertiseAddress;
  }

  public BindAddress getXmlRpcBindAddress() {
    return xmlRpcBindAddress;
  }

  public void setXmlRpcBindAddress(BindAddress xmlRpcBindAddress) {
    this.xmlRpcBindAddress = xmlRpcBindAddress;
  }

  public AdvertiseAddress getXmlRpcAdvertiseAddress() {
    return xmlRpcAdvertiseAddress;
  }

  public void setXmlRpcAdvertiseAddress(AdvertiseAddress xmlRpcAdvertiseAddress) {
    this.xmlRpcAdvertiseAddress = xmlRpcAdvertiseAddress;
  }

}
