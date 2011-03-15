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

package com.googlecode.rosjava.android;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.TopicDefinition;
import org.ros.message.Time;
import org.ros.message.geometry.Point;
import org.ros.message.geometry.PoseStamped;
import org.ros.message.geometry.Quaternion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Activity {

  private final Point origin;
  private volatile int seq = 0;

  public Main() {
    super();
    origin = new Point();
    origin.x = 0;
    origin.y = 0;
    origin.z = 0;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    MasterClient master = null;
    try {
      master = new MasterClient(new URI("http://192.168.1.136:11311/"));
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return;
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    TopicDefinition topicDefinition =
        new TopicDefinition("/android/pose",
            MessageDefinition
                .createFromMessage(new org.ros.message.geometry.PoseStamped()));
    final Publisher publisher;
    publisher = new Publisher(topicDefinition);
    publisher.start(new InetSocketAddress(0));

    SlaveServer slave = new SlaveServer("/android", master, "192.168.1.141", 7331);
    try {
      slave.start();
      slave.addPublisher(publisher);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    sensorManager.registerListener(new SensorEventListener() {

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

    }, sensor, SensorManager.SENSOR_DELAY_FASTEST);
  }

}
