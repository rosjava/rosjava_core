package org.ros;

import org.ros.Ros;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.TopicDefinition;
import org.ros.message.Message;

import java.io.IOException;

/**
 * A handle for publishing messages of a particular type on a given topic.
 * 
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 * 
 * @param <MessageT>
 *          The message type to template on. The publisher may only publish
 *          messages of this type.
 */
public class Publisher<MessageT extends Message> {

  Publisher(String topic_name, Class<MessageT> clazz) {
    this.topic_name = topic_name;
    this.clazz = clazz;
  }

  /**
   * @param m
   *          The message to publish. This message will be available on the
   *          topic that this Publisher has been associated with.
   */
  public void publish(MessageT m) {
    publisher.publish(m);
  }

  /**
   * This starts up the topic
   * 
   * @throws IOException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  protected void start() throws IOException, InstantiationException, IllegalAccessException {

    // create an instance of the message of type MessageT
    Message m = (Message) clazz.newInstance();
    
    TopicDefinition topicDefinition;
    topicDefinition = new TopicDefinition(topic_name, MessageDefinition.createFromMessage(m));
    publisher = new org.ros.internal.topic.Publisher(topicDefinition, Ros.getHostName(), 0);
    publisher.start();
  }

  org.ros.internal.topic.Publisher publisher;
  String topic_name;
  // deal with type erasure for generics
  Class<MessageT> clazz;
}