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
public class ServiceLoader {

  private static final Log log = LogFactory.getLog(ServiceLoader.class);

  private final Collection<File> searchPaths;
  private final Map<String, String> serviceDefinitions;

  public ServiceLoader() {
    searchPaths = Sets.newHashSet();
    serviceDefinitions = Maps.newConcurrentMap();
  }

  public void addSearchPath(File path) {
    searchPaths.add(path);
  }

  public void updateServiceDefinitions() {
    for (File searchPath : searchPaths) {
      findMessages(searchPath);
    }
  }

  private final class FindServicesFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory() || pathname.getName().endsWith(".srv");
    }
  }

  private String pathToServiceName(File root, File message) {
    String absolutePath = message.getAbsolutePath();
    String relativePath =
        absolutePath.substring(root.getAbsolutePath().length() - root.getName().length());
    String strippedExtension = relativePath.substring(0, relativePath.length() - 4);
    return strippedExtension.replaceFirst("/srv/", "/");
  }

  private void findMessages(File searchPath) {
    CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
    FindServicesFilter filter = new FindServicesFilter();
    Queue<File> childPaths = Lists.newLinkedList();
    childPaths.addAll(listPathEntries(searchPath, filter));
    while (!childPaths.isEmpty()) {
      File servicePath = childPaths.poll();
      if (servicePath.isDirectory()) {
        childPaths.addAll(listPathEntries(servicePath, filter));
      } else {
        try {
          addServiceDefinitionFromPaths(searchPath, servicePath, decoder);
        } catch (IOException e) {
          log.error("Failed to read service: " + servicePath.getAbsolutePath(), e);
        }
      }
    }
  }

  private void addServiceDefinitionFromPaths(File searchPath, File servicePath,
      CharsetDecoder decoder) throws IOException {
    FileInputStream inputStream = new FileInputStream(servicePath);
    FileChannel channel = inputStream.getChannel();
    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
    channel.read(buffer);
    buffer.rewind();
    decoder.reset();
    String definition = decoder.decode(buffer).toString().trim();
    serviceDefinitions.put(pathToServiceName(searchPath, servicePath), definition);
    channel.close();
    inputStream.close();
  }

  private Collection<File> listPathEntries(File searchPath, FindServicesFilter filter) {
    File[] entries = searchPath.listFiles(filter);
    if (entries == null) {
      return Lists.newArrayList();
    }
    return Lists.newArrayList(entries);
  }

  public String getServiceDefinition(String serviceName) {
    return serviceDefinitions.get(serviceName);
  }

  public boolean hasServiceDefinition(String serviceName) {
    return serviceDefinitions.containsKey(serviceName);
  }

  @VisibleForTesting
  ImmutableMap<String, String> getServiceDefinitions() {
    return ImmutableMap.copyOf(serviceDefinitions);
  }

  @VisibleForTesting
  void addServiceDefinition(String serviceName, String serviceDefinition) {
    serviceDefinitions.put(serviceName, serviceDefinition);
  }

}
