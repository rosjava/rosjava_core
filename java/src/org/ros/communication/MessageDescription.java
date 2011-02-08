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

package org.ros.communication;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.ros.transport.ConnectionHeaderFields;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDescription {

  private final String type;
  private final String md5Checksum;

  public static MessageDescription createFromMessage(Message message) {
    return new MessageDescription(message.getDataType(), message.getMD5Sum());
  }

  public static MessageDescription createFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.TYPE));
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.MD5_CHECKSUM));
    return new MessageDescription(header.get(ConnectionHeaderFields.TYPE),
        header.get(ConnectionHeaderFields.MD5_CHECKSUM));
  }

  public static MessageDescription createMessageDescription(String type) {
    return new MessageDescription(type, null);
  }

  private MessageDescription(String type, String md5Checksum) {
    Preconditions.checkNotNull(type);
    this.type = type;
    this.md5Checksum = md5Checksum;
  }

  public String getType() {
    return type;
  }

  public Map<String, String> toHeader() {
    Preconditions.checkNotNull(md5Checksum);
    return new ImmutableMap.Builder<String, String>().put(ConnectionHeaderFields.TYPE, type)
        .put(ConnectionHeaderFields.MD5_CHECKSUM, md5Checksum).build();
  }

  @Override
  public String toString() {
    return "MessageDescription<" + type + ", " + md5Checksum + ">";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((md5Checksum == null) ? 0 : md5Checksum.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MessageDescription other = (MessageDescription) obj;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (md5Checksum == null || other.md5Checksum == null) {
      return true;
    } else if (!md5Checksum.equals(other.md5Checksum)) return false;
    return true;
  }
}
