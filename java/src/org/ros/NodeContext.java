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

import java.net.URI;

import org.ros.namespace.NameResolver;

/**
 * A context that allows nodeless api, and allows for flexible name resolution,
 * and FIXME fillin...
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class NodeContext {
  private NameResolver resolver;
  private URI rosMasterUri;
  private String addressOverride;
  private String rosRoot;
  private String[] rosPackagePath;

  /**
   * default context
   */
  public NodeContext() {
    resolver = null;
  }

  /**
   * @return The RosResolver fo this context, this should be used to resolve
   *         names that have been remapped.
   * @see NameResolver
   */
  public NameResolver getResolver() {
    return resolver;
  }

  public void setResolver(NameResolver resolver) {
    this.resolver = resolver;
  }

  public URI getRosMasterUri() {
    return rosMasterUri;
  }

  public void setRosMasterUri(URI rosMasterUri) {
    this.rosMasterUri = rosMasterUri;
  }

  /**
   * @return Address to use when binding interfaces or null if default.
   */
  public String getAddressOverride() {
    return addressOverride;
  }

  public void setAddressOverride(String addressOverride) {
    this.addressOverride = addressOverride;
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
}
