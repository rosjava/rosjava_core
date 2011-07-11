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

import org.ros.internal.namespace.DefaultGraphName;
import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.internal.node.DefaultNode;
import org.ros.internal.node.DefaultNodeConfiguration;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Ros {

  private Ros() {
    // Utility class
  }

  public static NodeConfiguration newPublicNodeConfiguration(String host,
      MessageSerializationFactory messageSerializationFactory) {
    // OS picks an available port.
    NodeConfiguration configuration = DefaultNodeConfiguration.newPublic(host, 0, 0);
    configuration.setMessageSerializationFactory(messageSerializationFactory);
    return configuration;
  }

  public static NodeConfiguration newPublicNodeConfiguration(URI masterUri, String host,
      MessageSerializationFactory messageSerializationFactory) {
    NodeConfiguration configuration = newPublicNodeConfiguration(host, messageSerializationFactory);
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPublicNodeConfiguration(String host, URI masterUri) {
    NodeConfiguration configuration =
        newPublicNodeConfiguration(host,
            new org.ros.internal.message.old_style.MessageSerializationFactory());
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPublicNodeConfiguration(String host) {
    return newPublicNodeConfiguration(host,
        new org.ros.internal.message.old_style.MessageSerializationFactory());
  }

  public static NodeConfiguration newPrivateNodeConfiguration(
      MessageSerializationFactory messageSerializationFactory) {
    // OS picks an available port.
    NodeConfiguration configuration = DefaultNodeConfiguration.newPrivate(0, 0);
    configuration.setMessageSerializationFactory(messageSerializationFactory);
    return configuration;
  }

  public static NodeConfiguration newPrivateNodeConfiguration(URI masterUri,
      MessageSerializationFactory messageSerializationFactory) {
    NodeConfiguration configuration = newPrivateNodeConfiguration(messageSerializationFactory);
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPrivateNodeConfiguration(URI masterUri) {
    NodeConfiguration configuration =
        newPrivateNodeConfiguration(new org.ros.internal.message.old_style.MessageSerializationFactory());
    configuration.setMasterUri(masterUri);
    return configuration;
  }

  public static NodeConfiguration newPrivateNodeConfiguration() {
    return newPrivateNodeConfiguration(new org.ros.internal.message.old_style.MessageSerializationFactory());
  }

  public static Node newNode(GraphName name, NodeConfiguration configuration) {
    return new DefaultNode(name, configuration);
  }

  public static Node newNode(String name, NodeConfiguration configuration) {
    return newNode(new DefaultGraphName(name), configuration);
  }

  public static GraphName newGraphName(String name) {
    return new DefaultGraphName(name);
  }

  public static GraphName newRootGraphName() {
    return DefaultGraphName.createRoot();
  }

  public static NameResolver newNameResolver(GraphName namespace,
      Map<GraphName, GraphName> remappings) {
    return new DefaultNameResolver(namespace, remappings);
  }

  public static NameResolver
      newNameResolver(String namespace, Map<GraphName, GraphName> remappings) {
    return newNameResolver(newGraphName(namespace), remappings);
  }

  public static NameResolver newNameResolver(GraphName namespace) {
    return newNameResolver(namespace, new HashMap<GraphName, GraphName>());
  }

  public static NameResolver newNameResolver(String namespace) {
    return newNameResolver(newGraphName(namespace));
  }

  public static NameResolver newNameResolver(Map<GraphName, GraphName> remappings) {
    return newNameResolver(newRootGraphName(), remappings);
  }

  public static NameResolver newNameResolver() {
    return newNameResolver(newRootGraphName());
  }

}
