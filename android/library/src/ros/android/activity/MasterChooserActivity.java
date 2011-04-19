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
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.yaml.snakeyaml.Yaml;
import ros.android.util.Net;
import ros.android.util.RobotDescription;
import ros.android.util.SdCardSetup;
import ros.android.util.zxing.IntentIntegrator;
import ros.android.util.zxing.IntentResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MasterChooserActivity extends Activity {

  private static final int ADD_URI_DIALOG_ID = 0;
  private static final int WIFI_DISABLED_DIALOG_ID = 1;
  private static final int WIFI_ENABLED_BUT_NOT_CONNECTED_DIALOG_ID = 2;

  public static final String ROBOT_DESCRIPTION_EXTRA = "org.ros.android.RobotDescription";

  // don't modify this without immediately calling updateListView().
  private List<RobotDescription> robots;
  private WifiManager wifiManager;

  public MasterChooserActivity() {
    robots = new ArrayList<RobotDescription>();
  }

  private File getRobotListFile() {
    if (!SdCardSetup.isReady()) {
      SdCardSetup.promptUserForMount(this);
      return null;
    } else {
      try {
        File rosDir = SdCardSetup.getRosDir();
        File robotListFile = new File(rosDir, "robots.yaml");
        if (!robotListFile.exists()) {
          Log.i("RosAndroid", "robots.yaml file does not exist, creating.");
          robotListFile.createNewFile();
        }
        return robotListFile;
      } catch (Exception ex) {
        Log.e("RosAndroid", "exception in getRobotListFile: " + ex.getMessage());
        return null;
      }
    }
  }

  public void writeRobotList() {
    File robotListFile = getRobotListFile();
    if (robotListFile == null) {
      Log.e("RosAndroid", "writeNewRobot(): no robots file.");
      return;
    }

    try {
      FileWriter writer = new FileWriter(robotListFile);
      Yaml yaml = new Yaml();
      yaml.dump(robots, writer);
      writer.close();
      Log.i("RosAndroid", "Wrote robots.yaml file.");
    } catch (Exception ex) {
      Log.e("RosAndroid", "exception writing robots.yaml to sdcard: " + ex.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private void readRobotList() {
    try {
      File robotListFile = getRobotListFile();
      if (robotListFile == null) {
        Log.e("RosAndroid", "readRobotList(): no robots.yaml file.");
        return;
      }

      BufferedReader reader = new BufferedReader(new FileReader(robotListFile));
      try {
        Yaml yaml = new Yaml();
        robots = (List<RobotDescription>) yaml.load(reader);
        if (robots == null) {
          robots = new ArrayList<RobotDescription>();
        }
      } finally {
        reader.close();
      }
    } catch (Exception ex) {
      Log.e("RosAndroid", "exception reading list of previous master URIs: " + ex.getMessage());
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle("Choose a ROS Master");
    readRobotList();
  }

  @Override
  protected void onResume() {
    super.onResume();
    wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    readRobotList();
    updateListView();
    warnIfWifiDown();
  }

  private void warnIfWifiDown() {
    if (!wifiManager.isWifiEnabled()) {
      showDialog(WIFI_DISABLED_DIALOG_ID);
    } else if (wifiManager.getConnectionInfo() == null) {
      showDialog(WIFI_ENABLED_BUT_NOT_CONNECTED_DIALOG_ID);
    } else {
      Log.i("RosAndroid", "wifi seems OK.");
    }
  }

  private void updateListView() {
    setContentView(R.layout.master_chooser);
    ListView listview = (ListView) findViewById(R.id.master_list);
    listview.setAdapter(new MasterAdapter(this, robots, Net.getNonLoopbackHostName()));
    listview.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        choose(position);
      }
    });
  }

  private void choose(int position) {
    Intent resultIntent = new Intent();
    resultIntent.putExtra(ROBOT_DESCRIPTION_EXTRA, robots.get(position));
    setResult(RESULT_OK, resultIntent);
    finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      addMaster(scanResult.getContents());
    } else {
      Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
    }
  }

  private void addMaster(String masterUri) {
    RobotDescription newRobot = new RobotDescription();
    newRobot.masterUri = masterUri;
    Iterator<RobotDescription> iter = robots.iterator();
    while (iter.hasNext()) {
      RobotDescription robot = iter.next();
      if (robot.masterUri.equals(masterUri)) {
        Toast.makeText(this, "That robot is already listed.", Toast.LENGTH_SHORT).show();
        return;
      }
    }
    robots.add(newRobot);
    onRobotsChanged();
  }

  private void onRobotsChanged() {
    writeRobotList();
    updateListView();
  }

  private void deleteUnresponsiveRobots() {
    Iterator<RobotDescription> iter = robots.iterator();
    while( iter.hasNext() ) {
      RobotDescription robot = iter.next();
      if( robot == null || robot.connectionStatus == null || !robot.connectionStatus.equals( "ok" )) {
        Log.i("RosAndroid", "Removing robot with connection status '" + robot.connectionStatus + "'");
        iter.remove();
      }
    }
    onRobotsChanged();
  }

  private void deleteAllRobots() {
    robots.clear();
    onRobotsChanged();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog;
    Button button;
    switch (id) {
    case ADD_URI_DIALOG_ID:
      dialog = new Dialog(this);
      dialog.setContentView(R.layout.add_uri_dialog);
      dialog.setTitle("Add a robot");
      EditText uriField = (EditText) dialog.findViewById(R.id.uri_editor);
      uriField.setOnKeyListener(new URIFieldKeyListener());
      button = (Button) dialog.findViewById(R.id.scan_robot_button);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          scanRobotClicked(v);
        }
      });
      button = (Button) dialog.findViewById(R.id.cancel_button);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dismissDialog( ADD_URI_DIALOG_ID );
        }
      });
      break;
    case WIFI_DISABLED_DIALOG_ID:
      dialog = new Dialog(this);
      dialog.setContentView(R.layout.wireless_disabled_dialog);
      dialog.setTitle("Wifi network disabled.");
      button = (Button) dialog.findViewById(R.id.ok_button);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dismissDialog(WIFI_DISABLED_DIALOG_ID);
        }
      });
      button = (Button) dialog.findViewById(R.id.enable_button);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          wifiManager.setWifiEnabled(true);
          dismissDialog(WIFI_DISABLED_DIALOG_ID);
        }
      });
      break;
    case WIFI_ENABLED_BUT_NOT_CONNECTED_DIALOG_ID:
      dialog = new Dialog(this);
      dialog.setContentView(R.layout.wireless_enabled_but_not_connected_dialog);
      dialog.setTitle("Wifi not connected.");
      button = (Button) dialog.findViewById(R.id.ok_button);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dismissDialog(WIFI_ENABLED_BUT_NOT_CONNECTED_DIALOG_ID);
        }
      });
    default:
      dialog = null;
    }
    return dialog;
  }

  public void addRobotClicked(View view) {
    showDialog(ADD_URI_DIALOG_ID);
  }

  public void scanRobotClicked(View view) {
    dismissDialog(ADD_URI_DIALOG_ID);
    IntentIntegrator.initiateScan(this,
                                  IntentIntegrator.DEFAULT_TITLE,
                                  IntentIntegrator.DEFAULT_MESSAGE,
                                  IntentIntegrator.DEFAULT_YES,
                                  IntentIntegrator.DEFAULT_NO,
                                  IntentIntegrator.QR_CODE_TYPES);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.master_chooser_options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.add_robot:
      showDialog( ADD_URI_DIALOG_ID );
      return true;
    case R.id.delete_unresponsive:
      deleteUnresponsiveRobots();
      return true;
    case R.id.delete_all:
      deleteAllRobots();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  public class URIFieldKeyListener implements View.OnKeyListener {
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        EditText uriField = (EditText) view;
        String newMasterUri = uriField.getText().toString();
        if( newMasterUri != null && newMasterUri.length() > 0 ) {
          addMaster(newMasterUri);
        }
        dismissDialog(ADD_URI_DIALOG_ID);
        return true;
      }
      return false;
    }
  }
}
