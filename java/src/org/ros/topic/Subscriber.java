package org.ros.topic;

import java.net.Socket;

public class Subscriber extends Topic {

  public Subscriber(TopicDescription description) {
    super(description, "localhost");
  }

  @Override
  protected void onNewConnection(Socket socket) {
  }

  public SubscriberDescription getDescription() {
    return null;
  }
  
}
