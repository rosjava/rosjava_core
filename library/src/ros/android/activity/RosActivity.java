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

package ros.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import org.ros.Node;
import org.ros.exceptions.RosInitException;
import ros.android.util.MasterChooser;
import ros.android.util.RobotDescription;

public class RosActivity extends Activity {
  private MasterChooser masterChooser;
  private Node node;
  private Exception errorException;
  private String errorMessage;

  public RosActivity() {
    masterChooser = new MasterChooser(this);
  }

  public Exception getErrorException() {
    return errorException;
  }

  public void setErrorException(Exception errorException) {
    this.errorException = errorException;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public RobotDescription getCurrentRobot() {
    return masterChooser.getCurrentRobot();
  }

  /**
   * Re-launch the MasterChooserActivity to choose a new ROS master. The results
   * are handled in onActivityResult() and onResume() since launching a new
   * activity necessarily pauses this current one.
   */
  public void chooseNewMaster() {
    masterChooser.launchChooserActivity();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (node != null) {
      node.stop();
    }
    node = null;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent result_intent) {
    if (masterChooser.handleActivityResult(requestCode, resultCode, result_intent)) {
      // Save before checking validity in case someone wants to force
      // the next app to use the chooser.
      masterChooser.saveCurrentRobot();
      if (!masterChooser.hasRobot()) {
        Toast.makeText(this, "Cannot run without a ROS master.", Toast.LENGTH_LONG).show();
        finish();
      }
    }
  }

  /**
   * Read the current ROS master URI from external storage and set up the ROS
   * node from the resulting node context. If the current master is not set or
   * is invalid, launch the MasterChooserActivity to choose one or scan a new
   * one.
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (node == null) {
      masterChooser.loadCurrentRobot();
      if (masterChooser.hasRobot()) {
        try {
          node = new Node("listener", masterChooser.createContext());
        } catch (Exception e) {
          Log.e("RosAndroid", "Exception while creating node: " + e.getMessage());
          node = null;
          setErrorMessage("failed to create node" + e.getMessage());
          setErrorException(e);
        }
      } else // we don't have a master yet.
      {
        masterChooser.launchChooserActivity();
        // Launching the master chooser activity causes this activity
        // to pause. this.onActivityResult() is called with the
        // result before onResume() is called again, so
        // masterChooser.hasRobot() should be true.
      }
    }
  }

  /**
   * Retrieve the ROS {@link Node} for this {@link Activity}. The {@link Node}
   * is stopped during {@code onPause()} and reinitialized during
   * {@code onResume()}. It is not safe to maintain a handle on the {@link Node}
   * instance.
   * 
   * @return Initialized {@link Node} instance.
   * @throws RosInitException
   *           If {@link Node} was not successfully initialized. Exception will
   *           contain original initialization exception.
   */
  public Node getNode() throws RosInitException {
    if (node == null) {
      throw new RosInitException(getErrorException());
    }
    return node;
  }

}