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

package org.ros.internal.message.context;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.ros.exception.RosRuntimeException;
import org.ros.internal.message.definition.MessageDefinitionParser;
import org.ros.internal.message.definition.MessageDefinitionParser.MessageDefinitionVisitor;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;

import java.util.concurrent.ExecutionException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageContextProvider {

  private final LoadingCache<MessageDeclaration, MessageContext> cache;

  public MessageContextProvider(final MessageFactory messageFactory) {
    Preconditions.checkNotNull(messageFactory);
    cache = CacheBuilder.newBuilder().build(new CacheLoader<MessageDeclaration, MessageContext>() {
      @Override
      public MessageContext load(MessageDeclaration messageDeclaration) throws Exception {
        MessageContext messageContext = new MessageContext(messageDeclaration, messageFactory);
        MessageDefinitionVisitor visitor = new MessageContextBuilder(messageContext);
        MessageDefinitionParser messageDefinitionParser = new MessageDefinitionParser(visitor);
        messageDefinitionParser.parse(messageDeclaration.getType(),
            messageDeclaration.getDefinition());
        return messageContext;
      }
    });
  }

  public MessageContext of(MessageDeclaration messageDeclaration) {
    try {
      return cache.get(messageDeclaration);
    } catch (ExecutionException e) {
      throw new RosRuntimeException(e);
    }
  }
}
