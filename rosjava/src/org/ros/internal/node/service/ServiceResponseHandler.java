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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.exception.RemoteException;
import org.ros.internal.node.response.StatusCode;
import org.ros.message.MessageDeserializer;
import org.ros.node.service.ServiceResponseListener;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Queue;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class ServiceResponseHandler<ResponseType> extends SimpleChannelHandler {

  private final Queue<ServiceResponseListener<ResponseType>> responseListeners;
  private final MessageDeserializer<ResponseType> deserializer;

  ServiceResponseHandler(
      Queue<ServiceResponseListener<ResponseType>> messageListeners,
      MessageDeserializer<ResponseType> deserializer) {
    this.responseListeners = messageListeners;
    this.deserializer = deserializer;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ServiceResponseListener<ResponseType> listener = responseListeners.poll();
    Preconditions.checkNotNull(listener);
    ServiceServerResponse response = (ServiceServerResponse) e.getMessage();
    ByteBuffer buffer = response.getMessage().toByteBuffer();
    if (response.getErrorCode() == 1) {
      listener.onSuccess(deserializer.deserialize(buffer));
    } else {
      String message = Charset.forName("US-ASCII").decode(buffer).toString();
      listener.onFailure(new RemoteException(StatusCode.ERROR, message));
    }
  }

}
