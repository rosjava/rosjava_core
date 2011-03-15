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

package org.ros.internal.topic;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.NettyConnectionHeader;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherTest {

  @Test
  public void testHandshake() {
    TopicDefinition topicDefinition =
        new TopicDefinition("/topic",
            MessageDefinition.createFromMessage(new org.ros.message.std.String()));
    SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/caller", null);
    SubscriberIdentifier subscriberDescription =
        new SubscriberIdentifier(slaveIdentifier, topicDefinition);
    Map<String, String> header = subscriberDescription.toHeader();
    ChannelBuffer subscriberHeader = NettyConnectionHeader.encode(header);

    Publisher publisher = new Publisher(topicDefinition);
    ChannelBuffer publisherHeader = publisher.handshake(subscriberHeader);
    Map<String, String> result = NettyConnectionHeader.decode(publisherHeader);
    assertEquals(result.get(ConnectionHeaderFields.TYPE), header.get(ConnectionHeaderFields.TYPE));
    assertEquals(result.get(ConnectionHeaderFields.MD5_CHECKSUM),
        header.get(ConnectionHeaderFields.MD5_CHECKSUM));
  }
}
