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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.ros.internal.node.service.ServiceClient.DecodingState;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class ServiceResponseDecoder<ResponseMessageType> extends
    ReplayingDecoder<ServiceClient.DecodingState> {

  private ServiceServerResponse response;

  public ServiceResponseDecoder() {
    reset();
  }

  @SuppressWarnings("fallthrough")
  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer,
      ServiceClient.DecodingState state) throws Exception {
    switch (state) {
      case ERROR_CODE:
        response.setErrorCode(buffer.readByte());
        checkpoint(DecodingState.MESSAGE_LENGTH);
      case MESSAGE_LENGTH:
        response.setMessageLength(buffer.readInt());
        checkpoint(DecodingState.MESSAGE);
      case MESSAGE:
        response.setMessage(buffer.readBytes(response.getMessageLength()));
        try {
          return response;
        } finally {
          reset();
        }
      default:
        throw new IllegalStateException();
    }
  }

  private void reset() {
    checkpoint(DecodingState.ERROR_CODE);
    response = new ServiceServerResponse();
  }

}