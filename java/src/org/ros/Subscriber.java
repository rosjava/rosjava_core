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

import com.google.common.collect.Sets;

import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.SubscriberListener;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.message.Message;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Handle for subscription
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 * 
 * @param <MessageType>
 * 
 */
public class Subscriber<MessageType extends Message> {

  private final Class<MessageType> messageClass;
  private final String topicName;
  private final String namespace;

  private SlaveClient slaveClient;
  private org.ros.internal.topic.Subscriber<MessageType> subscriber;

  protected Subscriber(String namespace, String topicName, Class<MessageType> messageClass) {
    this.messageClass = messageClass;
    this.topicName = topicName;
    this.namespace = namespace;
    subscriber = null;
  }

  /**
   * Cancel all callbacks listening on this topic.
   */
  public void cancel() {
    // FIXME cancel the subscrition
    subscriber.shutdown();
  }

  protected void init(SlaveServer server, final MessageListener<MessageType> callback)
      throws InstantiationException, IllegalAccessException, IOException, URISyntaxException,
      RemoteException {
    // Set up topic definition.
    Message m = messageClass.newInstance();
    TopicDefinition topicDefinition;
    topicDefinition = new TopicDefinition(topicName, MessageDefinition.createFromMessage(m));
    // Create a subscriber, and add a listener.
    subscriber = org.ros.internal.topic.Subscriber.create(topicName, topicDefinition, messageClass);

    // pass through the callbacks
    subscriber.addListener(new SubscriberListener<MessageType>() {
      @Override
      public void onNewMessage(MessageType message) {
        callback.onNewMessage(message);
      }
    });
    // FIXME is this correct? Can we just use the server to request a topic
    slaveClient = new SlaveClient(namespace, server.getUri());
    Response<ProtocolDescription> response = slaveClient.requestTopic(topicName,
        Sets.newHashSet(ProtocolNames.TCPROS));
    subscriber.start(response.getResult().getAddress());
  }

}