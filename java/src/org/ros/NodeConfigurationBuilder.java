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

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NodeConfigurationBuilder {
  
  private final NodeConfiguration nodeConfiguration;
  private NameResolver parentResolver;

  public NodeConfigurationBuilder() {
    nodeConfiguration = new NodeConfiguration();
  }
  
  public void setParentResolver(NameResolver parentResolver) {
    this.parentResolver = parentResolver;
  }

  public NameResolver getParentResolver() {
    return parentResolver;
  }

  public NodeConfiguration build() {
    if (getParentResolver() == null) {
      setParentResolver(NameResolver.createDefault());
    }
    nodeConfiguration.setParentResolver(getParentResolver());
    return nodeConfiguration;
  }

}
