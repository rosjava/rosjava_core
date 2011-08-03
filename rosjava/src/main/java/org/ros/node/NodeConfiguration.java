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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.ros.address.AdvertiseAddress;
import org.ros.address.AdvertiseAddressFactory;
import org.ros.address.BindAddress;
import org.ros.address.PrivateAdvertiseAddressFactory;
import org.ros.address.PublicAdvertiseAddressFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.message.MessageDefinitionFactory;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;

/**
 * Stores configuration information (e.g. ROS master URI) for {@link Node}s.
 * 
 * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
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

  /**
   * @param nodeConfiguration
   *          the {@link NodeConfiguration} to copy
   * @return a copy of the supplied {@link NodeConfiguration}
   */
  public static NodeConfiguration copyOf(NodeConfiguration nodeConfiguration) {
    NodeConfiguration copy = new NodeConfiguration();
    copy.parentResolver = nodeConfiguration.parentResolver;
    copy.masterUri = nodeConfiguration.masterUri;
    copy.rosRoot = nodeConfiguration.rosRoot;
    copy.rosPackagePath = nodeConfiguration.rosPackagePath;
    copy.nodeName = nodeConfiguration.nodeName;
    copy.messageSerializationFactory = nodeConfiguration.messageSerializationFactory;
    copy.tcpRosBindAddress = nodeConfiguration.tcpRosBindAddress;
    copy.tcpRosAdvertiseAddressFactory = nodeConfiguration.tcpRosAdvertiseAddressFactory;
    copy.xmlRpcBindAddress = nodeConfiguration.xmlRpcBindAddress;
    copy.xmlRpcAdvertiseAddressFactory = nodeConfiguration.xmlRpcAdvertiseAddressFactory;
    return copy;
  }

  private NameResolver parentResolver;
  private URI masterUri;
  private File rosRoot;
  private List<File> rosPackagePath;
  private GraphName nodeName;
  private MessageFactory messageFactory;
  private MessageDefinitionFactory messageDefinitionFactory;
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
    setMessageFactory(new org.ros.internal.message.old_style.MessageFactory());
    setMessageDefinitionFactory(new org.ros.internal.message.old_style.MessageDefinitionFactory());
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
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setParentResolver(NameResolver resolver) {
    this.parentResolver = resolver;
    return this;
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
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setMasterUri(URI masterUri) {
    this.masterUri = masterUri;
    return this;
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
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setRosRoot(File rosRoot) {
    this.rosRoot = rosRoot;
    return this;
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
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setRosPackagePath(List<File> rosPackagePath) {
    this.rosPackagePath = rosPackagePath;
    return this;
  }

  /**
   * @return the name of the {@link Node}
   */
  public GraphName getNodeName() {
    return nodeName;
  }

  /**
   * @param nodeName
   *          the name of the {@link Node}
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setNodeName(GraphName nodeName) {
    this.nodeName = nodeName;
    return this;
  }

  /**
   * @param nodeName
   *          the name of the {@link Node}
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setNodeName(String nodeName) {
    return setNodeName(new GraphName(nodeName));
  }

  /**
   * Sets the name of the {@link Node} if the name has not already been set.
   * 
   * @param nodeName
   *          the name of the {@link Node}
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setDefaultNodeName(GraphName nodeName) {
    if (this.nodeName == null) {
      setNodeName(nodeName);
    }
    return this;
  }

  /**
   * Sets the name of the {@link Node} if the name has not already been set.
   * 
   * @param nodeName
   *          the name of the {@link Node}
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setDefaultNodeName(String nodeName) {
    return setDefaultNodeName(new GraphName(nodeName));
  }

  /**
   * @return the {@link MessageSerializationFactory} for the {@link Node}
   */
  public MessageSerializationFactory getMessageSerializationFactory() {
    return messageSerializationFactory;
  }

  /**
   * @param messageSerializationFactory
   *          the {@link MessageSerializationFactory} for the {@link Node}
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setMessageSerializationFactory(
      MessageSerializationFactory messageSerializationFactory) {
    this.messageSerializationFactory = messageSerializationFactory;
    return this;
  }

  public void setMessageFactory(MessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }

  public MessageFactory getMessageFactory() {
    return messageFactory;
  }

  public void setMessageDefinitionFactory(MessageDefinitionFactory messageDefinitionFactory) {
    this.messageDefinitionFactory = messageDefinitionFactory;
  }

  public MessageDefinitionFactory getMessageDefinitionFactory() {
    return messageDefinitionFactory;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/TCPROS
   * 
   * @return the {@link BindAddress} for the {@link Node}'s TCPROS server
   */
  public BindAddress getTcpRosBindAddress() {
    return tcpRosBindAddress;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/TCPROS
   * 
   * @param tcpRosBindAddress
   *          the {@link BindAddress} for the {@link Node}'s TCPROS server
   */
  public NodeConfiguration setTcpRosBindAddress(BindAddress tcpRosBindAddress) {
    this.tcpRosBindAddress = tcpRosBindAddress;
    return this;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/TCPROS
   * 
   * @return the {@link AdvertiseAddressFactory} for the {@link Node}'s TCPROS
   *         server
   */
  public AdvertiseAddressFactory getTcpRosAdvertiseAddressFactory() {
    return tcpRosAdvertiseAddressFactory;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/TCPROS
   * 
   * @param tcpRosAdvertiseAddressFactory
   *          the {@link AdvertiseAddressFactory} for the {@link Node}'s TCPROS
   *          server
   * @return this {@link NodeConfiguration}
   */
  public NodeConfiguration setTcpRosAdvertiseAddressFactory(
      AdvertiseAddressFactory tcpRosAdvertiseAddressFactory) {
    this.tcpRosAdvertiseAddressFactory = tcpRosAdvertiseAddressFactory;
    return this;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/TCPROS
   * 
   * @return the {@link AdvertiseAddress} for the {@link Node}'s TCPROS server
   */
  public AdvertiseAddress getTcpRosAdvertiseAddress() {
    return tcpRosAdvertiseAddressFactory.create();
  }

  /**
   * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
   * 
   * @return the {@link BindAddress} for the {@link Node}'s XML-RPC server
   */
  public BindAddress getXmlRpcBindAddress() {
    return xmlRpcBindAddress;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
   * 
   * @param xmlRpcBindAddress
   *          the {@link BindAddress} for the {@link Node}'s XML-RPC server
   */
  public NodeConfiguration setXmlRpcBindAddress(BindAddress xmlRpcBindAddress) {
    this.xmlRpcBindAddress = xmlRpcBindAddress;
    return this;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
   * 
   * @return the {@link AdvertiseAddress} for the {@link Node}'s XML-RPC server
   */
  public AdvertiseAddress getXmlRpcAdvertiseAddress() {
    return xmlRpcAdvertiseAddressFactory.create();
  }

  /**
   * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
   * 
   * @return the {@link AdvertiseAddressFactory} for the {@link Node}'s XML-RPC
   *         server
   */
  public AdvertiseAddressFactory getXmlRpcAdvertiseAddressFactory() {
    return xmlRpcAdvertiseAddressFactory;
  }

  /**
   * @see http://www.ros.org/wiki/ROS/Technical%20Overview#Node
   * 
   * @param xmlRpcAdvertiseAddressFactory
   *          the {@link AdvertiseAddressFactory} for the {@link Node}'s XML-RPC
   *          server
   */
  public NodeConfiguration setXmlRpcAdvertiseAddressFactory(
      AdvertiseAddressFactory xmlRpcAdvertiseAddressFactory) {
    this.xmlRpcAdvertiseAddressFactory = xmlRpcAdvertiseAddressFactory;
    return this;
  }

}
