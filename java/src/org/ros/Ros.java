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
import java.net.URISyntaxException;

/**
 * Alias class for some global convenience functions ?
 * 
 * @author erublee
 * 
 */
public class Ros {

  /**
   * Finds the environment's host name, will look in TODO ROS_HOSTNAME
   * 
   * @return the undecorated hostname, e.g. 'localhost'
   */
  public static String getHostName() {
    // TODO better resolution? from env
    return "localhost";
  }

  /**
   * Get the master uri, maybe from environment or else where?
   * 
   * @return The url of the master, with port(typically 11311).
   * @throws URISyntaxException 
   */
  public static URI getMasterUri() throws URISyntaxException {
    return new URI("http://localhost:11311/");
  }

}
