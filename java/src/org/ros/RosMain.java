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

/**
 * A simple interface for entry points into your app.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 */
public abstract class RosMain {
  /**
   * Called to start your node by some magical ros java file.
   * 
   * @param argv
   * @param context The ros context with which to the RosMain is assumed to abide by.
   */
  abstract public void rosMain(String argv[], RosContext context);
}
