package org.ros.android.camera;

import org.ros.message.Time;

import android.hardware.Camera.Size;

import org.ros.Publisher;

import android.hardware.Camera;

import android.hardware.Camera.PreviewCallback;

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.message.sensor.Image;

import org.ros.Node;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class RosCameraNode implements PreviewCallback {
  private static final String ROS_CAMERA_TAG = "RosCamera";
  Node node;
  private Publisher<Image> imagePublisher;
  private Publisher<org.ros.message.std.String> talker;
  private int seq;

  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          // IPv4 only for now
          if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException ex) {
      Log.e(ROS_CAMERA_TAG, ex.toString());
    }
    return null;
  }

  public RosCameraNode() {
    // Find the total number of cameras available
    try {
      node = new Node("camera");
      Log.i(ROS_CAMERA_TAG, "My name is what? " + getLocalIpAddress());
      node.init("http://10.0.129.167:11311", getLocalIpAddress());
      imagePublisher = node.createPublisher("image_color", Image.class);
      talker = node.createPublisher("image_talker", org.ros.message.std.String.class);
    } catch (RosNameException e) {
      // TODO Auto-generated catch block
      Log.e(ROS_CAMERA_TAG, e.getMessage());
    } catch (RosInitException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    image = new Image();
  }

  Image image;

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
    image.height = sz.height + sz.height/2;
    image.header.stamp = Time.fromMillis(System.currentTimeMillis());
    image.header.frame_id = "android_camera";
    imagePublisher.publish(image);      
    
  }
}
