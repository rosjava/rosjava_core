package ros.android.utils.master;

import android.content.SharedPreferences.Editor;

import android.content.SharedPreferences;

import android.app.Activity;

import android.content.Intent;

import android.widget.Toast;

import android.app.AlertDialog;

import android.content.DialogInterface;

import android.content.Context;

import android.os.Environment;

import java.io.File;
import java.net.URI;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class MasterChooser {

  private static final String MASTER_URI_PREFS = "MASTER_URI_PREFS";
  private static final String MASTER_URI = "MASTER_URI";

  /**
   * @param ctx 
   * @param requestCode
   * @param resultCode
   * @param intent
   * @return 
   */
  public static String uriFromResult(final Activity ctx, final int resultCode, final Intent intent) {
    if (resultCode == Activity.RESULT_OK) {
      String contents = intent.getStringExtra("SCAN_RESULT");
      // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
      String masterURI = contents;
      MasterChooser.cacheURI(ctx, masterURI);
      return masterURI;
      // Handle successful scan
    } else if (resultCode == Activity.RESULT_CANCELED) {
      Toast.makeText(ctx, "No ROS Master URI found, Please try again.", Toast.LENGTH_LONG).show();
    }
    return "";

  }
  public static void launchUriIntent(final Activity ctx, final int requestCode)
  {
    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    intent.setPackage("com.google.zxing.client.android");
    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
    ctx.startActivityForResult(intent, requestCode);

    Toast.makeText(ctx, "Please Scan a QR Code with a ROS host URI.", Toast.LENGTH_LONG)
        .show();
  }

  /**
   * This will launch an activity to choose the uri
   * 
   * @param ctx
   * @param requestCode
   */
  public static void launchUriSelector(final Activity ctx, final int requestCode) {
    // create a selector gui for picking the ros master url
    final CharSequence[] uriItems = { "From Barcode", "http://localhost:11311",
        "http://10.0.2.2:11311", };
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle("Pick a ROS Master URI");
    builder.setItems(uriItems, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        if (item == 0) {
          launchUriIntent(ctx,requestCode);
        }

      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  /**
   * Return the master URI from the shared preferences of the app.
   * 
   * @param ctx
   *          The app context, the preference is local only to this app.
   * @return a string version of the master URI. null if not cached.
   */
  public static String getCachedURI(final Context ctx) {
    SharedPreferences prefs = ctx.getSharedPreferences(MASTER_URI_PREFS, 0);
    return prefs.getString(MASTER_URI, null);
    
  }

  /**
   * Add a master uri to the shared preferences. Retrieve with getCachedURI.
   * 
   * @param ctx
   *          The app context,the preference is local only to this app.
   * @param uri
   *          The ROS_MASTER uri
   */
  public static void cacheURI(final Context ctx, final String uri) {
    SharedPreferences prefs = ctx.getSharedPreferences(MASTER_URI_PREFS, 0);
    Editor editor = prefs.edit();
    editor.putString(MASTER_URI, uri);
    editor.commit();
  }
}
