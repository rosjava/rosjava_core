package org.ros;

import org.ros.ROS.Node;
import org.ros.ROS.Publisher;
import org.ros.ROS.Subscriber.Callback;
import org.ros.ROS.ros;
import org.ros.message.Time;
import org.ros.message.geometry.Point;
import org.ros.message.geometry.PoseStamped;
import org.ros.message.geometry.Quaternion;

public class SampleNode {

	public static void main(String[] argv) {
		Node node = new Node(argv, "sample_node");
		node.init();

		Publisher<PoseStamped> pub_pose = node.createPublisher("pose",
				PoseStamped.class);

		Callback<Quaternion> callback = new Callback<Quaternion>() {

			@Override
			public void onRecieve(Quaternion m) {
				ros.logi("Toto " + m.w);
			}
		};

		node.createSubscriber("foo", callback, Quaternion.class);

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
			node.spinOnce();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
