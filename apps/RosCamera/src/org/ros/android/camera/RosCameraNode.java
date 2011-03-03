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
package org.ros.android.camera;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.Ros;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.message.Time;
import org.ros.message.sensor.CameraInfo;
import org.ros.message.sensor.Image;

/**
 * A camera node that publishes images and camera_info
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class RosCameraNode implements PreviewCallback {
  private static final String ROS_CAMERA_TAG = "RosCamera";
  Node node;
  private Publisher<Image> imagePublisher;
  private Publisher<org.ros.message.std.String> talker;
  private int seq;
  private Publisher<CameraInfo> cameraInfoPublisher;

  /**
   * @param node_name
   *          the camera node name
   */
  public RosCameraNode(String masterURI,String node_name) {
    // Find the total number of cameras available
    try {
      node = new Node(node_name, Ros.getDefaultContext());
      Log.i(ROS_CAMERA_TAG, "My name is what? " + Ros.getLocalIpAddress());
      // FIXME resolve rosmaster from some global properties.
      node.init(masterURI, Ros.getLocalIpAddress());
      imagePublisher = node.createPublisher("~image_raw", Image.class);
      cameraInfoPublisher = node.createPublisher("~camera_info", CameraInfo.class);
      talker = node.createPublisher("~talker", org.ros.message.std.String.class);
    } catch (RosNameException e) {
      // TODO Auto-generated catch block
      Log.e(ROS_CAMERA_TAG, e.getMessage());
    } catch (RosInitException e) {
      // TODO Auto-generated catch block
      Log.e(ROS_CAMERA_TAG, e.getMessage());
    }

    image = new Image();
    cameraInfo = new CameraInfo();
  }

  Image image;
  CameraInfo cameraInfo;

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    org.ros.message.std.String str = new org.ros.message.std.String();
    str.data = "on new frame!" + seq++;
    talker.publish(str); // TODO interchange java string with ros String
    Size sz = camera.getParameters().getPreviewSize();
    image.data = data;
    image.encoding = "8UC1";
    image.step = sz.width;
    image.width = sz.width;
    image.height = sz.height + sz.height / 2;
    image.header.stamp = Time.fromMillis(System.currentTimeMillis());
    image.header.frame_id = "android_camera";
    imagePublisher.publish(image);
    cameraInfo.header.stamp = image.header.stamp;
    cameraInfo.header.frame_id = "android_camera";
    cameraInfo.width = sz.width;
    cameraInfo.width = sz.height;
    cameraInfoPublisher.publish(cameraInfo);
    // FIXME camera calibration parameters.
  }
}
