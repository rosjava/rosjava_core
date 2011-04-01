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

import ros.android.util.MasterChooser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MasterChooserActivity extends Activity {

  public static final String MASTER_CHOOSER_SELECT = "MASTER_CHOOSER_SELECT";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   // if(getIntent().getBooleanExtra(MASTER_CHOOSER_SELECT, false))
      MasterChooser.launchUriIntent(this);
    //else
    //  finish();
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
    MasterChooser.uriFromResult(this, requestCode, resultCode, data);
    Toast.makeText(this, "ROS Master updated to: " + MasterChooser.getCachedURI(this),
        Toast.LENGTH_LONG).show();
    finish();
  }
}