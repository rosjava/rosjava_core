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
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ServiceTest {

  private MessageLoader messageLoader;
  private DefaultedClassMap<Message> messageClassRegistry;
  private MessageFactory messageFactory;

  @Before
  public void setUp() {
    messageLoader = new MessageLoader();
    URL resource = this.getClass().getResource("/data/std_msgs");
    File searchPath = new File(resource.getPath());
    messageLoader.addSearchPath(searchPath);
    messageLoader.updateMessageDefinitions();
    messageClassRegistry = DefaultedClassMap.newFromDefaultClass(Message.class);
    messageFactory = new MessageFactory(messageLoader, messageClassRegistry);
  }

  @Test
  public void testCreateEchoService() {
    ServiceLoader loader = new ServiceLoader();
    loader.addServiceDefinition("Echo", "string data\n---\nstring data");
    ServiceFactory factory = new ServiceFactory(loader, messageFactory);
    Service echoService = factory.newService("Echo");
    Message request = echoService.getRequest();
    Message response = echoService.getResponse();
    request.setString("data", "Hello, ROS!");
    response.setString("data", "Hello, ROS!");
  }
}
