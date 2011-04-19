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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinition {

  private final String type;
  private final String definition;
  private final String md5Checksum;

  public static MessageDefinition createFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.TYPE));
    Preconditions.checkArgument(header.containsKey(ConnectionHeaderFields.MD5_CHECKSUM));
    return new MessageDefinition(header.get(ConnectionHeaderFields.TYPE), null,
        header.get(ConnectionHeaderFields.MD5_CHECKSUM));
  }

  public static MessageDefinition createFromTypeName(String type) {
    return new MessageDefinition(type, null, null);
  }

  public static MessageDefinition create(String type, String definition) {
    return new MessageDefinition(type, definition, DigestUtils.md5Hex(definition));
  }

  private MessageDefinition(String type, String definition, String md5Checksum) {
    Preconditions.checkNotNull(type);
    this.type = type;
    this.definition = definition;
    this.md5Checksum = md5Checksum;
  }

  public String getType() {
    return type;
  }

  public String getDefinition() {
    Preconditions.checkNotNull(definition);
    return definition;
  }

  public String getMd5Checksum() {
    Preconditions.checkNotNull(md5Checksum);
    return md5Checksum;
  }

  public Map<String, String> toHeader() {
    Preconditions.checkNotNull(md5Checksum);
    return new ImmutableMap.Builder<String, String>()
        .put(ConnectionHeaderFields.TYPE, type)
        .put(ConnectionHeaderFields.MD5_CHECKSUM, md5Checksum)
        .build();
  }

  @Override
  public String toString() {
    return "MessageDefinition<" + type + ", " + md5Checksum + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((md5Checksum == null) ? 0 : md5Checksum.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MessageDefinition other = (MessageDefinition) obj;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (md5Checksum == null || other.md5Checksum == null) {
      return true;
    } else if (!md5Checksum.equals(other.md5Checksum)) return false;
    return true;
  }

}
