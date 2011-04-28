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

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

import org.ros.Node;
import org.ros.NodeConfiguration;
import org.ros.NodeMain;
import org.ros.Publisher;
import org.ros.exceptions.RosInitException;
import org.ros.message.Time;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.Image;
import org.ros.namespace.NameResolver;

/**
 * A camera node that publishes images and camera_info
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CameraPublisher implements NodeMain, PreviewCallback {

  private Node node;
  private Publisher<Image> imagePublisher;
  private Publisher<CameraInfo> cameraInfoPublisher;
  private Image image;
  private CameraInfo cameraInfo;

  public CameraPublisher() {
    image = new Image();
    cameraInfo = new CameraInfo();
  }

  @Override
  public void run(NodeConfiguration nodeConfiguration) throws RosInitException {
    node = new Node("/anonymous", nodeConfiguration);
    NameResolver resolver = node.getResolver().createResolver("camera");
    // create image and camera info topics on local namespace
    imagePublisher = node.createPublisher(resolver.resolveName("image_raw"), Image.class);
    cameraInfoPublisher =
        node.createPublisher(resolver.resolveName("camera_info"), CameraInfo.class);
  }

  public void stop() {
    imagePublisher = null;
    cameraInfoPublisher = null;
    Log.i("RosAndroid", "camera stop()");
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (imagePublisher == null || cameraInfoPublisher == null) {
      Log.i("RosAndroid", "onPreviewFrame: not initialized");
      return;
    }
    // Log.i("RosAndroid", "onPreviewFrame");

    Size sz = camera.getParameters().getPreviewSize();
    if (image.data == null || image.data.length != data.length) image.data = new byte[data.length];

    // TODO(ethan) right now serialization is deferred. When serialization
    // happens inline, we don't need to copy.
    System.arraycopy(data, 0, image.data, 0, data.length);

    image.encoding = "8UC1"; // just plain old buffer
    image.step = sz.width;
    image.width = sz.width;
    image.height = sz.height + sz.height / 2; // yuv image, y is top width
                                              // *height, uv are width*height/2
                                              // on the end.
    image.header.stamp = Time.fromMillis(System.currentTimeMillis());
    image.header.frame_id = "android_camera";
    imagePublisher.publish(image);
    cameraInfo.header.stamp = image.header.stamp; // give camera info the same
                                                  // stamp as image
    cameraInfo.header.frame_id = "android_camera"; // TODO externalize this
                                                   // somehow.
    cameraInfo.width = sz.width;
    cameraInfo.height = sz.height;
    cameraInfoPublisher.publish(cameraInfo);
    // Log.i("RosAndroid", "onPreviewFrame complete");
    // FIXME camera calibration parameters.
  }

}
