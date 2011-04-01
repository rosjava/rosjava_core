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
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import ros.android.util.zxing.IntentResult;
import ros.android.util.zxing.IntentIntegrator;

public class MasterChooserActivity extends Activity {

  public static final String MASTER_URI_EXTRA = "org.ros.android.MasterURI";

  private List<String> master_uris_;

  public MasterChooserActivity() {
    master_uris_ = new ArrayList<String>();
    master_uris_.add( "http://foo:11311" );
    master_uris_.add( "http://bar:11311" );
    master_uris_.add( "http://baz:11311" );
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle( "Choose a ROS Master" );
    setContentView(R.layout.master_chooser);
    ListView listview = (ListView) findViewById(R.id.master_list);

    listview.setAdapter( new MasterAdapter( this, master_uris_ ));

    listview.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          choose( position );
        }
      });
  }

  private void choose( int position ) {
    Intent result_intent = new Intent();
    result_intent.putExtra( MASTER_URI_EXTRA, master_uris_.get( position ));
    setResult( RESULT_OK, result_intent );
    finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      master_uris_.add( scanResult.getContents() );
    }
    else
    {
      Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
    }
  }

  public void scanNewRobotClicked( View view ) {
    IntentIntegrator.initiateScan( this );
  }
}