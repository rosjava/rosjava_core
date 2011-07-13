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
import org.ros.address.AdvertiseAddressFactory;
import org.ros.address.BindAddress;
import org.ros.address.PrivateAdvertiseAddressFactory;
import org.ros.address.PublicAdvertiseAddressFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.NameResolver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Stores configuration information (e.g. ROS master URI) for {@link Node}s.
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeConfiguration {

  /**
   * The default master URI.
   */
  public static final URI DEFAULT_MASTER_URI;

  static {
    try {
      DEFAULT_MASTER_URI = new URI("http://localhost:11311/");
    } catch (URISyntaxException e) {
      throw new RosRuntimeException(e);
    }
  }

  private NameResolver parentResolver;
  private URI masterUri;
  private File rosRoot;
  private List<File> rosPackagePath;
  private String nodeNameOverride;
  private MessageSerializationFactory messageSerializationFactory;
  private BindAddress tcpRosBindAddress;
  private AdvertiseAddressFactory tcpRosAdvertiseAddressFactory;
  private BindAddress xmlRpcBindAddress;
  private AdvertiseAddressFactory xmlRpcAdvertiseAddressFactory;

  /**
   * Creates a new {@link NodeConfiguration} for a publicly accessible
   * {@link Node}.
   * 
   * @param host
   *          the host that the {@link Node} will run on
   * @param masterUri
   *          the {@link URI} for the master that the {@link Node} will register
   *          with
   * @return a new {@link NodeConfiguration} for a publicly accessible
   *         {@link Node}
   */
  public static NodeConfiguration newPublic(String host, URI masterUri) {
    NodeConfiguration configuration = new NodeConfiguration();
    configuration.setXmlRpcBindAddress(BindAddress.newPublic());
    configuration.setXmlRpcAdvertiseAddressFactory(new PublicAdvertiseAddressFactory(host));
    configuration.setTcpRosBindAddress(BindAddress.newPublic());
    configuration.setTcpRosAdvertiseAddressFactory(new PublicAdvertiseAddressFactory(host));
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  /**
   * Creates a new {@link NodeConfiguration} for a publicly accessible
   * {@link Node}.
   * 
   * @param host
   *          the host that the {@link Node} will run on
   * @return a new {@link NodeConfiguration} for a publicly accessible
   *         {@link Node}
   */
  public static NodeConfiguration newPublic(String host) {
    return newPublic(host, DEFAULT_MASTER_URI);
  }

  /**
   * Creates a new {@link NodeConfiguration} for a {@link Node} that is only
   * accessible on the local host.
   * 
   * @param masterUri
   *          the {@link URI} for the master that the {@link Node} will register
   *          with
   * @return a new {@link NodeConfiguration} for a private {@link Node}
   */
  public static NodeConfiguration newPrivate(URI masterUri) {
    NodeConfiguration configuration = new NodeConfiguration();
    configuration.setXmlRpcBindAddress(BindAddress.newPrivate());
    configuration.setXmlRpcAdvertiseAddressFactory(new PrivateAdvertiseAddressFactory());
    configuration.setTcpRosBindAddress(BindAddress.newPrivate());
    configuration.setTcpRosAdvertiseAddressFactory(new PrivateAdvertiseAddressFactory());
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  /**
   * Creates a new {@link NodeConfiguration} for a {@link Node} that is only
   * accessible on the local host.
   * 
   * @return a new {@link NodeConfiguration} for a private {@link Node}
   */
  public static NodeConfiguration newPrivate() {
    return newPrivate(DEFAULT_MASTER_URI);
  }

  private NodeConfiguration() {
    setMessageSerializationFactory(new org.ros.internal.message.old_style.MessageSerializationFactory());
    setParentResolver(NameResolver.create());
  }

  /**
   * @return the {@link NameResolver} for the {@link Node}'s parent namespace
   */
  public NameResolver getParentResolver() {
    return parentResolver;
  }

  /**
   * @param resolver
   *          the {@link NameResolver} for the {@link Node}'s parent namespace
   */
  public void setParentResolver(NameResolver resolver) {
    this.parentResolver = resolver;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_MASTER_URI
   * @return the {@link URI} of the master that the {@link Node} will register
   *         with
   */
  public URI getMasterUri() {
    return masterUri;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_MASTER_URI
   * @param masterUri
   *          the {@link URI} of the master that the {@link Node} will register
   *          with
   */
  public void setMasterUri(URI masterUri) {
    this.masterUri = masterUri;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_ROOT
   * @return the location where the ROS core packages are installed
   */
  public File getRosRoot() {
    return rosRoot;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_ROOT
   * @param rosRoot
   *          the location where the ROS core packages are installed
   */
  public void setRosRoot(File rosRoot) {
    this.rosRoot = rosRoot;
  }

  /**
   * These ordered paths tell the ROS system where to search for more ROS
   * packages. If there are multiple packages of the same name, ROS will choose
   * the one that appears in the {@link List} first.
   * 
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_PACKAGE_PATH
   * @return the {@link List} of paths where the system will look for ROS
   *         packages
   */
  public List<File> getRosPackagePath() {
    return rosPackagePath;
  }

  /**
   * These ordered paths tell the ROS system where to search for more ROS
   * packages. If there are multiple packages of the same name, ROS will choose
   * the one that appears in the {@link List} first.
   * 
   * @see http://www.ros.org/wiki/ROS/EnvironmentVariables#ROS_PACKAGE_PATH
   * @param rosPackagePath
   *          the {@link List} of paths where the system will look for ROS
   *          packages
   */
  public void setRosPackagePath(List<File> rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
  }

  /**
   * @return the override for the name of the {@link Node}
   */
  public String getNodeNameOverride() {
    return nodeNameOverride;
  }

  /**
   * @param nodeNameOverride
   *          the override for the name of the {@link Node}
   */
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

  public AdvertiseAddressFactory getTcpRosAdvertiseAddressFactory() {
    return tcpRosAdvertiseAddressFactory;
  }

  public void
      setTcpRosAdvertiseAddressFactory(AdvertiseAddressFactory tcpRosAdvertiseAddressFactory) {
    this.tcpRosAdvertiseAddressFactory = tcpRosAdvertiseAddressFactory;
  }

  public AdvertiseAddress getTcpRosAdvertiseAddress() {
    return tcpRosAdvertiseAddressFactory.create();
  }

  public BindAddress getXmlRpcBindAddress() {
    return xmlRpcBindAddress;
  }

  public void setXmlRpcBindAddress(BindAddress xmlRpcBindAddress) {
    this.xmlRpcBindAddress = xmlRpcBindAddress;
  }

  public AdvertiseAddress getXmlRpcAdvertiseAddress() {
    return xmlRpcAdvertiseAddressFactory.create();
  }

  public AdvertiseAddressFactory getXmlRpcAdvertiseAddressFactory() {
    return xmlRpcAdvertiseAddressFactory;
  }

  public void
      setXmlRpcAdvertiseAddressFactory(AdvertiseAddressFactory xmlRpcAdvertiseAddressFactory) {
    this.xmlRpcAdvertiseAddressFactory = xmlRpcAdvertiseAddressFactory;
  }

}
