package org.ros.message;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public interface MessageFactory {   
  
  <T> T newMessage(String messageType);
  
}
