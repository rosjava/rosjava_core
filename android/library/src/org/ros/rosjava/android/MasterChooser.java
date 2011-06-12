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

package org.ros.rosjava.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ros.NodeConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterChooser extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.master_chooser);

    final EditText uriText = (EditText) findViewById(R.id.master_chooser_uri);
    final Button okButton = (Button) findViewById(R.id.master_chooser_ok);
    final Button cancelButton = (Button) findViewById(R.id.master_chooser_cancel);

    okButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        try {
          URI uri = new URI(uriText.getText().toString());
          if (uri.toString().length() == 0) {
            uri = new URI(NodeConfiguration.DEFAULT_MASTER_URI);
          }
          intent.putExtra("ROS_MASTER_URI", uri);
          setResult(RESULT_OK, intent);
          finish();
        } catch (URISyntaxException e) {
          Toast.makeText(MasterChooser.this, "Invalid URI", Toast.LENGTH_SHORT).show();
        }
      }
    });

    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
  }

}
