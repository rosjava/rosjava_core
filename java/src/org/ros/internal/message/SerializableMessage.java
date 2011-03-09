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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.message.Duration;
import org.ros.message.Time;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public abstract class SerializableMessage implements Message {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(SerializableMessage.class);

  public abstract int serializedLength();

  public abstract void serialize(ByteBuffer bb, int seq);

  public abstract void deserialize(ByteBuffer bb);

  public byte[] serialize(int seq) {
    int len = serializedLength();
    ByteBuffer bb = ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
    serialize(bb, seq);
    byte[] ret = bb.array();
    if (ret.length != len) {
      throw new RuntimeException("Non-matching serialization length!");
    }
    if (DEBUG) {
      log.info("Wrote " + ret.length + " bytes: ");
      for (int i = 0; i < ret.length; i++) {
        log.info(String.format("%x,", ret[i]));
      }
    }
    return ret;
  }

  public void deserialize(byte[] data) {
    if (DEBUG) {
      log.info("Read " + data.length + " bytes: ");
      for (int i = 0; i < Math.min(100, data.length); i++) {
        log.info(String.format("%x,", data[i]));
      }
    }
    ByteBuffer bb = ByteBuffer.wrap(data);
    deserialize(bb.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN));
  }

  @Override
  public Message clone() {
    try {
      return (Message) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Clone of message not supported?!");
    }
  }

  public static class Serialization {
    public static String readString(ByteBuffer bb) {
      int len = bb.getInt();
      byte[] bytes = new byte[len];
      bb.get(bytes);
      return new String(bytes);
    }

    public static Time readTime(ByteBuffer bb) {
      Time t = new Time(bb.getInt(), bb.getInt());
      return t;
    }

    public static Duration readDuration(ByteBuffer bb) {
      Duration t = new Duration(bb.getInt(), bb.getInt());
      return t;
    }

    public static void writeString(ByteBuffer bb, String s) {
      byte[] bytes = s.getBytes();
      bb.putInt(bytes.length);
      bb.put(bytes);
    }

    public static void writeTime(ByteBuffer bb, Time o) {
      bb.putInt(o.secs);
      bb.putInt(o.nsecs);
    }

    public static void writeDuration(ByteBuffer bb, Duration o) {
      bb.putInt(o.secs);
      bb.putInt(o.nsecs);
    }
  }
}
