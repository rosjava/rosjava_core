package ros.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import ros.android.utils.master.MasterChooser;

public class MasterChooserActivity extends Activity {

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MasterChooser.launchUriIntent(this, 0);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      MasterChooser.uriFromResult(this, resultCode, data);
      Toast.makeText(this, "ROS Master updated to: " + MasterChooser.getCachedURI(this),
          Toast.LENGTH_LONG).show();
    }
    finish();
  }
}