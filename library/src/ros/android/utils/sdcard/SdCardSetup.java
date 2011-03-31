package ros.android.utils.sdcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Environment;
import org.ros.exceptions.RosException;

import java.io.File;

/** Some helpers for android sdcard acces.
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 *
 */
public class SdCardSetup {

  /**
   * Checks if the external storage is mounted and writable.
   * 
   * @return true if the external storage is mounted and writable.
   */
  private static boolean checkExternalStorage() {
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
    builder.setTitle("Please mount your sdcard.");
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
   * doesn't exist.
   * 
   * @return A file that will likely be /sdcard/ros
   * @throws RosException
   */
  public static File getRosDir() throws RosException {
    if (!checkExternalStorage())
      throw new RosException("Couldn't access the external storage device!");
    File sdcard = Environment.getExternalStorageDirectory();
    File ros_dir = new File(sdcard, "ros");
    if (!ros_dir.exists())
      ros_dir.mkdirs();
    return ros_dir;
  }
}
