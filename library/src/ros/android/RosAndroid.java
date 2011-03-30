package ros.android;

import android.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import ros.android.utils.master.MasterChooser;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class RosAndroid extends Activity {

  private static final int MASTER_URI_REQUEST = 0;
  private static final String ROSANDROID_TAG = "RosAndroid";
  private String mMasterUri;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == MASTER_URI_REQUEST) {
      mMasterUri = MasterChooser.uriFromResult(this, resultCode, intent);
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mMasterUri = MasterChooser.getCachedURI(getApplicationContext());
    if (mMasterUri == "") {
      MasterChooser.launchUriSelector(this, MASTER_URI_REQUEST);
    }
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    Log.d(ROSANDROID_TAG, "Master uri is : " + mMasterUri);
  }
  
  
}