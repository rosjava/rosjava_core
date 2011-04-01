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

package ros.android.sensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import org.ros.exceptions.RosInitException;
import org.ros.message.Time;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.Vector3Stamped;
import ros.android.activity.RosActivity;

/**
 * GravityTeleop presents the phone's sensors in a format suitable for
 * teleoperation. It will auto-detect the orientation of the phone's screen, so
 * it can be used in both portrait and landscape orientations. However, it
 * should only be used in applications where the screen orientation is locked.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class GravityTeleop implements SensorEventListener {

  private Sensor gravity;
  private Vector3Stamped vector;
  private int rotation;
  private Twist twist;

  public GravityTeleop() {
    vector = null;
    twist = new Twist();
  }

  public void start(Activity activity) throws RosInitException {
    WindowManager windowManager = (WindowManager) activity
        .getSystemService(Activity.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    rotation = display.getRotation();
    SensorManager sensorManager = (SensorManager) activity
        .getSystemService(Activity.SENSOR_SERVICE);
    gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
  }

  public void stop(RosActivity activity) {
    SensorManager sensorManager = (SensorManager) activity
        .getSystemService(Activity.SENSOR_SERVICE);
    sensorManager.unregisterListener(this);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() != Sensor.TYPE_GRAVITY) {
      return;
    }
    float x, y;
    // Don't teleop if Z is negative.
    if (event.values[2] < 0) {
      x = 0;
      y = 0;
    } else {
      switch (rotation) {
      case Surface.ROTATION_0:
        x = event.values[0];
        y = event.values[1];
        break;
      case Surface.ROTATION_90:
        x = -event.values[1];
        y = event.values[0];
        break;
      case Surface.ROTATION_180:
        x = -event.values[0];
        y = -event.values[1];
        break;
      case Surface.ROTATION_270:
        x = event.values[1];
        y = -event.values[0];
        break;
      default:
        x = event.values[0];
        y = event.values[1];
      }
    }

    Vector3Stamped nextVector = new Vector3Stamped();
    nextVector.header.stamp = Time.fromNano(event.timestamp);
    nextVector.vector.x = x / 9.8;
    nextVector.vector.y = y / 9.8;
    nextVector.vector.z = event.values[2] / 9.8;
    vector = nextVector;
  }

  @Override
  public void onAccuracyChanged(Sensor arg0, int arg1) {
  }

  /**
   * @return Current teleop sensor state as a {@link Vector3Stamped}. Vector is
   *         normalized to -1.0..1.0 values for x, y, and z, where 1.0
   *         represents 9.8 m/s2.
   */
  public Vector3Stamped getGravityVector() {
    return vector;
  }

  /**
   * @return Current teleop sensor state as a {@link Twist}. This reuses the
   *         same {@link Twist} instance.
   */
  public Twist getTwist() {
    if (vector == null) {
      return null;
    } else {
      // cache for thread safety
      Vector3Stamped v = vector;
      twist.linear.x = -v.vector.y;
      twist.linear.y = 0;
      twist.linear.z = 0;

      twist.angular.x = 0;
      twist.angular.y = 0;
      twist.angular.z = 2 * v.vector.x;
      return twist;
    }
  }

}
