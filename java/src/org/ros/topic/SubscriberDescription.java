package org.ros.topic;

import java.util.Map;

import org.ros.communication.MessageDescription;
import org.ros.transport.HeaderFields;

public class SubscriberDescription {

  private final String name;
  private final TopicDescription topicDescription;
  private final MessageDescription messageDescription;

  public static SubscriberDescription CreateFromHeader(Map<String, String> header) {
    String callerId = header.get(HeaderFields.CALLER_ID);
    return new SubscriberDescription(callerId, TopicDescription.CreateFromHeader(header),
        MessageDescription.CreateFromHeader(header));
  }

  public SubscriberDescription(String name, TopicDescription topicDescription,
      MessageDescription messageDescription) {
    this.name = name;
    this.topicDescription = topicDescription;
    this.messageDescription = messageDescription;
  }

  public String getName() {
    return name;
  }

  public TopicDescription getTopicDescription() {
    return topicDescription;
  }

  public MessageDescription getMessageDescription() {
    return messageDescription;
  }

}
