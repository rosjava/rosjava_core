package org.ros.internal.message.old_style;

import com.google.common.base.Preconditions;

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
      throw new RosRuntimeException(e);
    }
    return new MessageDeserializer<MessageType>(messageClass);
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceRequestSerializer(
      String serviceType) {
    return newMessageSerializer("org.ros.service." + serviceType + "$Request");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType> newServiceRequestDeserializer(
      String serviceType) {
    return createDeserializer(serviceType + "$Request", "org.ros.service.");
  }

  @Override
  public <MessageType> org.ros.message.MessageSerializer<MessageType> newServiceResponseSerializer(
      String serviceType) {
    return newMessageSerializer("org.ros.service." + serviceType + "$Response");
  }

  @Override
  public <MessageType> org.ros.message.MessageDeserializer<MessageType> newServiceResponseDeserializer(
      String serviceType) {
    return createDeserializer(serviceType + "$Response", "org.ros.service.");
  }

}