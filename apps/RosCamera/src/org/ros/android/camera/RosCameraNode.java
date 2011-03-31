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

import com.google.common.primitives.UnsignedBytes;

import org.ros.NodeContext;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.message.Time;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.Image;

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
  private Publisher<CameraInfo> cameraInfoPublisher;

  /**
   * @param masterURI
   *          Uri of our ros master to use.
   * @param node_name
   *          The name of this node. the camera node name
   */
  public RosCameraNode(String masterURI, String node_name) {

    try {
      node = new Node(node_name, new NodeContext());
      // FIXME resolve rosmaster from some global properties.
      String camera_ns = node.resolveName("camera");
      // RosNamespace camera_ns = node.createNamespace("camera");
      // create image and camera info topics on local namespace
      imagePublisher = node.createPublisher(camera_ns + "/image_raw", Image.class);
      cameraInfoPublisher = node.createPublisher(camera_ns + "/camera_info", CameraInfo.class);
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
    Size sz = camera.getParameters().getPreviewSize();
    if (image.data == null || image.data.length != data.length)
      image.data = new byte[data.length];
    System.arraycopy(data, 0, image.data, 0, data.length);
    // FIXME!!
    // image.data = new short[data.length];
    image.data = data;
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
    cameraInfo.width = sz.height;
    cameraInfoPublisher.publish(cameraInfo);
    // FIXME camera calibration parameters.
  }

  public void shutdown() {
    // node.shutdown();
  }
}
