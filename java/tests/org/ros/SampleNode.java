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
package org.ros;

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.message.geometry_msgs.Point;
import org.ros.message.geometry_msgs.PoseStamped;
import org.ros.message.geometry_msgs.Quaternion;

public class SampleNode {

  public static void main(String[] argv) throws RosInitException, RosNameException {
    // Node node = new Node(argv, "sample_node"); this crashes when topic is
    // subscribed to
    // Node node = new Node(argv, "sample_rosjava_node");
    
    RosLoader loader = new CommandLineLoader(argv);
    Node node = new Node("sample_rosjava_node", loader.createContext());
    node.init();

    Publisher<PoseStamped> pub_pose = node.createPublisher("pose", PoseStamped.class);

    PoseStamped p = new PoseStamped();
    pub_pose.publish(p);

    Point origin;
    origin = new Point();
    origin.x = 0;
    origin.y = 0;
    origin.z = 0;
    int seq = 0;

    while (true) {
      float[] quaternion = new float[4];
      Quaternion orientation = new Quaternion();
      orientation.w = quaternion[0];
      orientation.x = quaternion[1];
      orientation.y = quaternion[2];
      orientation.z = quaternion[3];
      PoseStamped pose = new PoseStamped();
      pose.header.frame_id = "/map";
      pose.header.seq = seq++;
      pose.header.stamp = node.currentTime();
      pose.pose.position = origin;
      pose.pose.orientation = orientation;
      pub_pose.publish(pose);
    }

  }
}
