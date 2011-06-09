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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.ros.Node;
import org.ros.NodeConfiguration;
import org.ros.NodeMain;
import org.ros.Publisher;
import org.ros.message.Time;
import org.ros.message.geometry_msgs.PoseStamped;
import org.ros.message.geometry_msgs.Quaternion;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OrientationPublisher implements NodeMain {

  private final SensorManager sensorManager;

  private Node node;
  private OrientationListener orientationListener;

  private final class OrientationListener implements SensorEventListener {

    private final Publisher<PoseStamped> publisher;
    private final org.ros.message.geometry_msgs.Point origin;

    private volatile int seq;

    private OrientationListener(Publisher<PoseStamped> publisher) {
      this.publisher = publisher;
      origin = new org.ros.message.geometry_msgs.Point();
      origin.x = 0;
      origin.y = 0;
      origin.z = 0;
      seq = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion, event.values);
        Quaternion orientation = new Quaternion();
        orientation.w = quaternion[0];
        orientation.x = quaternion[1];
        orientation.y = quaternion[2];
        orientation.z = quaternion[3];
        PoseStamped pose = new PoseStamped();
        pose.header.frame_id = "/map";
        pose.header.seq = seq++;
        pose.header.stamp = Time.fromMillis(System.currentTimeMillis());
        pose.pose.position = origin;
        pose.pose.orientation = orientation;
        publisher.publish(pose);
      }
    }
  }

  public OrientationPublisher(SensorManager sensorManager) {
    this.sensorManager = sensorManager;
  }

  @Override
  public void main(NodeConfiguration configuration) throws Exception {
    try {
      node = new Node("orientation", configuration);
      Publisher<org.ros.message.geometry_msgs.PoseStamped> publisher =
          node.createPublisher("android/orientation",
              org.ros.message.geometry_msgs.PoseStamped.class);
      orientationListener = new OrientationListener(publisher);
      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
      sensorManager.registerListener(orientationListener, sensor, 500000); // 10 Hz
    } catch (Exception e) {
      if (node != null) {
        node.getLog().fatal(e);
      } else {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void shutdown() {
    sensorManager.unregisterListener(orientationListener);
    node.shutdown();
  }

}
