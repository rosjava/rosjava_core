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

package org.ros.node.service;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.ros.internal.message.Message;
import org.ros.internal.transport.ConnectionHeader;

/**
 * @author Spyros Koukas
 * @param <T>
 * @param <S>
 */
public interface ChannelBufferServiceServer<T extends Message, S extends Message> extends ServiceServer<T, S> {


    ChannelBuffer finishHandshake(final ConnectionHeader incomingHeader);

    ChannelHandler newRequestHandler();
}
