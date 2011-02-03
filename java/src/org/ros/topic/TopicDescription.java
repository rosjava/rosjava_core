package org.ros.topic;

import java.util.Map;

import org.ros.communication.MessageDescription;
import org.ros.transport.HeaderFields;

import com.google.common.base.Preconditions;

public class TopicDescription {

  private final String name;
  private final MessageDescription messageDescription;

  public static TopicDescription CreateFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(HeaderFields.TOPIC));
    return new TopicDescription(header.get(HeaderFields.TOPIC), MessageDescription
        .CreateFromHeader(header));
  }

  public TopicDescription(String name, MessageDescription messageDescription) {
    this.name = name;
    this.messageDescription = messageDescription;
  }

  public String getName() {
    return name;
  }
  
  public String getMessageType() {
    return messageDescription.getName();
  }
  
  public String getMd5Checksum() {
    return messageDescription.getMd5Checksum();
  }

}
