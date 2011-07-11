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
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.message.MessageSerializationFactory;
import org.ros.namespace.NameResolver;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface NodeConfiguration {

  public static final String DEFAULT_MASTER_URI = "http://localhost:11311/";

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
   * @return Override for Node name or null if no override.
   */
  String getNodeNameOverride();

  void setNodeNameOverride(String nodeNameOverride);

  MessageSerializationFactory getMessageSerializationFactory();

  void setMessageSerializationFactory(MessageSerializationFactory messageSerializationFactory);

  BindAddress getTcpRosBindAddress();

  void setTcpRosBindAddress(BindAddress tcpRosBindAddress);

  AdvertiseAddress getTcpRosAdvertiseAddress();

  void setTcpRosAdvertiseAddress(AdvertiseAddress tcpRosAdvertiseAddress);

  BindAddress getXmlRpcBindAddress();

  void setXmlRpcBindAddress(BindAddress xmlRpcBindAddress);

  AdvertiseAddress getXmlRpcAdvertiseAddress();

  void setXmlRpcAdvertiseAddress(AdvertiseAddress xmlRpcAdvertiseAddress);

}