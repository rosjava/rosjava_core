package com.googlecode.rosjava.android;

import java.io.IOException;
import java.net.URL;

import org.ros.communication.MessageDescription;
import org.ros.node.client.Master;
import org.ros.node.server.Slave;
import org.ros.topic.Publisher;
import org.ros.topic.TopicDescription;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {

  private static class Chatter extends Publisher {

    public Chatter(String hostname) {
      super(new TopicDescription("/chatter", new MessageDescription(
          org.ros.communication.std_msgs.String.__s_getDataType(),
          org.ros.communication.std_msgs.String.__s_getMD5Sum())), hostname);
    }

    @Override
    public void start(int port) throws IOException {
      super.start(port);
      (new Thread() {
        @Override
        public void run() {
          org.ros.communication.std_msgs.String message =
              new org.ros.communication.std_msgs.String();
          message.data = "Hello, ROS!";
          try {
            while (true) {
              publish(message);
              Thread.sleep(1000);
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    try {
      Slave slave = new Slave();
      Publisher publisher = new Chatter("localhost");
      publisher.start(7332);
      slave.addPublisher(publisher);
      slave.start(7331);
      Master master = new Master(new URL("http://10.0.2.2:11311/"));
      master.registerPublisher("/foo", publisher, "http://localhost:7331");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
