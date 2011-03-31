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
import org.ros.Node;
import org.ros.NodeContext;
import org.ros.exceptions.RosInitException;
import ros.android.BarcodeLoader;
import ros.android.util.MasterChooser;

public class RosActivity extends Activity {

  private static final int BARCODE_SCAN_REQUEST_CODE = 215421;
  private Node node;
  private Exception errorException;
  private String errorMessage;

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

  @Override
  protected void onPause() {
    super.onPause();
    if (node != null) {
      node.stop();
    }
    node = null;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (node == null) {
      Log.i("RosAndroid", "loading.... should only happen once");
      try {
        // TODO(kwc) check error conditions incrementally so that better error
        // handling behavior can surface to developer.
        BarcodeLoader loader = new BarcodeLoader(this, BARCODE_SCAN_REQUEST_CODE);
        loader.makeNotification(this);
        NodeContext context;
        context = loader.createContext();
        node = new Node("listener", context);
        // final Log log = node.getLog();
      } catch (Exception e) {
        node = null;
        setErrorMessage("failed to create node" + e.getMessage());
        setErrorException(e);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BARCODE_SCAN_REQUEST_CODE) {
      MasterChooser.uriFromResult(this, resultCode, data);
    }
  }
}