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
package org.ros.tutorials;

import com.google.common.collect.Sets;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.Response;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.SubscriberListener;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SimplePubSub {

  private static MasterServer masterServer;
  private static MasterClient masterClient;
  private static SlaveServer slaveServer;
  private static SlaveClient slaveClient;

  public static void main(String[] args) throws XmlRpcException, IOException, RemoteException,
      InterruptedException, URISyntaxException {
    masterServer = new MasterServer("localhost", 0);
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    slaveServer = new SlaveServer("/foo", masterClient, "localhost", 0);
    slaveServer.start();

    TopicDefinition topicDefinition = new TopicDefinition("/hello",
        MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    Publisher publisher = new Publisher(topicDefinition, "localhost", 0);
    publisher.start();
    slaveServer.addPublisher(publisher);

    Subscriber<org.ros.message.std.String> subscriber = Subscriber.create("/bloop",
        topicDefinition, org.ros.message.std.String.class);
    subscriber.addListener(new SubscriberListener<org.ros.message.std.String>() {
      @Override
      public void onNewMessage(org.ros.message.std.String message) {
        System.out.println("Received message: " + message.data);
      }
    });

    slaveClient = new SlaveClient("/bar", slaveServer.getUri());
    Response<ProtocolDescription> response = slaveClient.requestTopic("/hello",
        Sets.newHashSet(ProtocolNames.TCPROS));
    subscriber.start(response.getValue().getAddress());

    org.ros.message.std.String message = new org.ros.message.std.String();
    message.data = "Hello, ROS!";

    while (true) {
      publisher.publish(message);
      Thread.sleep(1000);
    }
  }

}
