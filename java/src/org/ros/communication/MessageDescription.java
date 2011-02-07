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

import java.util.Map;

import org.ros.transport.HeaderFields;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDescription {

  private final String type;
  private final String md5Checksum;

  public static MessageDescription CreateFromMessage(Message message) {
    return new MessageDescription(message.getDataType(), message.getMD5Sum());
  }

  public static MessageDescription CreateFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(HeaderFields.TYPE));
    Preconditions.checkArgument(header.containsKey(HeaderFields.MD5_CHECKSUM));
    return new MessageDescription(header.get(HeaderFields.TYPE),
        header.get(HeaderFields.MD5_CHECKSUM));
  }

  public MessageDescription(String type, String md5) {
    Preconditions.checkNotNull(type);
    this.type = type;
    this.md5Checksum = md5;
  }

  public String getType() {
    return type;
  }

  public String getMd5Checksum() {
    // TODO(damonkohler): This should move into the constructor. However,
    // presently we do not always know the MD5 at construction time.
    Preconditions.checkNotNull(md5Checksum);
    return md5Checksum;
  }

  public Map<String, String> toHeader() {
    Map<String, String> header = Maps.newHashMap();
    header.put(HeaderFields.TYPE, type);
    header.put(HeaderFields.MD5_CHECKSUM, md5Checksum);
    return header;
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
    if (md5Checksum == null) {
      if (other.md5Checksum != null) return false;
    } else if (!md5Checksum.equals(other.md5Checksum)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    return true;
  }
}
