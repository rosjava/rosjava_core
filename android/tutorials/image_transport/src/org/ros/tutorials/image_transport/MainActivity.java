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

package org.ros.tutorials.image_transport;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;

import org.ros.NodeRunner;
import org.ros.message.sensor_msgs.CompressedImage;

import ros.android.views.BitmapFromCompressedImage;
import ros.android.views.RosImageView;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {

  private final NodeRunner nodeRunner;

  public MainActivity() {
    super();
    nodeRunner = NodeRunner.createDefault();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    RosImageView<CompressedImage> image =
        (RosImageView<CompressedImage>) findViewById(R.id.image);
    image.setTopicName("/usb_cam/image_raw/compressed");
    image.setMessageClass(org.ros.message.sensor_msgs.CompressedImage.class);
    image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
    try {
      // TODO(damonkohler): The master needs to be set via some sort of
      // NodeConfiguration builder.
      nodeRunner.run(image,
          Lists.newArrayList("Compressed", "__master:=http://192.168.144.238:11311/"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
