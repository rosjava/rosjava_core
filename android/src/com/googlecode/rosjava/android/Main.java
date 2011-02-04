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

    public Chatter(String hostname, int port) throws IOException {
      super(new TopicDescription("/chatter",
          MessageDescription.CreateFromMessage(new org.ros.communication.std_msgs.String())),
          hostname, port);
    }

    @Override
    public void start() {
      super.start();
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
      Master master = new Master(new URL("http://10.0.2.2:11311/"));
      Slave slave = new Slave("/android", master, "localhost", 7331);
      Publisher publisher = new Chatter("localhost", 7332);
      publisher.start();
      slave.addPublisher(publisher);
      slave.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
