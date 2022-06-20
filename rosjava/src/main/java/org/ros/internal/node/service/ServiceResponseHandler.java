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

package org.ros.internal.node.service;

import com.google.common.base.Preconditions;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.exception.RemoteException;
import org.ros.internal.node.response.StatusCode;
import org.ros.message.MessageDeserializer;
import org.ros.node.service.ServiceResponseListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * A Netty {@link SimpleChannelHandler} for service responses.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
final class ServiceResponseHandler<ResponseType> extends SimpleChannelHandler {

  private final ConcurrentLinkedQueue<ServiceResponseListener<ResponseType>> responseListeners;
  private final MessageDeserializer<ResponseType> deserializer;
  private final ExecutorService executorService;

  public ServiceResponseHandler(ConcurrentLinkedQueue<ServiceResponseListener<ResponseType>> messageListeners,
      MessageDeserializer<ResponseType> deserializer, ExecutorService executorService) {
    this.responseListeners = messageListeners;
    this.deserializer = deserializer;
    this.executorService = executorService;
  }

  @Override
  public final void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    final ServiceResponseListener<ResponseType> listener = this.responseListeners.poll();
    Preconditions.checkNotNull(listener, "No listener for incoming service response.");
    final ServiceServerResponse response = (ServiceServerResponse) e.getMessage();
    final ChannelBuffer buffer = response.getMessage();
    executorService.execute(() -> {
      if (response.getErrorCode() == 1) {
        listener.onSuccess(deserializer.deserialize(buffer));
      } else {
       final String message = StandardCharsets.US_ASCII.decode(buffer.toByteBuffer()).toString();
        listener.onFailure(new RemoteException(StatusCode.ERROR, message));
      }
    });
  }
}
