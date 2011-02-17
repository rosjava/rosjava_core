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

package org.ros.topic;

import com.google.common.collect.ImmutableMap;

import org.ros.node.server.SlaveDescription;

import java.net.URL;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceDescription {

  private final SlaveDescription slaveDescription;
  private final ServiceDefinition serviceDefinition;
  private final URL url;

  public ServiceDescription(SlaveDescription slaveDescription, ServiceDefinition serviceDefinition,
      URL url) {
    this.slaveDescription = slaveDescription;
    this.serviceDefinition = serviceDefinition;
    this.url = url;
  }

  /**
   * @return
   */
  public Map<String, String> toHeader() {
    return ImmutableMap.<String, String>builder().putAll(slaveDescription.toHeader())
        .putAll(serviceDefinition.toHeader()).build();
  }

  /**
   * @return
   */
  public String getName() {
    return serviceDefinition.getType();
  }

  /**
   * @return
   */
  public Object getUrl() {
    return url;
  }

}
