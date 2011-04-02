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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Environment;
import org.ros.exceptions.RosException;

import java.io.File;

/** 
 * Helpers for Android sdcard access.
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class SdCardSetup {

  /**
   * Checks if the external storage is mounted and writable.
   * 
   * @return true if the external storage is mounted and writable.
   */
  public static boolean isReady() {
    boolean externalStorageAvailable = false;
    boolean externalStorageWriteable = false;
    String state = Environment.getExternalStorageState();

    if (Environment.MEDIA_MOUNTED.equals(state)) {
      // We can read and write the media
      externalStorageAvailable = externalStorageWriteable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      // We can only read the media
      externalStorageAvailable = true;
      externalStorageWriteable = false;
    } else {
      // Something else is wrong. It may be one of many other states, but all we
      // need
      // to know is we can neither read nor write
      externalStorageAvailable = externalStorageWriteable = false;
    }
    return externalStorageAvailable && externalStorageWriteable;
  }

  /**
   * Display an alert for the user to mount their sdcard.
   * 
   * @param ctx
   *          The application context with which to display the alert.
   */
  public static void promptUserForMount(Context ctx) {
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle("Please mount your (writable) sdcard.");
    builder.setCancelable(true);
    builder.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          dialog.dismiss();
        }
      });
    builder.create().show();
  }

  /**
   * Helper function for geting the ros directory. This will create it if it
   * doesn't exist.  This will fail if isReady() returns false.
   * 
   * @return A file that will likely be /sdcard/ros
   * @throws RosException
   */
  public static File getRosDir() {
    File sdcard = Environment.getExternalStorageDirectory();
    File ros_dir = new File(sdcard, "ros");
    if (!ros_dir.exists())
      ros_dir.mkdirs();
    return ros_dir;
  }
}
