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

package org.ros.rosjava.android.pan_tilt_camera;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.ros.NodeRunner;
import org.ros.internal.node.address.InetAddressFactory;
import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.rosjava.android.BitmapFromCompressedImage;
import org.ros.rosjava.android.OrientationPublisher;
import org.ros.rosjava.android.views.RosImageView;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {

  private final NodeRunner nodeRunner;

  private OrientationPublisher orientationPublisher;

  public MainActivity() {
    super();
    nodeRunner = NodeRunner.createDefault();
  }

  @Override
  protected void onPause() {
    super.onPause();
    android.os.Process.killProcess(android.os.Process.myPid());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    RosImageView<CompressedImage> image = (RosImageView<CompressedImage>) findViewById(R.id.image);
    image.setTopicName("/slow_image");
    image.setMessageClass(org.ros.message.sensor_msgs.CompressedImage.class);
    image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
    try {
      // TODO(damonkohler): The master needs to be set via some sort of
      // configuration builder.
      String uri = "__master:=http://10.68.0.1:11311";
      String ip = "__ip:=" + InetAddressFactory.createNonLoopback().getHostAddress();
      orientationPublisher =
          new OrientationPublisher((SensorManager) getSystemService(SENSOR_SERVICE));
      nodeRunner.run(orientationPublisher, Lists.newArrayList("Orientation", uri, ip));
      nodeRunner.run(image, Lists.newArrayList("Compressed", uri, ip));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}