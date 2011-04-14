/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2011, Willow Garage, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * Neither the name of Willow Garage, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ros.android.activity;

import android.util.Log;
import org.ros.Node;
import org.ros.exceptions.RosInitException;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import ros.android.util.RobotDescription;

/**
 * Activity for Android that acts as a client for an external ROS app.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosAppActivity extends RosActivity {

  protected AppManager appManager;

  public RosAppActivity() {

  }

  private AppManager createAppManagerCb(Node node, RobotDescription robotDescription)
      throws RosInitException, XmlRpcTimeoutException {
    if (robotDescription == null) {
      throw new RosInitException("no robot available");
    } else {
      Log.i("RosAndroid", "Using Robot: " + robotDescription.robotName + " "
          + robotDescription.masterUri);
      return AppManager.create(node, robotDescription.robotName);
    }
  }

  protected Namespace getAppNamespace(Node node) throws RosInitException {
    RobotDescription robotDescription = getCurrentRobot();
    if (robotDescription == null) {
      throw new RosInitException("no robot available");
    }
    return node.createNamespace(NameResolver.join(robotDescription.robotName, "application"));
  }

  @Override
  protected void onNodeCreate(Node node) {
    Log.i("RosAndroid", "RosAppActivity.onNodeCreate");
    super.onNodeCreate(node);
    RobotDescription robotDescription = getCurrentRobot();
    try {
      appManager = createAppManagerCb(node, robotDescription);
    } catch (RosInitException e) {
      Log.e("RosAndroid", "ros init failed", e);
      appManager = null;
    } catch (XmlRpcTimeoutException e) {
      Log.e("RosAndroid", "ros init failed", e);
      appManager = null;
    }
  }

}
