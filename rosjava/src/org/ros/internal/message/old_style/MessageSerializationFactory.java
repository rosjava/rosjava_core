package org.ros.internal.message.old_style;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationFactory implements org.ros.message.MessageSerializationFactory {

  @Override
  public <MessageType> MessageSerializer<MessageType> createSerializer(String messageType) {
    return new MessageSerializer<MessageType>();
  }

  @Override
  public <MessageType> MessageDeserializer<MessageType> createDeserializer(String messageType) {
    return createDeserializer(messageType, "org.ros.message.");
  }

  @SuppressWarnings("unchecked")
  public <MessageType> MessageDeserializer<MessageType> createDeserializer(String messageType, String classpath) {
    Preconditions.checkArgument(messageType.split("/").length == 2);
    Class<MessageType> messageClass;
    try {
      messageClass =
        (Class<MessageType>) Class.forName(classpath + messageType.replace('/', '.'));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new MessageDeserializer<MessageType>(messageClass);
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> createServiceRequestSerializer(
      String serviceType) {
    return createSerializer("org.ros.service." + serviceType + "$Request");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType> createServiceRequestDeserializer(
      String serviceType) {
    return createDeserializer(serviceType + "$Request", "org.ros.service.");
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> createServiceResponseSerializer(
      String serviceType) {
    return createSerializer("org.ros.service." + serviceType + "$Response");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType> createServiceResponseDeserializer(
      String serviceType) {
    return createDeserializer(serviceType + "$Response", "org.ros.service.");
  }

}