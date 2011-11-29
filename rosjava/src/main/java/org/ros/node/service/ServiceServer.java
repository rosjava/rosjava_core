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

package org.ros.node.service;

import org.ros.namespace.GraphName;

import java.net.URI;

/**
 * Provides a ROS service.
 * 
 * @see http://www.ros.org/wiki/Services
 * 
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <RequestType>
 *          the {@link ServiceServer} responds to requests of this type
 * @param <ResponseType>
 *          the {@link ServiceServer} returns responses of this type
 */
public interface ServiceServer<RequestType, ResponseType> {

  /**
   * @return the name of the {@link ServiceServer}
   */
  GraphName getName();

  /**
   * @return the {@link URI} for this {@link ServiceServer}
   */
  URI getUri();

  /**
   * Stops the service and unregisters it.
   */
  void shutdown();

  /**
   * Add a new lifecycle listener to the server.
   * 
   * @param listener
   *          The listener to add.
   */
  void addServiceServerListener(ServiceServerListener listener);

  /**
   * Remove a lifecycle listener from the server.
   * 
   * <p>
   * Nothing is done if the listener was never added.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeServiceServerListener(ServiceServerListener listener);

}