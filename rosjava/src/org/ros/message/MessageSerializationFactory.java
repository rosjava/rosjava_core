package org.ros.message;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationFactory implements org.ros.MessageSerializationFactory {

  @Override
  public <MessageType> MessageSerializer<MessageType> createSerializer(String messageType) {
    return new MessageSerializer<MessageType>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <MessageType> MessageDeserializer<MessageType> createDeserializer(String messageType) {
    Preconditions.checkArgument(messageType.split("/").length == 2);
    Class<MessageType> messageClass;
    try {
      messageClass =
          (Class<MessageType>) Class.forName("org.ros.message." + messageType.replace('/', '.'));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new MessageDeserializer<MessageType>(messageClass);
  }

  @Override
  public <MessageType> org.ros.MessageSerializer<MessageType> createServiceRequestSerializer(
      String serviceType) {
    return createSerializer("srv." + serviceType + "$Request");
  }

  @Override
  public <MessageType> org.ros.MessageDeserializer<MessageType> createServiceRequestDeserializer(
      String serviceType) {
    return createDeserializer("srv." + serviceType + "$Request");
  }

  @Override
  public <MessageType> org.ros.MessageSerializer<MessageType> createServiceResponseSerializer(
      String serviceType) {
    return createSerializer("srv." + serviceType + "$Response");
  }

  @Override
  public <MessageType> org.ros.MessageDeserializer<MessageType> createServiceResponseDeserializer(
      String serviceType) {
    return createDeserializer("srv." + serviceType + "$Response");
  }

}