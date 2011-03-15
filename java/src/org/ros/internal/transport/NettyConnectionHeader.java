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

package org.ros.internal.transport;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class NettyConnectionHeader {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(NettyConnectionHeader.class);

  private static final int ESTIMATED_HEADER_SIZE = 1024;

  private NettyConnectionHeader() {
    // Utility class
  }

  private static String decodeAsciiString(ChannelBuffer buffer, int length) {
    return buffer.readBytes(length).toString(Charset.forName("US-ASCII"));
  }

  /**
   * Decodes a header that came over the wire into a {@link Map} of fields and values.
   * 
   * @param buffer the incoming {@link ChannelBuffer} containing the header
   * @return a {@link Map} of header fields and values
   */
  public static Map<String, String> decode(ChannelBuffer buffer) {
    Map<String, String> result = Maps.newHashMap();
    int position = 0;
    int readableBytes = buffer.readableBytes();
    while (position < readableBytes) {
      int fieldSize = buffer.readInt();
      position += 4;
      if (fieldSize == 0) {
        throw new IllegalStateException("Invalid 0 length handshake header field.");
      }
      if (position + fieldSize > readableBytes) {
        throw new IllegalStateException("Invalid line length handshake header field.");
      }
      String field = decodeAsciiString(buffer, fieldSize);
      position += field.length();
      if (field.indexOf("=") == -1) {
        throw new IllegalStateException("Invalid line in handshake header: [" + field + "]");
      }
      String[] keyAndValue = field.split("=");
      result.put(keyAndValue[0], keyAndValue[1]);
    }
    if (DEBUG) {
      log.info("Decoded header: " + result);
    }
    return result;
  }

  /**
   * Encodes a header {@link Map} of fields and values for transmission over the wire.
   * 
   * @param header a {@link Map} of header fields and values
   * @return a {@link ChannelBuffer} containing the encoded header for wire transmission
   */
  public static ChannelBuffer encode(Map<String, String> header) {
    ChannelBuffer buffer =
        ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, ESTIMATED_HEADER_SIZE);
    for (Entry<String, String> entry : header.entrySet()) {
      String field = entry.getKey() + "=" + entry.getValue();
      buffer.writeInt(field.length());
      buffer.writeBytes(field.getBytes(Charset.forName("US-ASCII")));
    }
    return buffer;
  }

}
