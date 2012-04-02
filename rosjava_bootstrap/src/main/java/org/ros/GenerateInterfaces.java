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

package org.ros;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.io.FileUtils;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.message.DefaultMessageFactory;
import org.ros.internal.message.MessageDefinitionProviderChain;
import org.ros.internal.message.MessageDefinitionTupleParser;
import org.ros.internal.message.service.ServiceDefinitionFileProvider;
import org.ros.internal.message.topic.TopicDefinitionFileProvider;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class GenerateInterfaces {

  private final TopicDefinitionFileProvider topicDefinitionFileProvider;
  private final ServiceDefinitionFileProvider serviceDefinitionFileProvider;
  private final MessageDefinitionProviderChain messageDefinitionProviderChain;
  private final MessageFactory messageFactory;

  public GenerateInterfaces() {
    messageDefinitionProviderChain = new MessageDefinitionProviderChain();
    topicDefinitionFileProvider = new TopicDefinitionFileProvider();
    messageDefinitionProviderChain.addMessageDefinitionProvider(topicDefinitionFileProvider);
    serviceDefinitionFileProvider = new ServiceDefinitionFileProvider();
    messageDefinitionProviderChain.addMessageDefinitionProvider(serviceDefinitionFileProvider);
    messageFactory = new DefaultMessageFactory(messageDefinitionProviderChain);
  }

  /**
   * @param packages
   *          a list of packages containing the topic types to generate
   *          interfaces for
   * @param outputDirectory
   *          the directory to write the generated interfaces to
   * @throws IOException
   */
  private void writeTopicInterfaces(File outputDirectory, Collection<String> packages)
      throws IOException {
    Collection<MessageIdentifier> topicTypes = Sets.newHashSet();
    if (packages.size() == 0) {
      packages = topicDefinitionFileProvider.getPackages();
    }
    for (String pkg : packages) {
      Collection<MessageIdentifier> messageIdentifiers =
          topicDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        topicTypes.addAll(messageIdentifiers);
      }
    }
    for (MessageIdentifier topicType : topicTypes) {
      File file = new File(outputDirectory, topicType.getType() + ".java");
      String definition = messageDefinitionProviderChain.get(topicType.getType());
      MessageDeclaration messageDeclaration = new MessageDeclaration(topicType, definition);
      MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
      builder.setMessageDeclaration(messageDeclaration);
      builder.setPackageName(messageDeclaration.getPackage());
      builder.setInterfaceName(messageDeclaration.getName());
      builder.setAddConstantsAndMethods(true);
      FileUtils.writeStringToFile(file, builder.build(messageFactory));
    }
  }

  /**
   * @param packages
   *          a list of packages containing the topic types to generate
   *          interfaces for
   * @param outputDirectory
   *          the directory to write the generated interfaces to
   * @throws IOException
   */
  private void writeServiceInterfaces(File outputDirectory, Collection<String> packages)
      throws IOException {
    Collection<MessageIdentifier> serviceTypes = Sets.newHashSet();
    if (packages.size() == 0) {
      packages = serviceDefinitionFileProvider.getPackages();
    }
    for (String pkg : packages) {
      Collection<MessageIdentifier> messageIdentifiers =
          serviceDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        serviceTypes.addAll(messageIdentifiers);
      }
    }
    for (MessageIdentifier serviceType : serviceTypes) {
      File file = new File(outputDirectory, serviceType.getType() + ".java");
      String definition = messageDefinitionProviderChain.get(serviceType.getType());

      MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
      builder.setMessageDeclaration(new MessageDeclaration(serviceType, definition));
      builder.setPackageName(serviceType.getPackage());
      builder.setInterfaceName(serviceType.getName());
      builder.setAddConstantsAndMethods(false);

      List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);
      MessageDeclaration request = new MessageDeclaration(serviceType, requestAndResponse.get(0));
      MessageDeclaration response = new MessageDeclaration(serviceType, requestAndResponse.get(1));

      MessageInterfaceBuilder requestBuilder = new MessageInterfaceBuilder();
      requestBuilder.setMessageDeclaration(request);
      requestBuilder.setInterfaceName("Request");
      requestBuilder.setAddConstantsAndMethods(true);

      MessageInterfaceBuilder responseBuilder = new MessageInterfaceBuilder();
      responseBuilder.setMessageDeclaration(response);
      responseBuilder.setInterfaceName("Response");
      responseBuilder.setAddConstantsAndMethods(true);

      String nestedContent =
          requestBuilder.build(messageFactory) + "\n" + responseBuilder.build(messageFactory);
      builder.setNestedContent(nestedContent);
      FileUtils.writeStringToFile(file, builder.build(null));
    }
  }

  public void generate(File outputDirectory, Collection<String> packages,
      Collection<File> packagePath) {
    for (File directory : packagePath) {
      topicDefinitionFileProvider.addDirectory(directory);
      serviceDefinitionFileProvider.addDirectory(directory);
    }
    topicDefinitionFileProvider.update();
    serviceDefinitionFileProvider.update();
    try {
      writeTopicInterfaces(outputDirectory, packages);
      writeServiceInterfaces(outputDirectory, packages);
    } catch (IOException e) {
      throw new RosRuntimeException(e);
    }
  }

  public static void main(String[] args) {
    List<String> arguments = Lists.newArrayList(args);
    if (arguments.size() == 0) {
      arguments.add(".");
    }
    File outputDirectory = new File(arguments.remove(0));
    String rosPackagePath = System.getenv(EnvironmentVariables.ROS_PACKAGE_PATH);
    Collection<File> packagePath = Lists.newArrayList();
    for (String path : rosPackagePath.split(File.pathSeparator)) {
      packagePath.add(new File(path));
    }
    GenerateInterfaces generateInterfaces = new GenerateInterfaces();
    generateInterfaces.generate(outputDirectory, arguments, packagePath);
  }
}
