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

package ros.android.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import org.ros.NodeContext;
import org.ros.RosLoader;
import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import ros.android.activity.MasterChooserActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Helper class for launching the MasterChooserActivity for choosing a ROS
 * master. Keep this object around for the lifetime of an Activity.
 */
public class MasterChooser extends RosLoader {

  private Activity callingActivity;
  private String masterUri;

  /**
   * REQUEST_CODE number must be unique among activity requests which might be
   * seen by handleActivityResult().
   */
  private static final int REQUEST_CODE = 8748792;

  private static final String MASTER_URI_PREFS = "MASTER_URI_PREFS";
  private static final String MASTER_URI = "MASTER_URI";

  /**
   * Constructor. Does not read current master from disk, that must be done by
   * calling loadCurrentMaster().
   */
  public MasterChooser(Activity calling_activity) {
    callingActivity = calling_activity;
    masterUri = null;
  }

  /**
   * Returns a File for the current-master file if the sdcard is ready and
   * there's no error, null otherwise. The actual file on "disk" does not have
   * to exist for this to work and return a File object.
   */
  private File getCurrentMasterFile() {
    if (!SdCardSetup.isReady()) {
      return null;
    } else {
      try {
        File ros_dir = SdCardSetup.getRosDir();
        return new File(ros_dir, "current_master_uri");
      } catch (Exception ex) {
        Log.e("RosAndroid", "exception in getCurrentMasterFile: " + ex.getMessage());
        return null;
      }
    }
  }

  /**
   * Write the current value of private master_uri_ variable to a common file on
   * the sdcard, so it can be shared between ROS apps.
   */
  public void saveCurrentMaster() {
    File current_master_file = getCurrentMasterFile();
    if (current_master_file == null) {
      Log.e("RosAndroid", "writeNewMaster(): could not get current-master File object.");
      return;
    }

    try {
      if (!current_master_file.exists()) {
        Log.i("RosAndroid", "current-master file does not exist, creating.");
        current_master_file.createNewFile();
      }

      //overwrite the file contents
      FileWriter writer = new FileWriter(current_master_file, false); 
      writer.write(masterUri + "\n");
      writer.close();
      Log.i("RosAndroid", "Wrote '" + masterUri + "' to current-master file.");
    } catch (Exception ex) {
      Log.e("RosAndroid", "exception writing new master to sdcard: " + ex.getMessage());
    }
  }

  /**
   * Read the current master from a file shared by ROS applications, so we don't
   * have to re-choose the master for each new app launch. If the file does not
   * exist or has invalid data, haveMaster() will return false after this. On
   * success, private current_master_ variable is set. On failure, nothing is
   * changed.
   */
  public void loadCurrentMaster() {
    try {
      File currentMasterFile = getCurrentMasterFile();
      if (currentMasterFile == null) {
        Log.e("RosAndroid", "loadCurrentMaster(): can't get the current-master file.");
        return;
      }

      BufferedReader reader = new BufferedReader(new FileReader(currentMasterFile));
      try {
        String line = reader.readLine();
        if (line != null && line != "") {
          masterUri = line;
        }
      } finally {
        reader.close();
      }
    } catch (Exception ex) {
      Log.e("RosAndroid", "exception reading current-master file: " + ex.getMessage());
    }
  }

  /**
   * Returns true if current master URI is set in memory, false otherwise. Does
   * not read anything from disk.
   */
  public boolean hasMaster() {
    return (masterUri != null && masterUri.length() != 0);
  }

  /**
   * Call this from your activity's onActivityResult() to record the master URI.
   * This does not write to the current-master file on the sdcard, that must be
   * done explicitly. The return value of this function only indicates whether
   * the request-result described by the parameters is appropriate for this
   * class. It does not indicate anything about the validity of the returned
   * master URI.
   * 
   * @returns true if the activity result came from the activity started by this
   *          class, false otherwise.
   */
  public boolean handleActivityResult(int requestCode, int resultCode, Intent resultIntent) {
    if (requestCode != REQUEST_CODE)
      return false;

    if (resultCode == Activity.RESULT_OK) {
      masterUri = resultIntent.getStringExtra(MasterChooserActivity.MASTER_URI_EXTRA);
    }
    return true;
  }

  /**
   * Launch the {@link MasterChooserActivity} to choose or scan a new master.
   * Because this launches an activity, the caller's {@code onPause()},
   * {@code onActivityResult()} and {@code onResume()} functions will be called
   * before anything else happens there.
   */
  public void launchChooserActivity() throws ActivityNotFoundException {
    Log.i("RosAndroid", "starting master chooser activity");
    Intent chooserIntent = new Intent(callingActivity, MasterChooserActivity.class);
    callingActivity.startActivityForResult(chooserIntent, REQUEST_CODE);
  }

  /**
   * Create and return a new ROS NodeContext object based on the current value
   * of the internal master_uri_ variable. Throws an exception if that value is
   * invalid or if we can't get a hostname for the device we are running on.
   */
  @Override
  public NodeContext createContext() throws RosInitException {
    return createContext(masterUri, Net.getNonLoopbackHostName());
  }

  static public NodeContext createContext(String masterUri, String myHostName)
      throws RosInitException {
    if (masterUri == null) {
      throw new RosInitException("ROS Master URI is not set");
    }
    String namespace = Namespace.GLOBAL_NS;
    HashMap<GraphName, GraphName> remappings = new HashMap<GraphName, GraphName>();
    NameResolver resolver = new NameResolver(namespace, remappings);

    NodeContext context = new NodeContext();
    context.setParentResolver(resolver);
    context.setRosRoot("fixme");
    context.setRosPackagePath(null);
    try {
      context.setRosMasterUri(new URI(masterUri));
    } catch (URISyntaxException ex) {
      throw new RosInitException("ROS Master URI (" + masterUri + ") is invalid: "
          + ex.getMessage());
    }

    if (myHostName != null) {
      context.setHostName(myHostName);
    } else {
      throw new RosInitException("Could not get a hostname for this device.");
    }
    return context;
  }
}
