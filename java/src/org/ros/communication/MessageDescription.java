package org.ros.communication;

import java.util.Map;

import org.ros.transport.HeaderFields;

import com.google.common.base.Preconditions;

public class MessageDescription {
  
  private final String name;
  private final String md5Checksum;
  
  public static MessageDescription CreateFromHeader(Map<String, String> header) {
    Preconditions.checkArgument(header.containsKey(HeaderFields.TYPE));
    Preconditions.checkArgument(header.containsKey(HeaderFields.MD5_CHECKSUM));
    return new MessageDescription(header.get(HeaderFields.TYPE), header.get(HeaderFields.MD5_CHECKSUM));
  }
  
  public MessageDescription(String name, String md5) {
    this.name = name;
    this.md5Checksum = md5;
  }

  public String getName() {
    return name;
  }

  public String getMd5Checksum() {
    return md5Checksum;
  }

}
