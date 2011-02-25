package org.ros;

import org.ros.exceptions.RosInitException;
import org.ros.message.Time;
import org.ros.message.geometry.Point;
import org.ros.message.geometry.PoseStamped;
import org.ros.message.geometry.Quaternion;

public class SampleNode {

  public static void main(String[] argv) throws RosInitException {
    // Node node = new Node(argv, "sample_node"); this crashes when topic is
    // subscribed to
    Node node = new Node(argv, "/sample_node");
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
      pose.header.stamp = Time.now();
      pose.pose.position = origin;
      pose.pose.orientation = orientation;
      pub_pose.publish(pose);
    }

  }
}
