package org.ros.internal.message.old_style;

import org.ros.exception.RosRuntimeException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationFactory implements org.ros.message.MessageSerializationFactory {

  @Override
  public <MessageType> MessageSerializer<MessageType> newMessageSerializer(String messageType) {
    return new MessageSerializer<MessageType>();
  }

  @Override
  public <MessageType> MessageDeserializer<MessageType> newMessageDeserializer(String messageType) {
    return createDeserializer(messageType,
        MessageFactory.ROS_MESSAGE_CLASS_PACKAGE_PREFIX);
  }

  @SuppressWarnings("unchecked")
  public <MessageType> MessageDeserializer<MessageType> createDeserializer(String messageType,
      String packagepath) {
    try {
      Class<MessageType> messageClass =
          (Class<MessageType>) MessageFactory.loadMessageClass(messageType, packagepath);
      return new MessageDeserializer<MessageType>(messageClass);
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceRequestSerializer(
      String serviceType) {
    return newMessageSerializer(MessageFactory.ROS_SERVICE_CLASS_PACKAGE_PREFIX + "."
        + serviceType + "$Request");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType>
      newServiceRequestDeserializer(String serviceType) {
    return createDeserializer(serviceType + "$Request",
        MessageFactory.ROS_SERVICE_CLASS_PACKAGE_PREFIX);
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceResponseSerializer(
      String serviceType) {
    return newMessageSerializer(MessageFactory.ROS_SERVICE_CLASS_PACKAGE_PREFIX + "."
        + serviceType + "$Response");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType>
      newServiceResponseDeserializer(String serviceType) {
    return createDeserializer(serviceType + "$Response",
        MessageFactory.ROS_SERVICE_CLASS_PACKAGE_PREFIX);
  }

}