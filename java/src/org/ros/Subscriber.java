package org.ros;

import com.google.common.collect.Sets;

import org.ros.internal.node.Response;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.SubscriberListener;
import org.ros.internal.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.message.Message;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Handle for subscription
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 * 
 * @param <MessageT>
 * 
 */
public class Subscriber<MessageT extends Message> {
  private SlaveClient slaveClient = null;
  Class<MessageT> clazz = null;
  org.ros.internal.topic.Subscriber<MessageT> subscriber = null;
  private String topicName;
  private String namespace;

  protected Subscriber(String namespace, String topicName, Class<MessageT> clazz)
      throws MalformedURLException {
    this.clazz = clazz;
    this.topicName = topicName;
    this.namespace = namespace;
  }

  protected void init(SlaveServer server, final Callback<MessageT> callback)
      throws InstantiationException, IllegalAccessException, IOException {

    // Set up topic definition.
    Message m = (Message) clazz.newInstance(); // a raw instance of a message

    TopicDefinition topicDefinition;
    topicDefinition = new TopicDefinition(topicName, MessageDefinition.createFromMessage(m));
    // Create a subscriber, and add a listener.
    subscriber = org.ros.internal.topic.Subscriber.create(topicName, topicDefinition, clazz);

    // pass through the callbacks
    subscriber.addListener(new SubscriberListener<MessageT>() {
      @Override
      public void onNewMessage(MessageT message) {
        callback.onNewMessage(message);
      }
    });
    // FIXME is this correct? Can we just use the server to request a topic
    slaveClient = new SlaveClient(namespace, server.getAddress());
    Response<ProtocolDescription> response = slaveClient.requestTopic(topicName,
        Sets.newHashSet(ProtocolNames.TCPROS));
    subscriber.start(response.getValue().getAddress());
  }

  /**
   * Cancel all callbacks listening on this topic.
   */
  public void cancel() {
    // FIXME cancel the subscrition
    subscriber.shutdown();
  }

}