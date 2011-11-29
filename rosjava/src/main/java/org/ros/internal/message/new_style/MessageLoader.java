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

package org.ros.internal.message.new_style;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageLoader implements MessageDefinitionProvider {

  private static final Log log = LogFactory.getLog(MessageLoader.class);

  private static final String STD_MSGS_HEADER_NAME = "std_msgs/Header";

  private final Collection<File> searchPaths;
  private final Map<String, String> messageDefinitions;

  public MessageLoader() {
    searchPaths = Sets.newHashSet();
    messageDefinitions = Maps.newConcurrentMap();
  }

  public void addSearchPath(File path) {
    searchPaths.add(path);
  }

  public void updateMessageDefinitions() {
    for (File searchPath : searchPaths) {
      findMessages(searchPath);
    }
  }

  private final class FindMessagesFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory() || pathname.getName().endsWith(".msg");
    }
  }

  private String pathToMessageName(File root, File message) {
    String absolutePath = message.getAbsolutePath();
    String relativePath =
        absolutePath.substring(root.getAbsolutePath().length() - root.getName().length());
    String strippedExtension = relativePath.substring(0, relativePath.length() - 4);
    String messageName = strippedExtension.replaceFirst("/msg/", "/");
    if (messageName.equals(STD_MSGS_HEADER_NAME)) {
      return "Header";
    }
    return messageName;
  }

  private void findMessages(File searchPath) {
    CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
    FindMessagesFilter filter = new FindMessagesFilter();
    Queue<File> childPaths = Lists.newLinkedList();
    childPaths.addAll(listPathEntries(searchPath, filter));
    while (!childPaths.isEmpty()) {
      File messagePath = childPaths.poll();
      if (messagePath.isDirectory()) {
        childPaths.addAll(listPathEntries(messagePath, filter));
      } else {
        try {
          addMessageDefinitionFromPaths(searchPath, messagePath, decoder);
        } catch (IOException e) {
          log.error("Failed to read message: " + messagePath.getAbsolutePath(), e);
        }
      }
    }
  }

  private void addMessageDefinitionFromPaths(File searchPath, File messagePath,
      CharsetDecoder decoder) throws IOException {
    FileInputStream inputStream = new FileInputStream(messagePath);
    FileChannel channel = inputStream.getChannel();
    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
    channel.read(buffer);
    buffer.rewind();
    decoder.reset();
    String definition = decoder.decode(buffer).toString().trim();
    messageDefinitions.put(pathToMessageName(searchPath, messagePath), definition);
    channel.close();
    inputStream.close();
  }

  private Collection<File> listPathEntries(File searchPath, FindMessagesFilter filter) {
    File[] entries = searchPath.listFiles(filter);
    if (entries == null) {
      return Lists.newArrayList();
    }
    return Lists.newArrayList(entries);
  }

  @Override
  public String get(String messageName) {
    return messageDefinitions.get(messageName);
  }

  @Override
  public boolean has(String messageName) {
    return messageDefinitions.containsKey(messageName);
  }

  @VisibleForTesting
  ImmutableMap<String, String> getMessageDefinitions() {
    return ImmutableMap.copyOf(messageDefinitions);
  }

  @VisibleForTesting
  void addMessageDefinition(String messageName, String messageDefinition) {
    messageDefinitions.put(messageName, messageDefinition);
  }

}
