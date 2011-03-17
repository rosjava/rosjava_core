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
import org.ros.MessageListener;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ServiceManager;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.Publisher;
import org.ros.internal.topic.PublisherIdentifier;
import org.ros.internal.topic.Subscriber;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.topic.TopicManager;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.internal.transport.tcp.TcpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    masterServer = new MasterServer(new InetSocketAddress(0));
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    TopicManager topicManager = new TopicManager();
    TcpServer tcpServer = new TcpServer(topicManager, new ServiceManager());
    tcpServer.start(new InetSocketAddress(0));
    slaveServer = new SlaveServer("/foo", masterClient, new InetSocketAddress(0));
    slaveServer.start(tcpServer.getAddress());
    Executor executor = Executors.newCachedThreadPool();

    TopicDefinition topicDefinition =
        new TopicDefinition("/hello",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    SlaveIdentifier pubSlaveIdentifer = new SlaveIdentifier("/pub", new URI("http://fake:1234"));
    PublisherIdentifier publisherIdentifier =
        new PublisherIdentifier(pubSlaveIdentifer, topicDefinition);
    Publisher<org.ros.message.std.String> publisher =
        new Publisher<org.ros.message.std.String>(topicDefinition, org.ros.message.std.String.class);
    topicManager.putPublisher(topicDefinition.getName(), publisher);
    slaveServer.addPublisher(publisher);

    SlaveIdentifier subSlaveIdentifier = new SlaveIdentifier("/bloop", new URI("http://fake:5678"));
    Subscriber<org.ros.message.std.String> subscriber =
        Subscriber.create(subSlaveIdentifier, topicDefinition, org.ros.message.std.String.class,
            executor);
    topicManager.putSubscriber(topicDefinition.getName(), subscriber);
    subscriber.addMessageListener(new MessageListener<org.ros.message.std.String>() {
      @Override
      public void onNewMessage(org.ros.message.std.String message) {
        System.out.println("Received message: " + message.data);
      }
    });

    slaveClient = new SlaveClient("/bar", slaveServer.getUri());
    Response<ProtocolDescription> response =
        slaveClient.requestTopic("/hello", Sets.newHashSet(ProtocolNames.TCPROS));
    subscriber.addPublisher(publisherIdentifier, response.getResult().getAddress());

    org.ros.message.std.String message = new org.ros.message.std.String();
    message.data = "Hello, ROS!";

    while (true) {
      publisher.publish(message);
      Thread.sleep(1000);
    }
  }

}
