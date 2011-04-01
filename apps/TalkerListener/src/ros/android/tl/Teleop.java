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

package ros.android.tl;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.ros.exceptions.RosInitException;
import org.ros.message.geometry_msgs.Vector3Stamped;
import ros.android.activity.RosActivity;
import ros.android.sensor.GravityTeleop;

public class Teleop extends RosActivity {

  GravityTeleop sensor;
  Thread teleopSensorThread;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    sensor = new GravityTeleop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      setText("loading");

      sensor.start(this);

      teleopSensorThread = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            while (true) {
              Vector3Stamped v = sensor.getGravityVector();
              if (v != null) {
                Log.i("RosAndroid", "X: " + v.vector.x + "Y: " + v.vector.y);
              }
              Thread.sleep(100);
            }
          } catch (InterruptedException e) {
          }
        }
      });
      teleopSensorThread.start();

    } catch (RosInitException e) {
      setText(e.getMessage());
    }

  }

  @Override
  protected void onPause() {
    super.onPause();
    sensor.stop(this);
    teleopSensorThread.interrupt();
    teleopSensorThread = null;
  }

  private void setText(String text) {
    TextView t = (TextView) findViewById(R.id.text_view);
    t.setText(text);
  }

}