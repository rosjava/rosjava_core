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

import org.ros.internal.namespace.DefaultNameResolver;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.NameResolver;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface NodeConfiguration {

  /**
   * @return The {@link DefaultNameResolver} for a {@link Node}'s parent namespace.
   */
  NameResolver getParentResolver();

  void setParentResolver(NameResolver resolver);

  URI getMasterUri();

  void setMasterUri(URI masterUri);

  File getRosRoot();

  void setRosRoot(File rosRoot);

  List<String> getRosPackagePath();

  void setRosPackagePath(List<String> rosPackagePath);

  /**
   * @return host name/address to use when advertising this node via URLs.
   */
  String getHost();

  /**
   * Set host name/address to use when advertising this node via URLs.
   * 
   * @param host
   */
  void setHost(String host);

  /**
   * @return Port to bind TCPROS server to, or 0 to bind to any open port.
   */
  int getTcpRosPort();

  /**
   * Set port to bind TCPROS server to. 0 binds to any open port.
   */
  void setTcpRosPort(int tcpRosPort);

  /**
   * @return Port to bind XMLRPC server to, or 0 to bind to any open port.
   */
  int getXmlRpcPort();

  /**
   * Set port to bind XMLRPC server to. 0 binds to any open port.
   */
  void setXmlRpcPort(int xmlRpcPort);

  /**
   * @return Override for Node name or null if no override.
   */
  String getNodeNameOverride();

  void setNodeNameOverride(String nodeNameOverride);

  MessageSerializationFactory getMessageSerializationFactory();

  void setMessageSerializationFactory(MessageSerializationFactory messageSerializationFactory);

}