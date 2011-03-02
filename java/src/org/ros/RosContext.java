package org.ros;

import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.message.Message;

import org.ros.namespace.RosNamespace;

public class RosContext implements RosNamespace {

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topic_name,
      Class<MessageType> clazz) throws RosInitException, RosNameException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topic_name,
      MessageListener<MessageType> callback, Class<MessageType> clazz) throws RosInitException,
      RosNameException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String resolveName(String name) throws RosNameException {
    // TODO Auto-generated method stub
    return null;
  }

}
