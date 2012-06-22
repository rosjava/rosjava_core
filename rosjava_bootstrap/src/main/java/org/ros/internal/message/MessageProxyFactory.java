/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

import com.google.common.base.Preconditions;

import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageProxyFactory {

  private static final String HEADER_MESSAGE_TYPE = "std_msgs/Header";
  private static final String SEQUENCE_FIELD_NAME = "seq";
  private static final AtomicInteger SEQUENCE_NUMBER = new AtomicInteger(0);

  private final MessageInterfaceClassProvider messageInterfaceClassProvider;
  private final MessageContextProvider messageContextProvider;

  public MessageProxyFactory(MessageInterfaceClassProvider messageInterfaceClassProvider,
      MessageFactory messageFactory) {
    this.messageInterfaceClassProvider = messageInterfaceClassProvider;
    messageContextProvider = new MessageContextProvider(messageFactory);
  }

  // TODO(damonkohler): Use MessageDeclaration.
  @SuppressWarnings("unchecked")
  public <T> T newMessageProxy(String messageType, String messageDefinition) {
    Preconditions.checkNotNull(messageType);
    Preconditions.checkNotNull(messageDefinition);
    MessageContext context =
        messageContextProvider.provide(MessageDeclaration.of(messageType, messageDefinition));
    Class<T> messageInterfaceClass = (Class<T>) messageInterfaceClassProvider.get(messageType);
    MessageImpl implementation = newMessageProxyImplementation(context);
    return newProxy(messageInterfaceClass, implementation);
  }

  private MessageImpl newMessageProxyImplementation(MessageContext context) {
    MessageImpl implementation = new MessageImpl(context);
    if (implementation.getType().equals(HEADER_MESSAGE_TYPE)) {
      implementation.setUInt32(SEQUENCE_FIELD_NAME, SEQUENCE_NUMBER.incrementAndGet());
    }
    return implementation;
  }

  /**
   * @param interfaceClass
   *          the interface class to provide
   * @param implementation
   *          the instance to proxy
   * @return a new proxy for {@code implementation} that implements
   *         {@code interfaceClass}
   */
  @SuppressWarnings("unchecked")
  private <T> T newProxy(Class<T> interfaceClass, final MessageImpl implementation) {
    ClassLoader classLoader = implementation.getClass().getClassLoader();
    Class<?>[] interfaces = new Class<?>[] { interfaceClass, GetInstance.class };
    MessageProxyInvocationHandler invocationHandler =
        new MessageProxyInvocationHandler(implementation);
    return (T) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
  }
}
