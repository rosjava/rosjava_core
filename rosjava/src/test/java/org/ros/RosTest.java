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

import org.junit.After;
import org.junit.Before;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.parameter.ParameterTree;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RosTest {

  protected RosCore rosCore;
  protected ParameterTree parameters;
  protected NodeConfiguration nodeConfiguration;
  protected NodeMainExecutor nodeMainExecutor;
  
  @Before
  public void setUp() throws InterruptedException {
    rosCore = RosCore.newPrivate();
    rosCore.start();
    rosCore.awaitStart();

    nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeConfiguration = NodeConfiguration.newPrivate(rosCore.getUri());
  }
  
  @After
  public void tearDown() {
    nodeMainExecutor.shutdown();
    rosCore.shutdown();
  }
}
