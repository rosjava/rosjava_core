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
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import ros.android.util.zxing.IntentResult;
import ros.android.util.zxing.IntentIntegrator;
import ros.android.util.SdCardSetup;
import ros.android.util.Net;
import ros.android.util.RobotDescription;

import org.yaml.snakeyaml.Yaml;

public class MasterChooserActivity extends Activity {

  private static final int ADD_URI_DIALOG_ID = 0;

  public static final String ROBOT_DESCRIPTION_EXTRA = "org.ros.android.RobotDescription";

  // TODO: This should eventually be a list of RobotDescriptions to
  // persist robot name and type data.
  private List<RobotDescription> robots_; // don't modify this without immediately calling updateListView().

  public MasterChooserActivity() {
    robots_ = new ArrayList<RobotDescription>();
  }

  private File getRobotListFile() {
    if( !SdCardSetup.isReady() )
    {
      SdCardSetup.promptUserForMount( this );
      return null;
    }
    else
    {
      try
      {
        File ros_dir = SdCardSetup.getRosDir();
        File robot_list_file = new File( ros_dir, "robots.yaml" );
        if( ! robot_list_file.exists() )
        {
          Log.i( "RosAndroid", "robots.yaml file does not exist, creating." );
          robot_list_file.createNewFile();
        }
        return robot_list_file;
      }
      catch( Exception ex )
      {
        Log.e( "RosAndroid", "exception in getRobotListFile: " + ex.getMessage() );
        return null;
      }
    }
  }

  public void writeRobotList() {
    File robot_list_file = getRobotListFile();
    if( robot_list_file == null )
    {
      Log.e( "RosAndroid", "writeNewRobot(): no robots file." );
      return;
    }

    try
    {
      FileWriter writer = new FileWriter( robot_list_file );
      Yaml yaml = new Yaml();
      yaml.dump( robots_, writer );
      writer.close();
      Log.i( "RosAndroid", "Wrote robots.yaml file." );
    }
    catch( Exception ex )
    {
      Log.e( "RosAndroid", "exception writing robots.yaml to sdcard: " + ex.getMessage() );
    }
  }

  private void readRobotList() {
    try
    {
      File robot_list_file = getRobotListFile();
      if( robot_list_file == null )
      {
        Log.e( "RosAndroid", "readRobotList(): no robots.yaml file." );
        return;
      }

      BufferedReader reader = new BufferedReader( new FileReader( robot_list_file ));
      try
      {
        Yaml yaml = new Yaml();
        robots_ = (List<RobotDescription>) yaml.load( reader );
        if( robots_ == null )
        {
          robots_ = new ArrayList<RobotDescription>();
        }
      }
      finally
      {
        reader.close();
      }
    }
    catch( Exception ex )
    {
      Log.e( "RosAndroid", "exception reading list of previous master URIs: " + ex.getMessage() );
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle( "Choose a ROS Master" );
  }

  @Override
  protected void onResume() {
    super.onResume();
    readRobotList();
    updateListView();
  }

  private void updateListView() {
    setContentView(R.layout.master_chooser);
    ListView listview = (ListView) findViewById(R.id.master_list);
    listview.setAdapter( new MasterAdapter( this, robots_, Net.getNonLoopbackHostName() ));

    listview.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          choose( position );
        }
      });
  }

  private void choose( int position ) {
    Intent result_intent = new Intent();
    result_intent.putExtra( ROBOT_DESCRIPTION_EXTRA, robots_.get( position ));
    setResult( RESULT_OK, result_intent );
    finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      addMaster( scanResult.getContents() );
    }
    else
    {
      Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
    }
  }

  private void addMaster( String master_uri ) {
    RobotDescription new_robot = new RobotDescription();
    new_robot.masterUri = master_uri;
    robots_.add( new_robot );
    onRobotsChanged();
  }

  private void addRobot( RobotDescription new_robot ) {
    robots_.add( new_robot );
    onRobotsChanged();
  }

  private void onRobotsChanged() {
    writeRobotList();
    updateListView();
  }

  @Override
  protected Dialog onCreateDialog( int id ) {
    Dialog dialog;
    switch( id )
    {
    case ADD_URI_DIALOG_ID:
      dialog = new Dialog( this );
      dialog.setContentView( R.layout.add_uri_dialog );
      dialog.setTitle( "Add a robot" );
      EditText uri_field = (EditText) dialog.findViewById( R.id.uri_editor );
      uri_field.setOnKeyListener( new URIFieldKeyListener() );
      Button scan_button = (Button) dialog.findViewById( R.id.scan_robot_button );
      scan_button.setOnClickListener( new View.OnClickListener() {
          @Override
          public void onClick( View v ) {
            scanRobotClicked( v );
          }
        });
      break;
    default:
      dialog = null;
    }
    return dialog;
  }

  public void addRobotClicked( View view ) {
    showDialog( ADD_URI_DIALOG_ID );
  }

  public void scanRobotClicked( View view ) {
    dismissDialog( ADD_URI_DIALOG_ID );
    IntentIntegrator.initiateScan( this );
  }

  public class URIFieldKeyListener implements View.OnKeyListener {
    @Override
    public boolean onKey( View view, int key_code, KeyEvent event ) {
      if( event.getAction() == KeyEvent.ACTION_DOWN &&
          key_code == KeyEvent.KEYCODE_ENTER )
      {
        EditText uri_field = (EditText) view;
        addMaster( uri_field.getText().toString() );
        dismissDialog( ADD_URI_DIALOG_ID );
        return true;
      }
      return false;
    }
  }
}
